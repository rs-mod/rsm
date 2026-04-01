package com.ricedotwho.rsm.utils.render.animation;

import lombok.Getter;

public class Animation {

    private Easing easing = Easing.LINEAR;

    private long startTime = System.currentTimeMillis();
    private long duration = 0;

    private double startValue = 0;
    private double targetValue = 1;

    @Getter
    private Double value = 0.0;

    @Getter
    private boolean finished;

    public Animation setEasing(Easing easing) {
        this.easing = easing;
        return this;
    }

    public Animation setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public Animation setTargetValue(double value) {
        if (value != this.targetValue) {
            this.targetValue = value;
            this.reset();
        }

        return this;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.startValue = this.value;
    }

    public void run() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - this.startTime;

        if (!this.finished) {
            double progress = this.getProgress(currentTime);
            double easingValue = this.easing.getFunction().apply(progress);

            this.value = this.value > this.targetValue ?
                    this.startValue - (this.startValue - this.targetValue) * easingValue :
                    this.startValue + (this.targetValue - this.startValue) * easingValue;
        }

        this.finished = deltaTime > this.duration || this.value == this.targetValue;
    }

    public double getProgress() {
        return getProgress(System.currentTimeMillis());
    }

    public double getProgress(long time) {
        return Math.clamp((double) (time - this.startTime) / this.duration, 0, 1);
    }

}