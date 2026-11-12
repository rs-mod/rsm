package com.ricedotwho.rsm.ui.impl.elements;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Widget;
import com.ricedotwho.rsm.ui.impl.animations.LinearAnimation;
import com.ricedotwho.rsm.utils.mouse.CursorTypes;
import com.ricedotwho.rsm.utils.mouse.MouseUtils;
import lombok.val;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;

public class ClickHandler extends Widget {
    public ClickHandler(@NotNull Node node, boolean leftEnabled, boolean rightEnabled) {
        this.rightEnabled = rightEnabled;
        this.leftEnabled = leftEnabled;
        super(node);
    }

    protected final LinearAnimation hoverAnimation = new LinearAnimation(100);
    protected final LinearAnimation clickedAnimation = new LinearAnimation(50);

    private boolean leftClicked = false;
    private boolean rightClicked = false;
    private boolean wasHovered = false;
    private final boolean leftEnabled;
    private final boolean rightEnabled;

    protected boolean getClicked() {
        return (leftEnabled && leftClicked) || (rightEnabled && rightClicked);
    }

    @Override
    public void frame(GuiGraphicsExtractor gfx, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        val hovered = isInteractable() && isHovered(parentX, parentY, mouseX, mouseY, scrollY);
        if (hovered != wasHovered) hoverAnimation.attemptStart();
        if (hovered) MouseUtils.requestCursor(CursorTypes.POINTING_HAND);
        wasHovered = hovered;
        onRender(hovered);
    }

    protected float getClickedAnimationContribution() {
        return getClicked() || clickedAnimation.isAnimating() ? clickedAnimation.get(0f, 0.1f, !getClicked()) : 0f;
    }

    protected void onRender(boolean hovered) {}


    @Override
    public boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!isHovered(parentX, parentY, mouseX, mouseY, scrollY)) return false;
        if (leftEnabled && button == InputConstants.MOUSE_BUTTON_LEFT) {
            leftClicked = true;
            clickedAnimation.forceStart();
        }
        if (rightEnabled && button == InputConstants.MOUSE_BUTTON_RIGHT) {
            rightClicked = true;
            clickedAnimation.forceStart();
        }
        return true;
    }

    protected void onLeftTriggered() { }

    protected void onRightTriggered() { }

    @Override
    public void mouseReleased(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        val hovered = isHovered(parentX, parentY, mouseX, mouseY, scrollY);
        if (!leftClicked && !rightClicked) return;
        if (leftEnabled && button == InputConstants.MOUSE_BUTTON_LEFT) {
            leftClicked = false;
            if (hovered) onLeftTriggered();
        }
        if (rightEnabled && button == InputConstants.MOUSE_BUTTON_RIGHT) {
            rightClicked = false;
            if (hovered) onRightTriggered();
        }
    }
}
