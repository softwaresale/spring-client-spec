package com.github.softwaresale.clientspec.model;

import java.util.Map;

public record EndpointTemplate(
        String template,
        Map<String, RequestValue> pathVariables,
        Map<String, RequestValue> queryVariables
) {
}
