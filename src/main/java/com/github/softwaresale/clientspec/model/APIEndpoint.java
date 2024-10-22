package com.github.softwaresale.clientspec.model;

import java.util.Map;

public record APIEndpoint(
        String name,
        String endpoint,
        String method,
        Map<String, RequestValue> pathVariables,
        Map<String, RequestValue> queryVariables,
        RequestValue requestBody,
        RequestValue responseBody
) {
}
