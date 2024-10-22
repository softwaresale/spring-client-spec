package com.github.softwaresale.clientspec;

import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpMethodConverter {

    private static final Map<RequestMethod, HttpMethod> mapping = new HashMap<>();

    static {
        mapping.put(RequestMethod.GET, HttpMethod.GET);
        mapping.put(RequestMethod.POST, HttpMethod.POST);
        mapping.put(RequestMethod.PUT, HttpMethod.PUT);
        mapping.put(RequestMethod.DELETE, HttpMethod.DELETE);
        mapping.put(RequestMethod.HEAD, HttpMethod.HEAD);
        mapping.put(RequestMethod.OPTIONS, HttpMethod.OPTIONS);
        mapping.put(RequestMethod.TRACE, HttpMethod.TRACE);
        mapping.put(RequestMethod.PATCH, HttpMethod.PATCH);
    }

    /**
     * Given a request mapping, get the corresponding HTTP method that can consume it. If multiple are specified,
     * only the first is taken for now.
     * @param requestMapping A request mapping annotation
     * @return An optional HTTP method. If no HTTP Methods are specified, its' empty
     */
    public static Optional<HttpMethod> getHttpMethod(RequestMapping requestMapping) {
        return Arrays.stream(requestMapping.method())
                .findFirst()
                .flatMap(requestMethod -> Optional.ofNullable(mapping.get(requestMethod)));
    }
}
