package com.ricedotwho.rsm.component.impl.notification;

import com.ricedotwho.rsm.data.StopWatch;

public class Notification {
    public final String title;
    public final String description;
    public final boolean warning;
    public final int duration;
    public final StopWatch timer;
    public boolean expired = false;
    public long slideOutStartTime = -1;

    private static final int SLIDE_OUT_DURATION = 300;

    public Notification(String title, String description, boolean warning, int duration) {
        this.title = title;
        this.description = description;
        this.warning = warning;
        this.duration = duration;
        this.timer = new StopWatch();
    }

    public boolean isExpired() {
        if (!expired && timer.getElapsedTime() >= duration) {
            expired = true;
            slideOutStartTime = System.currentTimeMillis();
        }
        return expired;
    }

    public void startSlideOut() {
        if (!expired) {
            expired = true;
            slideOutStartTime = System.currentTimeMillis();
        }
    }

    public boolean isReadyToRemove() {
        return expired && System.currentTimeMillis() - slideOutStartTime >= SLIDE_OUT_DURATION;
    }

    public float getProgress() {
        float progress = (float) timer.getElapsedTime() / duration;
        return Math.min(progress, 1.0f);
    }

    public float getSlideOutProgress() {
        if (slideOutStartTime == -1) return 0f;
        float progress = (System.currentTimeMillis() - slideOutStartTime) / (float) SLIDE_OUT_DURATION;
        return Math.min(progress, 1.0f);
    }
}
