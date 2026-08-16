package com.ricedotwho.rsm.type;

import java.util.ArrayList;
import java.util.List;

public class UndoStack<T> {
    private List<T> list = new ArrayList<>();
    private int index = 0;

    public UndoStack() { }
    
    public T undo() {
        index = Math.max(0, index - 1);
        return list.get(index);
    }

    public T redo() {
        index = Math.min(index + 1, list.size() - 1);
        return list.get(index);
    }

    public T peek() {
        return list.get(index);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void pushState(T state) {
        if (index < list.size() - 1) list = new ArrayList<>(list.subList(0, index + 1));
        list.add(state);
        index = list.size() - 1;
    }
}
