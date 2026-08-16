package com.ricedotwho.rsm.ui.impl.popups.impl.colorSelector;

import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.nodes.ImageNode;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.val;

class RevertColorButton extends ClickHandler {
    private static final Image revertColorImage = NVGUtils.createImage("/assets/cameladdons/clickgui/refresh.png");
    private final ColorPopup popup;
    private final Node overlay;
    public RevertColorButton(ColorPopup popup) {
        val node = new RectangleNode.Builder()
                .width(Palette.largeElementHeight + Palette.elementInteriorPadding)
                .height(Palette.largeElementHeight + Palette.elementInteriorPadding)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .alignItems(Align.CENTER)
                .justifyContent(JustifyContent.CENTER)
                .color(Palette.elementBackgroundLight)
                .build();

        super(node, true, false);
        val image = new ImageNode.Builder()
                .image(revertColorImage)
                .height(Palette.elementHeight)
                .width(Palette.elementHeight)
                .build();
        node.addChild(image);
        overlay = new RectangleNode.Builder()
                .width(Palette.largeElementHeight + Palette.elementInteriorPadding)
                .height(Palette.largeElementHeight + Palette.elementInteriorPadding)
                .positionType(PositionType.ABSOLUTE)
                .left(0)
                .top(0)
                .color(Color.BLACK.clone())
                .build();

        node.addChild(overlay);
        this.popup = popup;
    }

    protected void onRender(boolean hovered) {
        overlay.getColor().setAlpha(getClickedAnimationContribution());
    }

    @Override
    protected void onLeftTriggered() {
        popup.getTargetColor().setToColor(popup.colorDifference.getPrevious().getColor());
    }
}
