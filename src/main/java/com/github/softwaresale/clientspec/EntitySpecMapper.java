package com.github.softwaresale.clientspec;

import com.github.softwaresale.clientspec.model.DynamicType;
import com.github.softwaresale.clientspec.model.EntitySpec;
import com.github.softwaresale.clientspec.model.PropertySpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EntitySpecMapper {

    private static final DynamicTypeMapper dynamicTypeMapper = new DynamicTypeMapper();

    public static EntitySpec mapDeclaredTypeElement(Element declaredTypeElement) {

        Map<String, PropertySpec> entityProperties = declaredTypeElement.getEnclosedElements().stream()
                .filter(enclosedField -> enclosedField.getKind() == ElementKind.FIELD)
                .map(EntitySpecMapper::mapProperty)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new EntitySpec(declaredTypeElement.getSimpleName().toString(), entityProperties);
    }

    private static Map.Entry<String, PropertySpec> mapProperty(Element fieldElement) {
        DynamicType propertyType = fieldElement.asType().accept(dynamicTypeMapper, null);
        PropertySpec propertySpec = new PropertySpec(propertyType, true);

        return new AbstractMap.SimpleImmutableEntry<>(fieldElement.getSimpleName().toString(), propertySpec);
    }
}
