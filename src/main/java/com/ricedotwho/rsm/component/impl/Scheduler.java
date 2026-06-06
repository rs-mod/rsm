package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.ServerTickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


public class Scheduler extends ModComponent {
    private static final HashMap<Class<? extends Event>, TaskContainer<?>> scheduledTasks = new HashMap<>();

    public Scheduler() { super("Scheduler"); }

    @SubscribeEvent
    private void onWorldChange(WorldEvent.Load event) {
        scheduledTasks.values().forEach(TaskContainer::clear);
    }

    static final private class TaskContainer<T extends Event> {
        private final Queue<Task<T>> queue = new PriorityQueue<>(Comparator.comparingInt(t -> t.priority));
        private final List<Task<T>> pendingTasks = new ArrayList<>(2);
        private boolean processing = false;

        private void clear() {
            queue.clear();
            pendingTasks.clear();
        }

        private void triggerTasks(T event) {
            processing = true;
            queue.removeIf(task -> task.trigger(event));
            processing = false;
            queue.addAll(pendingTasks);
            pendingTasks.clear();
        }

        private void addTask(Consumer<T> consumer, Byte priority, int delay) {
            Task<T> task = new Task<>(consumer, priority, delay);
            if (processing) {
                pendingTasks.add(task);
                return;
            }
            queue.add(task);
        }
    }

    private static class Task<T extends Event> {
        int delay;
        byte priority;
        Consumer<T> consumer;

        private boolean trigger(T event) {
            delay--;
            if (delay < 0) {
                consumer.accept(event);
                return true;
            }
            return false;
        }
        private Task (Consumer<T> consumer, Byte priority, int delay) {
            this.consumer = consumer;
            this.delay = delay;
            this.priority = priority;
        }
    }

    public <T extends Event> void triggerEvent(T event) {
        @SuppressWarnings("unchecked")
        TaskContainer<T> container = (TaskContainer<T>) scheduledTasks.get(event.getClass());
        if (container == null) return;
        container.triggerTasks(event);
    }

    /**
     * <b>Scheduled tasks will always fire before @SubscribeEvent</b>.
     * Scheduled tasks will trigger after {@code delay} occurrences.
     * The task is removed after it fires.
     *
     * @param event the event class to listen for
     * @param priority execution priority relative to other scheduled tasks on the same event
     * @param delay number of event occurrences to wait before firing; {@code 1} fires on the next occurrence
     * @param consumer callback invoked when the delay is reached
     * @param <T> event type
     */
    public static <T extends Event> void schedule(Class<T> event, EventPriority priority, int delay, Consumer<T> consumer) {
        @SuppressWarnings("unchecked")
        TaskContainer<T> container = (TaskContainer<T>) scheduledTasks.computeIfAbsent(event, ignored -> new TaskContainer<>());
        container.addTask(consumer, (byte) priority.ordinal(), delay);
    }

    /**
     * Schedules a one-shot callback at {@link EventPriority#NORMAL} that fires on the {@code delay}-th occurrence of {@code event}.
     *
     * @see #schedule(Class, EventPriority, int, Consumer)
     */
    public static <T extends Event> void schedule(Class<T> event, int delay, Consumer<T> consumer) {
        schedule(event, EventPriority.NORMAL, delay, consumer);
    }

    /**
     * Schedules a one-shot callback at {@link EventPriority#NORMAL} that fires on the next occurrence of {@code event}.
     *
     * @see #schedule(Class, EventPriority, int, Consumer)
     */
    public static <T extends Event> void schedule(Class<T> event, Consumer<T> consumer) {
        schedule(event, EventPriority.NORMAL, 0, consumer);
    }

    /**
     * Schedules a one-shot {@link Runnable} at {@link EventPriority#NORMAL} that fires after {@code delay} occurrences of {@code event}.
     * Use when the event instance is not needed.
     *
     * @see #schedule(Class, EventPriority, int, Consumer)
     */
    public static <T extends Event> void schedule(Class<T> event, int delay, Runnable consumer) {
        schedule(event, EventPriority.NORMAL, delay, _ -> consumer.run());
    }

    /**
     * Schedules a one-shot {@link Runnable} at {@link EventPriority#NORMAL} that fires on the next occurrence of {@code event}.
     * Use when the event instance is not needed.
     *
     * @see #schedule(Class, EventPriority, int, Consumer)
     */
    public static <T extends Event> void schedule(Class<T> event, Runnable consumer) {
        schedule(event, EventPriority.NORMAL, 0, _ -> consumer.run());
    }

    public static void tick(Runnable consumer) {
        schedule(ClientTickEvent.Start.class, consumer);
    }

    public static void serverTick(Runnable consumer) {
        schedule(ServerTickEvent.class, consumer);
    }

    private static final List<Pair<Long, Runnable>> millisecondTasks = new CopyOnWriteArrayList<>();

    @SubscribeEvent
    private void onMillisecond(TimeEvent.Millisecond event) {
        millisecondTasks.removeIf(pair -> {
            if (pair.getFirst() >= System.currentTimeMillis()) {
                pair.getSecond().run();
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * @param delay milliseconds to wait before firing
     * @param run callback to invoke when the delay has elapsed
     */
    public static void scheduleMilliseconds(long delay, Runnable run) {
        millisecondTasks.add(new Pair<>(delay + System.currentTimeMillis(), run));
    }
}
