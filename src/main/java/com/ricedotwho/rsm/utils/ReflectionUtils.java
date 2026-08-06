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
                throw new RuntimeException(
                        "Static initialization failed for " + clazz.getName()
                                + " while resolving singleton field '" + declaredField.getName() + "'. "
                                + "This is usually caused by a static field being declared/initialized "
                                + "before another static field it depends on. Root cause below.",
                        e.getCause() != null ? e.getCause() : e
                );
            }
        }
        return null;
    }

    public boolean inheritsClass(Class<?> parent, Class<?> queriedClass) {
        return parent.isAssignableFrom(queriedClass);
    }

    public boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }
}
