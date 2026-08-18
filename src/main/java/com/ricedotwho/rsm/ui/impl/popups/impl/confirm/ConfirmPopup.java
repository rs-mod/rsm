package com.ricedotwho.rsm.ui.impl.popups.impl.confirm;

import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Gui;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import com.ricedotwho.rsm.ui.impl.animations.LinearAnimation;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.nodes.TextNode;
import com.ricedotwho.rsm.ui.impl.popups.Popup;
import com.ricedotwho.rsm.utils.MouseUtils;
import lombok.Getter;
import lombok.val;

import static com.ricedotwho.rsm.type.Accessor.mc;

public class ConfirmPopup extends Popup {
    @Getter
    private static final ConfirmPopup instance = new ConfirmPopup();
    public ConfirmPopup() {
        val base = new RectangleNode.Builder()
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .justifyContent(JustifyContent.CENTER)
                .alignItems(Align.CENTER)
                .color(Color.BLACK.clone().setAlpha(0.5f))
                .build();

        super(base);

        val container = new RectangleNode.Builder()
                .flexDirection(FlexDirection.COLUMN)
                .positionType(PositionType.ABSOLUTE)
                .width(376)
                .gap(24)
                .color(Palette.elementBackgroundDark)
                .outline(Palette.strokeThickness, Palette.stroke)
                .paddingLeft(24)
                .paddingRight(24)
                .paddingBottom(18)
                .paddingTop(36)
                .alignItems(Align.STRETCH)
                .build();
        base.addChild(container);


        val textContainer = new RectangleNode.Builder()
                .widthPercent(100)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.COLUMN)
                .gap(16)
                .justifyContent(JustifyContent.FLEX_START)
                .alignItems(Align.CENTER)
                .build();

        container.addChild(textContainer);

        val title = new TextNode.Builder()
                .widthPercent(100)
                .height(20)
                .text("Are you sure?")
                .color(Palette.text)
                .align(TextAlignment.CenterMiddle)
                .font(Palette.fontBold)
                .fontSize(Palette.titleFontSize)
                .build();

        description = new TextNode.Builder()
                .fontSize(Palette.fontSize)
                .widthPercent(100)
                .font(Palette.font)
                .shadow(false)
                .color(Palette.descriptions)
                .align(TextAlignment.TopMiddle)
                .wrap(true)
                .build();

        textContainer.addChild(title);
        textContainer.addChild(description);

        val buttonWrapper = new RectangleNode.Builder()
                .flexDirection(FlexDirection.ROW)
                .display(Display.FLEX)
                .gap(8f)
                .justifyContent(JustifyContent.CENTER)
                .alignItems(Align.CENTER)
                .build();

        buttonWrapper.addChild(new CancelButton());

        confirmButton = new ConfirmButton();
        buttonWrapper.addChild(confirmButton);

        container.addChild(buttonWrapper);
        setVisible(false);
        Gui.registerPopup(this);
    }
    private final LinearAnimation animation = new LinearAnimation(200);
    private final ConfirmButton confirmButton;
    private final TextNode description;



    @Override
    public void dispatchFrame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        if (!isVisible()) return;
        val animating = animation.isAnimating();
        if (animating) {
            NVGUtils.push();
            val progress = animation.get(0f, 1f, false);
            NVGUtils.globalAlpha(progress);
        } else {
            NVGUtils.globalAlpha(0.9999f); //idk why I had to do this, but it freaked out if I didn't so like
        }
        super.dispatchFrame(parentX, parentY, mouseX, mouseY, scrollY);
        if (animating) {
            NVGUtils.pop();
        }
        MouseUtils.lockCursorRequest();
    }

    @Override
    protected boolean mouseClicked(int button, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        return true;
    }

    @Override
    protected boolean mouseScrolled(float verticalAmount, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        return true;
    }

    @Override
    public void onGuiClose() {
        setVisible(false);
    }

    public static void open(Runnable action, String description, Gui gui) {
        float width = mc.getWindow().getWidth();
        float height = mc.getWindow().getHeight();

        instance.description.setText(description);
        instance.animation.attemptStart();
        instance.setLeft(-gui.originX());
        instance.setTop(-gui.originY());
        instance.setWidth(width);
        instance.setHeight(height);
        instance.confirmButton.setRunnable(action);
        instance.setVisible(true);
    }

}
