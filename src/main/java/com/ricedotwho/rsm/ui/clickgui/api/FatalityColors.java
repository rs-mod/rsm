package com.ricedotwho.rsm.ui.clickgui.api;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import lombok.experimental.UtilityClass;

import java.awt.*;

@UtilityClass
public class FatalityColors {

    public Colour BACKGROUND = new Colour(28,28,28);
    public Colour HEADER_BACKGROUND = new Colour(24,24,24);
    public Colour SELECTED_BACKGROUND = new Colour(35,35,35);

    public Colour LINE = new Colour(38,38,38);

    public Colour NAME1 = new Colour(255,255,255);
    public Colour NAME2 = new Colour(0,0,255);
    public Colour NAME3 = new Colour(255,120,130);

    public Colour PANEL = new Colour(22,22,22);
    public Colour PANEL_LINES = new Colour(20,20,20);

    public Colour TEXT = new Colour(255,255,255);
    public Colour UNSELECTED_TEXT = new Colour(105,105,105);
    public Colour SELECTED_TEXT = new Colour(-1);

    public Colour SELECTED = new Colour(132, 25, 25);

    public Colour ICON = new Colour(-1);

    public void setColours(ClickGUI instance) {
        BACKGROUND = instance.getBackground().getValue();
        SELECTED_BACKGROUND = instance.getSelectedBackground().getValue();

        LINE = instance.getLine().getValue();

        NAME1 = instance.getName1().getValue();
        NAME2 = instance.getName2().getValue();
        NAME3 = instance.getName3().getValue();

        PANEL = instance.getPanel().getValue();
        PANEL_LINES = instance.getPanel().getValue();

        TEXT = instance.getText().getValue();
        UNSELECTED_TEXT = instance.getUnselectedText().getValue();
        SELECTED_TEXT = instance.getSelectedText().getValue();

        SELECTED = instance.getSelected().getValue();

        ICON = instance.getIcon().getValue();
    }
}
