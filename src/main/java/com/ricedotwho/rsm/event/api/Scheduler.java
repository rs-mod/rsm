package com.ricedotwho.rsm.event.api;

import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.FilterableEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.type.Pair;
import lombok.experimental.UtilityClass;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


@UtilityClass
@Register
public class Scheduler {
    private static final ConcurrentHashMap<Class<? extends Event>, TaskContainer<?>> scheduledTasks = new ConcurrentHashMap<>();

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
            List<Task<T>> toRequeue = new ArrayList<>();

            while (!queue.isEmpty()) {
                Task<T> task = queue.poll();

                try {
                    if (!task.trigger(event)) {
                        toRequeue.add(task);
                    }
                } catch (Exception e) {
                    RSM.getLogger().error("Error in scheduled task for {}", event.getClass().getSimpleName(), e);
                }
            }

            queue.addAll(toRequeue);

            processing = false;
            queue.addAll(pendingTasks);
            pendingTasks.clear();
        }

        private void addTask(Consumer<T> consumer, Byte priority, int delay, Class<?> filterType) {
            Task<T> task = new Task<>(consumer, priority, delay, filterType);
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
        @Nullable
        Class<?> filterType;

        private boolean trigger(T event) {

            if (filterType != null) {
                if (!(event instanceof FilterableEvent fe)) throw new RuntimeException("task has a filter when the event isn't filterable");
                if (fe.generalTypeInfo().isAssignableFrom(filterType)) throw new RuntimeException("Scheduled task filterType is not applicable to the given filter: " + filterType.getSimpleName() + " is not an instance of " + fe.generalTypeInfo().getSimpleName());

                Object filteredData = fe.getData();
                if (filteredData == null || !filterType.isAssignableFrom(filteredData.getClass())) return false;
            }

            delay--;
            if (delay < 0) {
                consumer.accept(event);
                return true;
            }
            return false;
        }

        private Task(Consumer<T> consumer, Byte priority, int delay, @Nullable Class<?> filterType) {
            this.consumer = consumer;
            this.delay = delay;
            this.priority = priority;
            this.filterType = filterType;
        }
    }

    public static <T extends Event> void triggerEvent(T event, ProfilerFiller profiler) {
        @SuppressWarnings("unchecked")
        TaskContainer<T> container = (TaskContainer<T>) scheduledTasks.get(event.getClass());
        if (container == null) return;
        profiler.push("RSM-Scheduler: " + event.getClass().getSimpleName());
        container.triggerTasks(event);
        profiler.pop();
    }

    public static <T extends Event> void schedule(Class<T> event, EventPriority priority, int delay, Consumer<T> consumer) {
        @SuppressWarnings("unchecked")
        TaskContainer<T> container = (TaskContainer<T>) scheduledTasks.computeIfAbsent(event, ignored -> new TaskContainer<>());
        container.addTask(consumer, (byte) priority.ordinal(), delay, null);
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


    /**
     * Schedules a one-shot callback that fires only when {@code event}'s {@link FilterableEvent#getData()}
     * matches {@code filterType}, after {@code delay} matching occurrences.
     *
     * @param filterType the filter class to match against {@link FilterableEvent#getData()}, null for wildcard.
     * @see #schedule(Class, EventPriority, int, Consumer)
     */
    public static <T extends Event & FilterableEvent> void scheduleFiltered(Class<T> event, EventPriority priority, int delay, @NotNull Class<?> filterType, Consumer<T> consumer) {
        @SuppressWarnings("unchecked")
        TaskContainer<T> container = (TaskContainer<T>) scheduledTasks.computeIfAbsent(event, ignored -> new TaskContainer<>());
        container.addTask(consumer, (byte) priority.ordinal(), delay, filterType);
    }

    public static <T extends Event & FilterableEvent> void scheduleFiltered(Class<T> event, int delay, @NotNull Class<?> filterType, Consumer<T> consumer) {
        scheduleFiltered(event, EventPriority.NORMAL, delay, filterType, consumer);
    }

    public static <T extends Event & FilterableEvent> void scheduleFiltered(Class<T> event, @NotNull Class<?> filterType, Consumer<T> consumer) {
        scheduleFiltered(event, EventPriority.NORMAL,  0, filterType, consumer);
    }


    public static void tick(int delay, Runnable consumer) {
        schedule(TickEvent.ClientStart.class, delay, consumer);
    }

    public static void serverTick(int delay, Runnable consumer) {
        schedule(TickEvent.Server.class, delay, consumer);
    }

    public static void tick(Runnable consumer) {
        tick( 0, consumer);
    }

    public static void serverTick(Runnable consumer) {
        serverTick( 0, consumer);
    }

    private final List<Pair<Long, Runnable>> millisecondTasks = new CopyOnWriteArrayList<>();

    @SubscribeEvent
    private void onMillisecond(TimeEvent.Millisecond event) {
        millisecondTasks.removeIf(pair -> {
            if (event.getMillis() >= pair.getFirst()) {
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
    public void scheduleMilliseconds(long delay, Runnable run) {
        millisecondTasks.add(new Pair<>(delay + System.currentTimeMillis(), run));
    }
}
