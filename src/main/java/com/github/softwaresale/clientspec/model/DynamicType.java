package com.github.softwaresale.clientspec.model;

import java.util.List;

public record DynamicType(
        DynamicTypeID typeID,
        String reference,
        List<DynamicType> nested
) {
    public static DynamicType primitive(DynamicTypeID typeID) {
        return new DynamicType(typeID, null, null);
    }
}
