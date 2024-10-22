package com.github.softwaresale.clientspec.model;

import java.util.ArrayList;
import java.util.List;

public record ServiceSpec(
        String name,
        List<APIEndpoint> endpoints
) {
    public ServiceSpec(String name) {
        this(name, new ArrayList<>());
    }

    public void addEndpoint(APIEndpoint apiEndpoint) {
        endpoints.add(apiEndpoint);
    }
}
