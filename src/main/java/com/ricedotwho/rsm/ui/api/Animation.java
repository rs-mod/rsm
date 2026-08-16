package com.ricedotwho.rsm.ui.api;


import lombok.val;

public abstract class Animation<T extends Number & Comparable<T>> {
    private boolean animating = false;
    private long animationStartTime = 0L;
    private final long duration;

    public Animation(long duration) {
        this.duration = duration;
    }

    public boolean attemptStart() {
        if (animating) updateIsAnimating();
        if (!animating) {
            forceStart();
            return true;
        }
        return false;
    }

    public void forceStart() {
        animating = true;
        animationStartTime = System.currentTimeMillis();
    }

    public float getPercent() {
        return getPercent(false);
    }

    public float getPercent(boolean reverse) {
        updateIsAnimating();
        val t = animating ? Math.min(getTime() / ((float) duration), 1f) : 1f;
        return reverse ? 1 - t : t;
    }

    public boolean isAnimating() {
        if (animating) updateIsAnimating();
        return animating;
    }

    private void updateIsAnimating() {
        if (getTime() < duration) return;
        animating = false;
    }

    private long getTime() {
        return System.currentTimeMillis() - animationStartTime;
    }

    public T get(T start, T end) {
        return get(start, end, false);
    }

    public abstract T get(T start, T end, boolean reverse);
}
