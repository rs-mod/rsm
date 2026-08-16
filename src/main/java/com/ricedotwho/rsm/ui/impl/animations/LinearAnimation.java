package com.ricedotwho.rsm.ui.impl.animations;

import com.ricedotwho.rsm.ui.api.Animation;
import lombok.val;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class LinearAnimation extends Animation<Float> {
    public LinearAnimation(long duration) {
        super(duration);
    }

    @Override
    public Float get(Float start, Float end, boolean reverse) {
        if (!isAnimating()) return reverse ? start : end;

        val startVal = reverse ? end : start;
        val endVal = reverse ? start : end;

        return clamp(
                startVal + (endVal - startVal) * getPercent(),
                min(start, end),
                max(start, end)
        );
    }
}
