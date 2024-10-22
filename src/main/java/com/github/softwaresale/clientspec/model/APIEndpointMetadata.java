package com.github.softwaresale.clientspec.model;

import org.springframework.http.HttpMethod;

/**
 * Used to represent metadata from a request mapping. Handles aliases for get, post, and so on
 * @param endpoint
 * @param method
 */
public record APIEndpointMetadata(
        String[] endpoint,
        HttpMethod method
) {
}
