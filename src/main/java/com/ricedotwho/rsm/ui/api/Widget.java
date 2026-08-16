package com.ricedotwho.rsm.ui.api;

import java.util.ArrayList;

/**
 * Acts like an extension to a node, allowing additional frame behavior to be added to an existing node type without overriding it
 */
public abstract class Widget extends UiElement {
    protected final Node node;

    public Widget(Node node) { this.node = node; }

    @Override
    public void dispatchFrame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!isVisible()) return;
        if (isHovered(parentX, parentY, mouseX, mouseY, scrollY)) hover();

        frame(parentX, parentY, mouseX, mouseY, scrollY);
        node.dispatchFrame(parentX, parentY, mouseX, mouseY, scrollY);
    }

    @Override
    public long getYogaNode() {
        return node.getYogaNode();
    }

    @Override
    protected ArrayList<UiElement> getChildren() {
        return node.getChildren();
    }

    @Override
    public void close() {
        node.close();
        markDebugFreed();
    }
}
