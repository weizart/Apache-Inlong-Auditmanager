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

package org.apache.inlong.audit.tool.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Apache InLong Audit Tool");
        response.put("version", "2.3.0-SNAPSHOT");
        response.put("description", "Audit data consistency checking tool for Apache InLong");
        response.put("endpoints", new String[]{
                "/api/audit/alarm/list - List audit alarm policies",
                "/actuator/health - Application health check",
                "/actuator/prometheus - Prometheus metrics",
                "/actuator/info - Application information"
        });
        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "inlong-audit-tool");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "Apache InLong Audit Tool");
        response.put("version", "2.3.0-SNAPSHOT");
        response.put("description", "Audit data consistency checking tool for Apache InLong");
        response.put("features", new String[]{
                "Audit alarm policy management",
                "Data consistency checking",
                "Prometheus metrics",
                "OpenTelemetry tracing"
        });
        return response;
    }
}