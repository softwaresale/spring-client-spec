package com.github.softwaresale.clientspec.util;

public record Pair<LeftT, RightT>(LeftT left, RightT right) {
    public static <LeftT, RightT> Pair<LeftT, RightT> of(LeftT left, RightT right) {
        return new Pair<>(left, right);
    }
}
