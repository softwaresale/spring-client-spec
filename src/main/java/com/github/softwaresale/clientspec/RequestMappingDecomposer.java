package com.github.softwaresale.clientspec;

import com.github.softwaresale.clientspec.model.APIEndpointMetadata;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Given some kind of request handler annotation, reduce it down to a RequestMapping annotation
 */
public class RequestMappingDecomposer {

    public static <T extends Annotation> Optional<APIEndpointMetadata> convert(T annotation) {
        if (annotation instanceof RequestMapping requestMapping) {
            return Optional.of(convertRequestMapping(requestMapping));
        } else if (annotation instanceof GetMapping requestMapping) {
            return Optional.of(convertGetMapping(requestMapping));
        } else if (annotation instanceof DeleteMapping requestMapping) {
            return Optional.of(convertDeleteMapping(requestMapping));
        } else if (annotation instanceof PutMapping requestMapping) {
            return Optional.of(convertPutMapping(requestMapping));
        } else if (annotation instanceof PatchMapping requestMapping) {
            return Optional.of(convertPatchMapping(requestMapping));
        } else if (annotation instanceof PostMapping requestMapping) {
            return Optional.of(convertPostMapping(requestMapping));
        } else {
            return Optional.empty();
        }
    }

    private static APIEndpointMetadata convertRequestMapping(RequestMapping mapping) {
        HttpMethod method = HttpMethodConverter.getHttpMethod(mapping).orElseThrow();
        return new APIEndpointMetadata(mapping.path(), method);
    }

    private static APIEndpointMetadata convertGetMapping(GetMapping mapping) {
        String[] path = mapping.value();
        if (mapping.path().length > 0) {
            path = mapping.path();
        }
        return new APIEndpointMetadata(path, HttpMethod.GET);
    }

    private static APIEndpointMetadata convertPostMapping(PostMapping mapping) {
        String[] path = mapping.value();
        if (mapping.path().length > 0) {
            path = mapping.path();
        }
        return new APIEndpointMetadata(path, HttpMethod.POST);
    }

    private static APIEndpointMetadata convertPutMapping(PutMapping mapping) {
        String[] path = mapping.value();
        if (mapping.path().length > 0) {
            path = mapping.path();
        }
        return new APIEndpointMetadata(path, HttpMethod.PUT);
    }

    private static APIEndpointMetadata convertPatchMapping(PatchMapping mapping) {
        String[] path = mapping.value();
        if (mapping.path().length > 0) {
            path = mapping.path();
        }
        return new APIEndpointMetadata(path, HttpMethod.PATCH);
    }

    private static APIEndpointMetadata convertDeleteMapping(DeleteMapping mapping) {
        String[] path = mapping.value();
        if (mapping.path().length > 0) {
            path = mapping.path();
        }
        return new APIEndpointMetadata(path, HttpMethod.DELETE);
    }
}
