package com.ricedotwho.rsm.ui.impl.clickgui.topbar;

import com.ricedotwho.rsm.core.UniversalSettings;
import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.nodes.ImageNode;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import lombok.val;

public class SettingsGear extends ClickHandler {
    private static final Image settingsGear = NVGUtils.createImage("/assets/cameladdons/clickgui/settings_gear.png");
    public SettingsGear() {
        val node = new RectangleNode.Builder()
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .alignItems(Node.Align.CENTER)
                .justifyContent(Node.JustifyContent.CENTER)
                .height(36)
                .width(36)
                .rounding(5)
                .right(20)
                .positionType(PositionType.ABSOLUTE)
                .build();
        super(node, true, false);

        val icon = new ImageNode.Builder()
                .height(28)
                .width(28)
                .image(settingsGear)
                .build();


        node.addChild(icon);

    }


    @Override
    protected void onLeftTriggered() {
        UniversalSettings.openPage();
    }
}
