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

package org.apache.inlong.audit.tool.service;

import org.apache.inlong.manager.dao.entity.AuditAlarmPolicyEntity;

import io.prometheus.client.Gauge;
import org.springframework.stereotype.Service;

@Service("PROMETHEUS")
public class PrometheusReporter implements Reporter {

    private static final Gauge AUDIT_DATA_MISMATCH = Gauge.build()
            .name("inlong_audit_data_mismatch")
            .help("Mismatch in audit data")
            .labelNames("inlong_group_id", "inlong_stream_id", "audit_ids")
            .register();

    @Override
    public void report(AuditAlarmPolicyEntity policy, long count1, long count2) {
        long diff = Math.abs(count1 - count2);
        AUDIT_DATA_MISMATCH.labels(policy.getInlongGroupId(), policy.getInlongStreamId(), policy.getAuditIds())
                .set(diff);
    }
}