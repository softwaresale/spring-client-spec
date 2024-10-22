package com.github.softwaresale.clientspec.model;

import java.util.Map;

public record EntitySpec(
        String name,
        Map<String, PropertySpec> properties
) {
}
