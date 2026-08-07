package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

@UtilityClass
public class ReflectionUtils {

    public @Nullable Object getSingleton(Class<?> clazz) throws RuntimeException {
        val fieldOption = getSingletonField(clazz);
        if (fieldOption.isEmpty()) return null;

        val field = fieldOption.get();
        field.setAccessible(true);

        try {
            return field.get(clazz);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    "Static initialization failed for " + clazz.getName()
                            + " while resolving singleton field '" + field.getName() + "'. "
                            + "This is usually caused by a static field being declared/initialized "
                            + "before another static field it depends on. Root cause below.",
                    e.getCause() != null ? e.getCause() : e
            );
        }
    }

    public Optional<Field> getSingletonField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (!inheritsClass(clazz, field.getType())) continue;
            if (!Modifier.isStatic(field.getModifiers())) continue;

            return Optional.of(field);
        }
        return Optional.empty();
    }

    public boolean inheritsClass(Class<?> parent, Class<?> queriedClass) {
        return parent.isAssignableFrom(queriedClass);
    }

    public boolean isFinal(Field field) { return Modifier.isFinal(field.getModifiers()); }

    public boolean isStatic(Field field) {
        return Modifier.isStatic(field.getModifiers());
    }

    public boolean isStatic(Method method) {
        return Modifier.isStatic(method.getModifiers());
    }
}
