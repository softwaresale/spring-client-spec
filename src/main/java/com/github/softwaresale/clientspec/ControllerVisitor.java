package com.github.softwaresale.clientspec;

import com.github.softwaresale.clientspec.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Visits a class that is annotated with @RestController
 */
public class ControllerVisitor {

    private static final Logger logger = LoggerFactory.getLogger(ControllerVisitor.class);

    private static final DynamicTypeMapper dynamicTypeMapper = new DynamicTypeMapper();
    private static final Set<Class<? extends Annotation>> handlerAnnotations = new HashSet<>();

    static {
        handlerAnnotations.add(RequestMapping.class);
        handlerAnnotations.add(GetMapping.class);
        handlerAnnotations.add(PostMapping.class);
        handlerAnnotations.add(PutMapping.class);
        handlerAnnotations.add(DeleteMapping.class);
        handlerAnnotations.add(PatchMapping.class);
    }

    /**
     * Determine if we can accept the given annotated element
     * @param annotatedElement the element to test
     * @return True if we can process
     */
    public <T extends Element> boolean accept(T annotatedElement) {
        return annotatedElement.getKind() == ElementKind.CLASS && annotatedElement.getAnnotation(RestController.class) != null;
    }

    /**
     * Actually visit a controller class and generate a service specification
     * @param controllerClass The annotated element. Should be a class
     * @return A service spec for the given controller
     */
    public ServiceSpec visitController(Element controllerClass) {
        // quick sanity check
        if (controllerClass.getKind() != ElementKind.CLASS) {
            throw new IllegalArgumentException(String.format("%s is not a class", controllerClass));
        }

        // find a request mapping at the root
        RequestMapping classLevelRequestMapping = controllerClass.getAnnotation(RequestMapping.class);
        String rootPath;
        if (classLevelRequestMapping != null) {
            rootPath = joinHandlerPath("", classLevelRequestMapping.value());
        } else {
            rootPath = "";
        }

        // pull out all endpoints
        List<APIEndpoint> endpoints = controllerClass.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .peek(element -> logger.info("found method {}", element.getSimpleName()))
                .filter(this::methodIsHandler)
                .map(element -> this.visitHandler(element, rootPath))
                .toList();

        return new ServiceSpec(controllerClass.getSimpleName().toString(), endpoints);
    }

    private boolean methodIsHandler(Element element) {
        for (var anno : handlerAnnotations) {
            if (element.getAnnotation(anno) != null) {
                return true;
            }
        }
        return false;
    }

    private static Optional<Annotation> findRequestAnnotation(Element element) {
        for (var anno : handlerAnnotations) {
            Annotation annotation = element.getAnnotation(anno);
            if (annotation != null) {
                return Optional.of(annotation);
            }
        }

        return Optional.empty();
    }

    private APIEndpoint visitHandler(Element element, String prefixPath) {
        logger.info("vising handler {}", element.getSimpleName());

        // first, figure out our http method
        Annotation handlerAnnotation = findRequestAnnotation(element).orElseThrow();
        APIEndpointMetadata metadata = RequestMappingDecomposer.convert(handlerAnnotation).orElseThrow();

        // figure out the path
        String handlerPath = joinHandlerPath(prefixPath, metadata.endpoint());
        // TODO map this to a template and parse out handler variables

        // figure out path variables etc.
        EndpointTemplate endpointTemplate = EndpointTemplateParser.parse(handlerPath, element);

        // find the response body
        ExecutableElement executableElement = (ExecutableElement) element;

        DynamicType responseType = executableElement.getReturnType().accept(dynamicTypeMapper, null);
        RequestValue responseBody = new RequestValue(responseType, true);

        DynamicType requestBodyType = executableElement.getParameters().stream()
                .filter(param -> param.getAnnotation(RequestBody.class) != null)
                .map(param -> param.asType().accept(dynamicTypeMapper, null))
                .findFirst()
                .orElse(DynamicType.primitive(DynamicTypeID.VOID));
        RequestValue requestBody = new RequestValue(requestBodyType, false);

        return new APIEndpoint(element.getSimpleName().toString(),
                endpointTemplate.template(),
                metadata.method().name(),
                endpointTemplate.pathVariables(),
                endpointTemplate.queryVariables(),
                requestBody,
                responseBody);
    }

    private static String joinHandlerPath(String prefix, String[] components) {
        String suffix = Arrays.stream(components)
                .filter(str -> !str.isBlank())
                .collect(Collectors.joining("/"));

        if (prefix.isEmpty() && suffix.isEmpty()) {
            return "";
        }

        if (prefix.isEmpty()) {
            return suffix;
        }

        if (suffix.isEmpty()) {
            return prefix;
        }

        // if prefix has leading slash, just concat
        if (suffix.charAt(0) == '/' || prefix.charAt(prefix.length() - 1) == '/') {
            return prefix + suffix;
        }

        return String.join("/", prefix, suffix);
    }
}
