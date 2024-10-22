package com.github.softwaresale.clientspec;

import com.github.softwaresale.clientspec.model.DynamicType;
import com.github.softwaresale.clientspec.model.EndpointTemplate;
import com.github.softwaresale.clientspec.model.RequestValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import java.util.AbstractMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EndpointTemplateParser {

    private static final Logger logger = LoggerFactory.getLogger(EndpointTemplateParser.class);

    /** Finds group definitions in the pattern */
    private static final Pattern groupFinder;
    private static final DynamicTypeMapper dynamicTypeMapper = new DynamicTypeMapper();

    static {
        groupFinder = Pattern.compile("\\{\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*}");
    }

    public static EndpointTemplate parse(String endpointTemplate, Element methodElement) {
        ExecutableElement executableElement = (ExecutableElement) methodElement;

        Map<String, RequestValue> pathVariables = extractPathVariables(executableElement);
        Map<String, RequestValue> queryVariables = extractQueryParameters(methodElement);

        String expandedTemplate = groupFinder.matcher(endpointTemplate).replaceAll(matchResult -> {
            String variableName = matchResult.group(1);
            return String.format("{{%s}}", variableName);
        });

        // parse the template
        return new EndpointTemplate(expandedTemplate, pathVariables, queryVariables);
    }

    private static Map<String, RequestValue> extractPathVariables(ExecutableElement methodElement) {
        return methodElement.getParameters().stream()
                .peek(param -> logger.info("got parameter {}", param.getSimpleName()))
                .filter(param -> param.getAnnotation(PathVariable.class) != null)
                .map(param -> {
                    PathVariable pathVariableAnnotation = param.getAnnotation(PathVariable.class);

                    // first, try taking the annotation name. If name is specified, take that over value
                    String pathVariableName = pathVariableAnnotation.value();
                    if (!pathVariableAnnotation.name().isEmpty()) {
                        pathVariableName = pathVariableAnnotation.name();
                    }

                    // if path variable name is not specified, then use the variable's name
                    if (pathVariableName.isEmpty()) {
                        pathVariableName = param.getSimpleName().toString();
                    }

                    // we have a variable name, we now need to extract its type
                    DynamicType dynamicType = param.asType().accept(dynamicTypeMapper, null);

                    RequestValue requestValue = new RequestValue(dynamicType, pathVariableAnnotation.required());

                    return new AbstractMap.SimpleEntry<>(pathVariableName, requestValue);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map<String, RequestValue> extractQueryParameters(Element methodElement) {
        return methodElement.getEnclosedElements().stream()
                .filter(param -> param.getKind() == ElementKind.PARAMETER)
                .filter(param -> param.getAnnotation(RequestParam.class) != null)
                .map(param -> {
                    RequestParam requestParamAnnotation = param.getAnnotation(RequestParam.class);

                    // first, try taking the annotation name. If name is specified, take that over value
                    String requestParamName = requestParamAnnotation.value();
                    if (!requestParamAnnotation.name().isEmpty()) {
                        requestParamName = requestParamAnnotation.name();
                    }

                    // if path variable name is not specified, then use the variable's name
                    if (requestParamName.isEmpty()) {
                        requestParamName = param.getSimpleName().toString();
                    }

                    // we have a variable name, we now need to extract its type
                    DynamicType dynamicType = param.asType().accept(dynamicTypeMapper, null);

                    RequestValue requestValue = new RequestValue(dynamicType, requestParamAnnotation.required());

                    return new AbstractMap.SimpleEntry<>(requestParamName, requestValue);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
