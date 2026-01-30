package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class Utils implements Accessor {
    public boolean equalsOneOf(Object object, Object... others) {
        for (Object obj : others) {
            if (Objects.equals(object, obj)) {
                return true;
            }
        }
        return false;
    }
}
