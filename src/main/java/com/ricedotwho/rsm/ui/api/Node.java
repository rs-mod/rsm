package com.ricedotwho.rsm.ui.api;

import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;


public class Node extends UiElement {
    @Getter
    protected final long yogaNode;

    @Setter
    @Nullable
    protected Color color;

    public Color getColor() throws IllegalStateException {
        if (color == null) throw new IllegalStateException("Color is Null!");
        return color;
    }

    protected final ArrayList<UiElement> children = new ArrayList<>();

    public Node(long yogaNode, @Nullable Color color) {
        this.yogaNode = yogaNode;
        this.color = color;
    }


    @Override
    protected ArrayList<UiElement> getChildren() {
        return children;
    }


}
