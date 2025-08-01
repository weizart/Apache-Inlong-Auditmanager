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

import org.apache.inlong.audit.tool.dao.mapper.AuditDataMapper;
import org.apache.inlong.audit.tool.service.AlarmPolicyService;
import org.apache.inlong.audit.tool.service.ReporterFactory;
import org.apache.inlong.manager.dao.entity.AuditAlarmPolicyEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class AuditDataCheckerTest {

    @InjectMocks
    private AuditDataChecker auditDataChecker;

    @Mock
    private AlarmPolicyService alarmPolicyService;

    @Mock
    private AuditDataMapper auditDataMapper;

    @Mock
    private ReporterFactory reporterFactory;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCheckWithThresholdAlarm() {
        AuditAlarmPolicyEntity policy = new AuditAlarmPolicyEntity();
        policy.setId(1);
        policy.setAlarmType("THRESHOLD");
        policy.setTriggerType("GREATER_THAN");
        policy.setThreshold(100L);
        policy.setAuditIds("1,2");
        policy.setInlongGroupId("test-group");
        policy.setInlongStreamId("test-stream");

        when(alarmPolicyService.getAllAlarmPolicies()).thenReturn(Collections.singletonList(policy));
        when(auditDataMapper.selectByCondition(any(), any(), eq("1"), any(), any()))
                .thenReturn(Collections.emptyList());
        when(auditDataMapper.selectByCondition(any(), any(), eq("2"), any(), any()))
                .thenReturn(Collections.emptyList());

        auditDataChecker.check();

        verify(reporterFactory, never()).getReporter(any());
    }
}