package com.ricedotwho.rsm.ui.impl.clickgui.contents;

import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import com.ricedotwho.rsm.ui.impl.elements.ClickHandler;
import com.ricedotwho.rsm.ui.impl.nodes.ImageNode;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.popups.impl.confirm.ConfirmPopup;
import lombok.Setter;
import lombok.val;

public class RevertButton extends ClickHandler {

    private static final Image settingsGear = NVGUtils.createImage("/assets/cameladdons/clickgui/refresh.png");

    @Setter
    private static ModuleTab moduleTab = null;
    public RevertButton() {
        val node = new RectangleNode.Builder()
                .display(Node.Display.FLEX)
                .flexDirection(Node.FlexDirection.ROW)
                .alignItems(Node.Align.CENTER)
                .justifyContent(Node.JustifyContent.CENTER)
                .height(36)
                .width(36)
                .rounding(5)
                .top(11)
                .right(16)
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

    private void revert() {
        for (Setting<?> setting : moduleTab.getBareSettings()) {
            setting.resetToDefault();
        }
    }

    @Override
    protected void onLeftTriggered() {
        ConfirmPopup.open(this::revert, "Are you sure you want to reset this tab's settings? This action cannot be undone.", ClickGui.getInstance());
    }
}
