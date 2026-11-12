package com.ricedotwho.rsm.render.render2d;

import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3x2f;

import java.util.ArrayDeque;
import java.util.Deque;

@UtilityClass
public class RenderState {
    private static final Deque<State> stack = new ArrayDeque<>();
    private static Float globalAlpha = Float.NaN;
    private static TextAlignment alignment = TextAlignment.CenterLeft;
    private static Matrix3x2f cachedPose = null;

    public static Matrix3x2f snapshotPose(Matrix3x2f current) {
        var cached = cachedPose;
        if (cached != null && cached.equals(current, 0f)) return cached;
        cachedPose = new Matrix3x2f(current);
        return cachedPose;
    }

    public static void globalAlpha(Float alpha) {
        globalAlpha = alpha;
    }

    public static void align(TextAlignment textAlignment) {
        alignment = textAlignment;
    }

    public static TextAlignment alignment() {
        return alignment;
    }

    static int argb(Color colour) {
        if (!globalAlpha.isNaN()) {
            return colour.getARGBWithAlpha(Math.clamp(globalAlpha + colour.getAlpha(), 0f, 1f));
        } else {
            return colour.getARGB();
        }
    }

    @ApiStatus.Internal
    public static void push() {
        stack.push(new State(globalAlpha, alignment));
    }

    @ApiStatus.Internal
    public static void pop() {
        if (stack.isEmpty())
            throw new IllegalStateException("RenderState stack underflow");
        stack.pop().assign();
    }

    private record State(Float alpha, TextAlignment textAlignment) {
        private void assign() {
            globalAlpha = this.alpha;
            alignment = this.textAlignment;
        }
    }
}
