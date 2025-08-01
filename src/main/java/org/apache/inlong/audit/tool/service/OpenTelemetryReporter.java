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

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("OPENTELEMETRY")
public class OpenTelemetryReporter implements Reporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenTelemetryReporter.class);

    @Autowired
    private Tracer tracer;

    @Override
    public void report(AuditAlarmPolicyEntity policy, long count1, long count2) {
        LOGGER.info("Reporting audit data mismatch to OpenTelemetry for policy {}", policy.getId());
        Span span = tracer.spanBuilder("audit-alarm").startSpan();
        try {
            span.setAttribute("inlong.group.id", policy.getInlongGroupId());
            span.setAttribute("inlong.stream.id", policy.getInlongStreamId());
            span.setAttribute("audit.ids", policy.getAuditIds());
            span.setAttribute("alarm.type", policy.getAlarmType());
            span.setAttribute("trigger.type", policy.getTriggerType());
            span.setAttribute("count1", count1);
            span.setAttribute("count2", count2);
            span.setAttribute("mismatch.value", Math.abs(count1 - count2));
        } finally {
            span.end();
        }
    }
}