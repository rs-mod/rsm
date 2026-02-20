package com.ricedotwho.rsm.ui.clickgui.api;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FatalityColours {

    public static Colour BACKGROUND = new Colour(28,28,28);
    public static Colour SELECTED_BACKGROUND = new Colour(35,35,35);

    public static Colour LINE = new Colour(38,38,38);

    public static Colour NAME1 = new Colour(255,255,255);
    public static Colour NAME2 = new Colour(0,0,255);
    public static Colour NAME3 = new Colour(255,120,130);

    public static Colour PANEL = new Colour(22,22,22);
    public static Colour PANEL_LINES = new Colour(20,20,20);

    public static Colour TEXT = new Colour(255,255,255);
    public static Colour UNSELECTED_TEXT = new Colour(105,105,105);
    public static Colour SELECTED_TEXT = new Colour(-1);
    public static Colour UNSAFE_TEXT = new Colour(205,130,50);

    public static Colour SELECTED = new Colour(255,80,95);

    public static Colour ICONS = TEXT;
    public static Colour UNSELECTED_ICONS = UNSELECTED_TEXT;

    public static Colour DEFAULT_AVATAR = new Colour(18,18,18);

    public static Colour GROUP_FILL = new Colour(28, 28, 28);
    public static Colour GROUP_OUTLINE = new Colour(50, 50, 50);

    public void setColours(ClickGUI instance) {
        BACKGROUND = instance.getBackground().getValue();
        SELECTED_BACKGROUND = instance.getSelectedBackground().getValue();

        LINE = instance.getLine().getValue();

        NAME1 = instance.getName1().getValue();
        NAME2 = instance.getName2().getValue();
        NAME3 = instance.getName3().getValue();

        PANEL = instance.getPanel().getValue();
        PANEL_LINES = instance.getPanelLines().getValue();

        TEXT = instance.getText().getValue();
        UNSELECTED_TEXT = instance.getUnselectedText().getValue();
        SELECTED_TEXT = instance.getSelectedText().getValue();

        SELECTED = instance.getSelected().getValue();

        GROUP_FILL = instance.getGroupFill().getValue();
        GROUP_OUTLINE = instance.getGroupOutline().getValue();
    }
}
