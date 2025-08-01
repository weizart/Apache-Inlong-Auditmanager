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

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();

        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");

        errorDetails.put("timestamp", System.currentTimeMillis());
        errorDetails.put("status", statusCode);
        errorDetails.put("error", getErrorDescription(statusCode));
        errorDetails.put("message", errorMessage != null ? errorMessage : "No message available");
        errorDetails.put("path", requestUri);

        if (exception != null) {
            errorDetails.put("exception", exception.getClass().getName());
        }

        errorDetails.put("available_endpoints", new String[]{
                "/ - Home page",
                "/health - Health check",
                "/info - Application info",
                "/api/audit/alarm/list - List audit alarm policies",
                "/actuator/health - Spring Boot health check",
                "/actuator/prometheus - Prometheus metrics"
        });

        HttpStatus httpStatus = HttpStatus.valueOf(statusCode != null ? statusCode : 500);
        return new ResponseEntity<>(errorDetails, httpStatus);
    }

    private String getErrorDescription(Integer statusCode) {
        if (statusCode == null) {
            return "Internal Server Error";
        }

        switch (statusCode) {
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 405:
                return "Method Not Allowed";
            case 500:
                return "Internal Server Error";
            case 502:
                return "Bad Gateway";
            case 503:
                return "Service Unavailable";
            default:
                return "Unknown Error";
        }
    }
}