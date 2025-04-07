package com.github.softwaresale.clientspec.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines an entire API
 * @param name The name of the API
 * @param entities the entities needed to consume this API
 * @param services The services provided by this API
 */
public record APISpec(
        String name,
        List<EntitySpec> entities,
        List<ServiceSpec> services,
        Map<String, Object> config
) {
    public APISpec(String name) {
        this(name, new ArrayList<>(), new ArrayList<>(), new HashMap<>());
    }

    public void addEntity(EntitySpec entitySpec) {
        entities.add(entitySpec);
    }

    public void addService(ServiceSpec serviceSpec) {
        services.add(serviceSpec);
    }

    public void registerConfigProperty(String key, Object value) {
        config.put(key, value);
    }
}
