package com.ricedotwho.rsm.event.api;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {
    private static final Map<Class<? extends Event>, List<MethodData>> LISTENERS = new HashMap<>();

    public void register(Object ...objects) {
        for (final Object object : objects) {
            for (final Method method : getAllMethods(object.getClass())) {
                if (isMethodBad(method)) continue;
                register(method, object);
            }
        }
    }

    public void register(Object object, Class<? extends Event> eventClass) {
        for (final Method method : getAllMethods(object.getClass())) {
            if (isMethodBad(method, eventClass)) continue;
            register(method, object);
        }
    }

    public void unregister(Object object) {
        for (final List<MethodData> dataList : LISTENERS.values()) {
            dataList.removeIf(data -> data.getSource().equals(object));
        }
        cleanMap(true);
    }

    @SuppressWarnings("unchecked")
    private void register(Method method, Object object) {
        Class<? extends Event> eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
        final MethodData methodData = new MethodData(object, method, method.getAnnotation(SubscribeEvent.class));

        if (!methodData.getTarget().isAccessible()) {
            methodData.getTarget().setAccessible(true);
        }

        LISTENERS.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(methodData);
        sortListValue(eventClass);
    }

    public void cleanMap(boolean onlyEmptyEntries) {
        LISTENERS.entrySet().removeIf(entry -> !onlyEmptyEntries || entry.getValue().isEmpty());
    }

    private void sortListValue(Class<? extends Event> eventClass) {
        List<MethodData> sortedList = new CopyOnWriteArrayList<>();
        for (final EventPriority priority : EventPriority.values()) {
            for (final MethodData methodData : LISTENERS.get(eventClass)) {
                if (methodData.getPriority() == priority) {
                    sortedList.add(methodData);
                }
            }
        }
        LISTENERS.put(eventClass, sortedList);
    }

    private boolean isMethodBad(Method method) {
        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(SubscribeEvent.class);
    }

    private boolean isMethodBad(Method method, Class<? extends Event> eventClass) {
        return isMethodBad(method) || !method.getParameterTypes()[0].equals(eventClass);
    }

    public <T extends Event> boolean post(T event) {
        Class<?> clazz = event.getClass();

        while (clazz != null && Event.class.isAssignableFrom(clazz)) {
            List<MethodData> dataList = LISTENERS.get(clazz);

            if (dataList != null) {
                for (final MethodData data : dataList) {
                    if (event.isCancelled() && !data.isReceiveCancelled()) continue;
                    invoke(data, event);
                }
            }

            clazz = clazz.getSuperclass();
        }

        return event.isCancellable() && event.isCancelled();
    }

    private void invoke(MethodData data, Event event) {
        try {
            data.getTarget().invoke(data.getSource(), event);
        } catch (IllegalAccessException | IllegalArgumentException ignored) {
            ignored.printStackTrace();
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            cause.printStackTrace();
            ChatUtils.chat("%s(%s) in listener: %s#%s", cause.getClass().getSimpleName(), cause.getMessage(), data.getTarget().getDeclaringClass().getName(), data.getTarget().getName());
        }
    }

    private List<Method> getAllMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();

        while (clazz != null && clazz != Object.class) {
            Collections.addAll(methods, clazz.getDeclaredMethods());
            clazz = clazz.getSuperclass();
        }

        return methods;
    }

    @Getter
    private static class MethodData {
        private final Object source;
        private final Method target;
        private final EventPriority priority;
        private final boolean receiveCancelled;

        public MethodData(Object source, Method target, SubscribeEvent event) {
            this.source = source;
            this.target = target;
            this.priority = event.priority();
            this.receiveCancelled = event.receiveCancelled();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            MethodData that = (MethodData) obj;
            return priority == that.priority &&
                    Objects.equals(source, that.source) &&
                    Objects.equals(target, that.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target, priority);
        }
    }
}
