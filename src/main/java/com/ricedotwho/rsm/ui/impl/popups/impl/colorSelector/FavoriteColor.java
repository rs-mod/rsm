package com.ricedotwho.rsm.ui.impl.popups.impl.colorSelector;

import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.nodes.ImageNode;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.val;

class FavoriteColor extends ClickHandler {
    private static final Image background = NVGUtils.createImage("/assets/rsm/clickgui/favorite_color_background.png");
    private final Color color;
    private final ColorPopup popup;
    public FavoriteColor(Color color, ColorPopup popup) {


        val node = new ImageNode.Builder()
                .height(Palette.elementHeight)
                .width(Palette.elementHeight)
                .image(background)
                .padding(0f)
                .display(Display.FLEX)
                .justifyContent(JustifyContent.CENTER)
                .alignItems(Align.STRETCH)
                .build();
        val filling = new RectangleNode.Builder()
                .flexGrow(1f)
                .color(color)
                .build();
        node.addChild(filling);

        super(node, true, false);
        this.color = color;
        this.popup = popup;
    }

    @Override
    protected void onLeftTriggered() {
        popup.getTargetColor().setToColor(color);
    }
}
