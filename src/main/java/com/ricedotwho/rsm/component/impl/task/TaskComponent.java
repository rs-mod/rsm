package com.ricedotwho.rsm.component.impl.task;

import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.EventComponent;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.ServerTickEvent;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TaskComponent extends ModComponent {
    private static final List<ScheduledTask> tickTasks = new CopyOnWriteArrayList<>();
    private static final List<ScheduledTask> milliTasks = new CopyOnWriteArrayList<>();
    private static final List<ScheduledTask> serverTickTasks = new CopyOnWriteArrayList<>();
    @Getter
    private static long clientTicks;

    public TaskComponent() {
        super("TaskComponent");
    }

    public static void addTask(ScheduledTask task) {
        if(task == null) return;
        switch (task.getType()) {
            case TICK:
                tickTasks.add(task);
                break;
            case MILLIS:
                milliTasks.add(task);
                break;
            case SERVER_TICK:
                serverTickTasks.add(task);
                break;

        }
    }

    public static void onTick(Runnable run) {
        onTick(0, run);
    }

    public static void onTick(long delay, Runnable run) {
        addTask(new ScheduledTask(delay, clientTicks, ScheduledTask.TaskType.TICK, run));
    }

    public static void onMilli(long delay, Runnable run) {
        addTask(new ScheduledTask(delay, System.currentTimeMillis(), ScheduledTask.TaskType.MILLIS, run));
    }

    public static void onServerTick(Runnable run) {
        onServerTick(0, run);
    }

    public static void onServerTick(long delay, Runnable run) {
        addTask(new ScheduledTask(delay, EventComponent.getTotalWorldTime(), ScheduledTask.TaskType.SERVER_TICK, run));
    }

    public static void removeTask(ScheduledTask task) {
        if(task == null) return;
        switch (task.getType()) {
            case TICK:
                tickTasks.remove(task);
                break;
            case MILLIS:
                milliTasks.remove(task);
                break;
            case SERVER_TICK:
                serverTickTasks.remove(task);
                break;
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Start event) {
        clientTicks++;
        removeIf(tickTasks, clientTicks);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        removeIf(serverTickTasks, event.getTime());
    }

    @SubscribeEvent
    public void onMilli(TimeEvent.Millisecond event) {
        removeIf(milliTasks, System.currentTimeMillis());
    }

    //todo: improve
    private void removeIf(List<ScheduledTask> taskList, long time) {
        if (mc.level == null || taskList.isEmpty()) return;
        List<Runnable> actions = new ArrayList<>();
        List<ScheduledTask> toRemove = new ArrayList<>();
        for (ScheduledTask t : taskList) {
            if (t == null) {
                toRemove.add(null);
                continue;
            }
            if (t.shouldRun(time)) {
                actions.add(t.getTask());
                toRemove.add(t);
            }
        }
        taskList.removeAll(toRemove);

        for (Runnable run : actions) {
            run.run();
        }
    }
}