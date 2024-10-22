package com.github.softwaresale.clientspec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Records a map of all entities involved in this API
 */
public class EntityMap {

    private static final Logger logger = LoggerFactory.getLogger(EntityMap.class);

    private final Set<Element> entityElements;

    public EntityMap() {
        this.entityElements = new HashSet<>();
    }

    /**
     * Try to register a type. Determine if the given type is a declared type. If it is, then register. Otherwise, it's
     * not worth looking at
     * @param typeMirror The type to try to register
     */
    public void tryRegisterType(TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.DECLARED && !DynamicTypeMapper.isPrimitiveType(typeMirror)) {
            DeclaredType declaredTypeElement = (DeclaredType) typeMirror;
            this.registerEntity(declaredTypeElement.asElement());
        }
    }

    /**
     * Registers an DeclaredElement entity
     * @param element DeclaredType element to register
     */
    private void registerEntity(Element element) {
        // this entity is not a record, then just keep moving
        if (!element.getKind().isClass() || DynamicTypeMapper.isPrimitiveType(element.asType())) {
            return;
        }

        // visit this element, and if it was already present, keep going
        if (!this.entityElements.add(element)) {
            return;
        }

        // if this element is generic, register any generic types

        logger.info("registering type {}...", element.getSimpleName());

        // find all directly dependent types
        Collection<TypeMirror> enclosedNonPrimitiveTypes = element.getEnclosedElements().stream()
                .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.FIELD)
                .map(Element::asType)
                .filter(elementType -> !DynamicTypeMapper.isPrimitiveType(elementType))
                .collect(Collectors.toSet());

        logger.info("got dependent types {} from {}", enclosedNonPrimitiveTypes, element);

        // for enclosed fields, recursively register all elements that need resolving
        for (TypeMirror typeMirror : enclosedNonPrimitiveTypes) {
            this.tryRegisterType(typeMirror);
        }

        // find all types that are included in generic types
        Collection<TypeMirror> dependentGenericTypes = element.getEnclosedElements().stream()
                .filter(enclosedElement -> enclosedElement.getKind() == ElementKind.FIELD)
                .map(Element::asType)
                // get all generic types
                .flatMap(fieldType -> {
                    if (fieldType.getKind() == TypeKind.DECLARED) {
                        DeclaredType declaredTypeElement = (DeclaredType) fieldType;
                        return declaredTypeElement.getTypeArguments().stream();
                    }

                    return Stream.empty();
                })
                .collect(Collectors.toSet());
        for (TypeMirror typeMirror : dependentGenericTypes) {
            this.tryRegisterType(typeMirror);
        }
    }

    /**
     * Get the uniquely registered and dependent types
     * @return Collection of elements needed to consume this API
     */
    public Collection<Element> getRegisteredElements() {
        return this.entityElements;
    }
}
