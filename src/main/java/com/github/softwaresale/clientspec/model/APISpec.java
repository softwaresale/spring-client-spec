package com.github.softwaresale.clientspec.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines an entire API
 * @param name The name of the API
 * @param entities the entities needed to consume this API
 * @param services The services provided by this API
 */
public record APISpec(
        String name,
        List<EntitySpec> entities,
        List<ServiceSpec> services
) {
    public APISpec(String name) {
        this(name, new ArrayList<>(), new ArrayList<>());
    }

    public void addEntity(EntitySpec entitySpec) {
        entities.add(entitySpec);
    }

    public void addService(ServiceSpec serviceSpec) {
        services.add(serviceSpec);
    }
}
