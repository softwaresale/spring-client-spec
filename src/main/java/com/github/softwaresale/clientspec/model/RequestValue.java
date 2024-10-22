package com.github.softwaresale.clientspec.model;

public record RequestValue(
        DynamicType type,
        boolean required
) {
}
