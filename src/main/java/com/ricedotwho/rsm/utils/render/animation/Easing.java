package com.ricedotwho.rsm.utils.render.animation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

import static com.ricedotwho.rsm.utils.MathUtils.*;

// https://easings.net/

@AllArgsConstructor
public enum Easing {

    LINEAR(x -> x),

    IN_SINE(x -> 1 - Math.cos((x * Math.PI) / 2)),
    IN_CUBIC(x -> x * x * x),
    IN_QUINT(x -> x * x * x * x * x),
    IN_CIRC(x -> 1 - Math.sqrt(1 - Math.pow(x, 2))),

    IN_QUAD(x -> x * x),
    IN_QUART(x -> x * x * x * x),
    IN_EXPO(x -> x == 0 ? 0 : Math.pow(2, 10 * x - 10)),
    IN_BACK(x -> OUT_MAGIC_2 * x * x * x - OUT_MAGIC_1 * x * x),

    OUT_SINE(x -> Math.sin((x * Math.PI) / 2)),
    OUT_CUBIC(x -> 1 - Math.pow(1 - x, 3)),
    OUT_QUINT(x -> 1 - Math.pow(1 - x, 5)),
    OUT_CIRC(x -> Math.sqrt(1 - Math.pow(x - 1, 2))),

    OUT_QUAD(x -> 1 - (1 - x) * (1 - x)),
    OUT_QUART(x -> 1 - Math.pow(1 - x, 4)),
    OUT_EXPO(x -> x == 1 ? 1 : 1 - Math.pow(2, -10 * x)),
    OUT_BACK(x -> 1 + OUT_MAGIC_2 * Math.pow(x - 1, 3) + OUT_MAGIC_1 * Math.pow(x - 1, 2)),

    IN_OUT_SINE(x -> -(Math.cos(Math.PI * x) - 1) / 2),
    IN_OUT_CUBIC(x -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2),
    IN_OUT_QUINT(x -> x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2),
    IN_OUT_CIRC(x -> x < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2),

    IN_OUT_QUAD(x -> x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2),
    IN_OUT_QUART(x -> x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2),
    IN_OUT_EXPO(x -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2 : (2 - Math.pow(2, -20 * x + 10)) / 2),
    IN_OUT_BACK(x -> x < 0.5 ? (Math.pow(2 * x, 2) * ((OUT_MAGIC_3 + 1) * 2 * x - OUT_MAGIC_3)) / 2 : (Math.pow(2 * x - 2, 2) * ((OUT_MAGIC_3 + 1) * (x * 2 - 2) + OUT_MAGIC_3) + 2) / 2);

    @Getter
    private final Function<Double, Double> function;

}