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

class AddFavoriteColorButton extends ClickHandler {
    private static final Image addImage = NVGUtils.createImage("/assets/cameladdons/clickgui/add.png");

    private final ColorPopup popup;
    private final Node overlay;

    public AddFavoriteColorButton(ColorPopup popup) {
        val node = new RectangleNode.Builder()
                .width(Palette.elementHeight)
                .height(Palette.elementHeight)
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .alignItems(Align.CENTER)
                .justifyContent(JustifyContent.CENTER)
                .color(Palette.elementBackgroundLight)
                .build();

        super(node, true, false);
        val image = new ImageNode.Builder()
                .image(addImage)
                .height(16)
                .width(16)
                .build();
        node.addChild(image);

        overlay = new RectangleNode.Builder()
                .width(Palette.elementHeight)
                .height(Palette.elementHeight)
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
        popup.addFavoriteColor(popup.getTargetColor().clone());
    }
}
