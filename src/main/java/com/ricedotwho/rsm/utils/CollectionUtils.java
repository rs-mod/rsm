package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unused")
@UtilityClass
public class CollectionUtils {
    public void removeAllPast(ArrayList<?> list, int index) {
        list.subList(index, list.size()).clear();
    }

    public boolean equalsOneOf(Object object, Object... others) {
        for (Object obj : others) {
            if (Objects.equals(object, obj)) {
                return true;
            }
        }
        return false;
    }

    @SafeVarargs
    public <T> boolean containsAll(Collection<T> list, T... entries) {
        for (T entry : entries) {
            if (!list.contains(entry)) return false;
        }
        return true;
    }

    @SafeVarargs
    public <T> ArrayList<T> combine(List<T>... lists) {
        ArrayList<T> list = new ArrayList<>();
        for (List<T> ts : lists) {
            list.addAll(ts);
        }
        return list;
    }

    @SafeVarargs
    public <T> ArrayList<T> arrayListOf(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    public int[] createIntArray(int size, Function<Integer, Integer> function) {
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = function.apply(i);
        }
        return array;
    }

    public double[] createDoubleArray(int size, Function<Integer, Double> function) {
        double[] array = new double[size];

        for (int i = 0; i < size; i++) {
            array[i] = function.apply(i);
        }
        return array;
    }

    @SafeVarargs
    public <T> ArrayList<T> getListWith(List<T> array, T... elements) {
        ArrayList<T> newArray = new ArrayList<>(array);
        newArray.addAll(Arrays.asList(elements));
        return newArray;
    }
}
