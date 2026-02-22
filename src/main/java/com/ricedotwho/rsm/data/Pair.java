package com.ricedotwho.rsm.data;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Pair<T, U> {
    protected T first;
    protected U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    public List<Object> toList() {
        List<Object> list = new ArrayList<>();
        list.add(this.first);
        list.add(this.second);
        return list;
    }

    @Override
    public String toString() {
        return "Pair" +
                "{" +
                "first=" + this.first +
                ",second=" + this.second +
                "}";
    }
}