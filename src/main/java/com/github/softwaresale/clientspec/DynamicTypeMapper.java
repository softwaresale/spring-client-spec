package com.github.softwaresale.clientspec;

import com.github.softwaresale.clientspec.model.DynamicType;
import com.github.softwaresale.clientspec.model.DynamicTypeID;

import javax.lang.model.type.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DynamicTypeMapper implements TypeVisitor<DynamicType, Void> {

    private static final Map<String, DynamicTypeID> wellKnownDeclaredScalarTypes = new HashMap<>();
    private static final Map<String, DynamicTypeID> wellKnownDeclaredSequenceTypes = new HashMap<>();

    static {
        // well-known scalar types
        wellKnownDeclaredScalarTypes.put(Object.class.getSimpleName(), DynamicTypeID.ANY);
        wellKnownDeclaredScalarTypes.put(Character.class.getSimpleName(), DynamicTypeID.CHAR);
        wellKnownDeclaredScalarTypes.put(Byte.class.getSimpleName(), DynamicTypeID.CHAR);
        wellKnownDeclaredScalarTypes.put(String.class.getSimpleName(), DynamicTypeID.STRING);
        wellKnownDeclaredScalarTypes.put(Short.class.getSimpleName(), DynamicTypeID.INTEGER);
        wellKnownDeclaredScalarTypes.put(Integer.class.getSimpleName(), DynamicTypeID.INTEGER);
        wellKnownDeclaredScalarTypes.put(Long.class.getSimpleName(), DynamicTypeID.INTEGER);
        wellKnownDeclaredScalarTypes.put(Float.class.getSimpleName(), DynamicTypeID.FLOAT);
        wellKnownDeclaredScalarTypes.put(Double.class.getSimpleName(), DynamicTypeID.FLOAT);

        // well-known sequence types
        wellKnownDeclaredSequenceTypes.put(Set.class.getSimpleName(), DynamicTypeID.ARRAY);
        wellKnownDeclaredSequenceTypes.put(List.class.getSimpleName(), DynamicTypeID.ARRAY);
    }


    @Override
    public DynamicType visit(TypeMirror typeMirror, Void unused) {
        return null;
    }

    @Override
    public DynamicType visitPrimitive(PrimitiveType primitiveType, Void unused) {
        DynamicTypeID typeID = switch (primitiveType.getKind()) {
            case BOOLEAN -> DynamicTypeID.BOOLEAN;
            case BYTE, CHAR -> DynamicTypeID.CHAR;
            case SHORT, INT, LONG -> DynamicTypeID.INTEGER;

            case FLOAT, DOUBLE -> DynamicTypeID.FLOAT;
            case VOID -> DynamicTypeID.VOID;
            default -> {
                throw new IllegalArgumentException(String.format("Type %s is not a primitive type", primitiveType.getKind()));
            }
        };

        return DynamicType.primitive(typeID);
    }

    @Override
    public DynamicType visitNull(NullType nullType, Void unused) {
        return null;
    }

    @Override
    public DynamicType visitArray(ArrayType arrayType, Void unused) {
        DynamicType elementType = arrayType.getComponentType().accept(this, unused);
        return new DynamicType(DynamicTypeID.ARRAY, "", List.of(elementType));
    }

    @Override
    public DynamicType visitDeclared(DeclaredType declaredType, Void unused) {

        // if this is not generic
        if (declaredType.getTypeArguments().isEmpty()) {

            // figure if this is a well known scalar type
            DynamicTypeID wellKnownType = wellKnownDeclaredScalarTypes.get(declaredType.asElement().getSimpleName().toString());
            if (wellKnownType != null) {
                return DynamicType.primitive(wellKnownType);
            }

            return new DynamicType(DynamicTypeID.USER, declaredType.asElement().getSimpleName().toString(), List.of());
        }

        // this is generic, so figure that out
        DynamicTypeID sequenceTypeID = wellKnownDeclaredSequenceTypes.get(declaredType.asElement().getSimpleName().toString());
        if (sequenceTypeID != null) {
            // this type is a well-known sequence type (list, set, etc.). We need to get the inner type
            DynamicType elementType = declaredType.getTypeArguments().get(0).accept(this, unused);
            return new DynamicType(sequenceTypeID, "", List.of(elementType));
        }

        // otherwise, this is just another generic. Figure that out
        List<DynamicType> genericParams = declaredType.getTypeArguments().stream()
                .map(typeVar -> typeVar.accept(this, unused))
                .toList();

        return new DynamicType(DynamicTypeID.GENERIC, declaredType.asElement().getSimpleName().toString(), genericParams);
    }

    @Override
    public DynamicType visitError(ErrorType errorType, Void unused) {
        return null;
    }

    @Override
    public DynamicType visitTypeVariable(TypeVariable typeVariable, Void unused) {
        // just visit the upper bound
        return typeVariable.getUpperBound().accept(this, unused);
    }

    @Override
    public DynamicType visitWildcard(WildcardType wildcardType, Void unused) {
        // TODO this is a bad approximation... but it's good enough for now
        return DynamicType.primitive(DynamicTypeID.ANY);
    }

    @Override
    public DynamicType visitExecutable(ExecutableType executableType, Void unused) {
        return null;
    }

    @Override
    public DynamicType visitNoType(NoType noType, Void unused) {

        if (noType.getKind() == TypeKind.VOID) {
            return DynamicType.primitive(DynamicTypeID.VOID);
        }

        return null;
    }

    @Override
    public DynamicType visitUnknown(TypeMirror typeMirror, Void unused) {
        return null;
    }

    @Override
    public DynamicType visitUnion(UnionType unionType, Void unused) {
        return null;
    }

    @Override
    public DynamicType visitIntersection(IntersectionType intersectionType, Void unused) {
        return null;
    }
}
