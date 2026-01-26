package com.ricedotwho.rsm.component.impl.task;

import lombok.Getter;

@Getter
public class ScheduledTask {
    private final long delay;
    private final long start;
    private final Runnable task;
    private final TaskType type;

    public ScheduledTask(long delay, long start, TaskType type, Runnable task) {
        this.delay = delay;
        this.start = start;
        this.type = type;
        this.task = task;
    }

    public boolean shouldRun(long now) {
        return now > this.start + this.delay;
    }

    @Override
    public String toString() {
        return "ScheduledTask{task=" + this.task + ",delay=" + this.delay + ",start=" + this.start + "}";
    }
    public enum TaskType {
        TICK,
        SERVER_TICK,
        MILLIS
    }
}
