package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@UtilityClass
public class ReflectionUtils {

    public Object getSingleton(Class<?> clazz) throws RuntimeException {

        for (Field declaredField : clazz.getDeclaredFields()) {
            if (!inheritsClass(clazz, declaredField.getType())) continue;
            if (!Modifier.isStatic(declaredField.getModifiers())) continue;
            declaredField.setAccessible(true);
            try {
                return declaredField.get(clazz);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public Boolean inheritsClass(Class<?> parent, Class<?> queriedClass) {
        return parent.isAssignableFrom(queriedClass);
    }

    public Boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }
}
