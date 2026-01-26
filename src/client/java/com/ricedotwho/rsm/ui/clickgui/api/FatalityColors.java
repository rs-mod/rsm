package com.ricedotwho.rsm.ui.clickgui.api;

import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import lombok.experimental.UtilityClass;

import java.awt.*;

@UtilityClass
public class FatalityColors {

    public Color BACKGROUND = new Color(28,28,28);
    public Color HEADER_BACKGROUND = new Color(24,24,24);
    public Color SELECTED_BACKGROUND = new Color(35,35,35);

    public Color LINE = new Color(38,38,38);

    public Color NAME1 = new Color(255,255,255);
    public Color NAME2 = new Color(0,0,255);
    public Color NAME3 = new Color(255,120,130);

    public Color PANEL = new Color(22,22,22);
    public Color PANEL_LINES = new Color(20,20,20);

    public Color TEXT = new Color(255,255,255);
    public Color UNSELECTED_TEXT = new Color(105,105,105);
    public Color SELECTED_TEXT = new Color(-1);

    public Color SELECTED = new Color(132, 25, 25);

    public Color ICON = new Color(-1);

    public void setColours(ClickGUI instance) {
        BACKGROUND = instance.getBackground().getValue().toJavaColor();
        SELECTED_BACKGROUND = instance.getSelectedBackground().getValue().toJavaColor();

        LINE = instance.getLine().getValue().toJavaColor();

        NAME1 = instance.getName1().getValue().toJavaColor();
        NAME2 = instance.getName2().getValue().toJavaColor();
        NAME3 = instance.getName3().getValue().toJavaColor();

        PANEL = instance.getPanel().getValue().toJavaColor();
        PANEL_LINES = instance.getPanel().getValue().toJavaColor();

        TEXT = instance.getText().getValue().toJavaColor();
        UNSELECTED_TEXT = instance.getUnselectedText().getValue().toJavaColor();
        SELECTED_TEXT = instance.getSelectedText().getValue().toJavaColor();

        SELECTED = instance.getSelected().getValue().toJavaColor();

        ICON = instance.getIcon().getValue().toJavaColor();
    }
}
