/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.inlong.audit.tool.task;

import org.apache.inlong.audit.tool.dao.entity.AuditData;
import org.apache.inlong.audit.tool.dao.mapper.AuditDataMapper;
import org.apache.inlong.audit.tool.service.AlarmPolicyService;
import org.apache.inlong.audit.tool.service.ReporterFactory;
import org.apache.inlong.manager.dao.entity.AuditAlarmPolicyEntity;
import org.apache.inlong.manager.pojo.audit.AlarmType;
import org.apache.inlong.manager.pojo.audit.TriggerType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class AuditDataChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditDataChecker.class);

    @Autowired
    private AlarmPolicyService alarmPolicyService;

    @Autowired
    private AuditDataMapper auditDataMapper;

    @Autowired
    private ReporterFactory reporterFactory;

    @Scheduled(cron = "0 0/1 * * * ?") // every minute
    public void check() {
        LOGGER.info("Start to check audit data according to alarm policies");
        List<AuditAlarmPolicyEntity> policies = alarmPolicyService.getAllAlarmPolicies();
        LOGGER.info("Got {} alarm policies", policies.size());

        for (AuditAlarmPolicyEntity policy : policies) {
            processPolicy(policy);
        }
    }

    private void processPolicy(AuditAlarmPolicyEntity policy) {
        String[] auditIds = policy.getAuditIds().split(",");
        if (auditIds.length < 2) {
            LOGGER.warn("At least two audit IDs are required for comparison, policy id: {}", policy.getId());
            return;
        }

        long now = System.currentTimeMillis();
        Date endTime = new Date(now);
        Date startTime = new Date(now - TimeUnit.MINUTES.toMillis(1)); // Check last minute data

        List<AuditData> data1 = auditDataMapper.selectByCondition(
                policy.getInlongGroupId(),
                policy.getInlongStreamId(),
                auditIds[0],
                startTime,
                endTime);

        List<AuditData> data2 = auditDataMapper.selectByCondition(
                policy.getInlongGroupId(),
                policy.getInlongStreamId(),
                auditIds[1],
                startTime,
                endTime);

        long count1 = data1.stream().mapToLong(AuditData::getCount).sum();
        long count2 = data2.stream().mapToLong(AuditData::getCount).sum();

        LOGGER.info("Comparing audit data for policy {}, group {}, stream {}, auditIds {}: count1={}, count2={}",
                policy.getId(), policy.getInlongGroupId(), policy.getInlongStreamId(), policy.getAuditIds(), count1,
                count2);

        boolean shouldReport = checkAlarm(policy, count1, count2);

        if (shouldReport) {
            LOGGER.warn("Audit data mismatch for policy {}, group {}, stream {}, auditIds {}: count1={}, count2={}",
                    policy.getId(), policy.getInlongGroupId(), policy.getInlongStreamId(), policy.getAuditIds(), count1,
                    count2);
            reporterFactory.getReporter(policy.getNotificationType())
                    .ifPresent(reporter -> reporter.report(policy, count1, count2));
        }
    }

    private boolean checkAlarm(AuditAlarmPolicyEntity policy, long count1, long count2) {
        try {
            AlarmType alarmType = AlarmType.valueOf(policy.getAlarmType());
            TriggerType triggerType = TriggerType.valueOf(policy.getTriggerType());

            switch (alarmType) {
                case THRESHOLD:
                    long diff = Math.abs(count1 - count2);
                    return checkThreshold(diff, policy.getThreshold(), triggerType);
                case CHANGE_RATE:
                    if (count1 == 0)
                        return false; // Avoid division by zero
                    BigDecimal changeRate = new BigDecimal(Math.abs(count1 - count2))
                            .divide(new BigDecimal(count1), 2, BigDecimal.ROUND_HALF_UP);
                    return checkThreshold(changeRate.longValue(), policy.getChangeRate().longValue(), triggerType);
                default:
                    return false;
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid alarm type or trigger type in policy {}", policy.getId(), e);
            return false;
        }
    }

    private boolean checkThreshold(long value, long threshold, TriggerType triggerType) {
        switch (triggerType) {
            case GREATER_THAN:
                return value > threshold;
            case LESS_THAN:
                return value < threshold;
            default:
                return false;
        }
    }
}