package com.ricedotwho.rsm.tests;

import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class ParameterTest {

    private record Pizza(
            int iLoveIntegers,
            float size = 2f,
            Sauce sauce = Sauce.TOMATO
    ) {}

    private void testFunction() {
        val pizza = new Pizza(2);
        val secondPizza = new Pizza(size:2f, iLoveIntegers:2);
    }

    private enum Sauce {
        TOMATO,
        CHEESE
    }
}
