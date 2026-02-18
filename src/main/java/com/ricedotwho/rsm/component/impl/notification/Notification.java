package com.ricedotwho.rsm.component.impl.notification;

import com.ricedotwho.rsm.data.StopWatch;
import net.minecraft.util.Mth;

public class Notification {
    public final String title;
    public final String description;
    public final boolean warning;
    public final int duration;
    public final StopWatch timer;
    public boolean slideIn = true;
    public boolean expired = false;
    public long slideStartTime;

    private static final int SLIDE_IN_DURATION = 250;
    private static final int SLIDE_OUT_DURATION = 300;

    public Notification(String title, String description, boolean warning, int duration) {
        this.title = title;
        this.description = description;
        this.warning = warning;
        this.duration = duration;
        this.timer = new StopWatch();
        slideStartTime = System.currentTimeMillis();
    }

    public void update() {
        if (!expired && timer.getElapsedTime() >= duration) {
            expired = true;
            slideStartTime = System.currentTimeMillis();
        } else if (slideIn && timer.getElapsedTime() > SLIDE_IN_DURATION) {
            slideIn = false;
            slideStartTime = -1;
        }
    }

    public boolean isReadyToRemove() {
        return expired && System.currentTimeMillis() - slideStartTime >= SLIDE_OUT_DURATION;
    }

    public float getProgress() {
        float progress = (float) timer.getElapsedTime() / duration;
        return Math.min(progress, 1.0f);
    }

    public float getSlideProgress() {
        if (slideStartTime == -1) return 0f;
        float progress = (System.currentTimeMillis() - slideStartTime) / (float) SLIDE_OUT_DURATION;
        if (slideIn) progress = 1F - progress;
        return Mth.clamp(progress, 0F, 1F);
    }
}
