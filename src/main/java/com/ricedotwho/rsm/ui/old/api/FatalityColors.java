package com.ricedotwho.rsm.ui.old.api;

import com.ricedotwho.rsm.core.UniversalSettings;
import com.ricedotwho.rsm.type.Color;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FatalityColors {

    public static Color BACKGROUND = Color.fromRGB(28,28,28);
    public static Color SELECTED_BACKGROUND = Color.fromRGB(35,35,35);

    public static Color LINE = Color.fromRGB(38,38,38);

    public static Color NAME1 = Color.fromRGB(255,255,255);
    public static Color NAME2 = Color.fromRGB(0,0,255);
    public static Color NAME3 = Color.fromRGB(255,120,130);

    public static Color PANEL = Color.fromRGB(22,22,22);
    public static Color PANEL_LINES = Color.fromRGB(20,20,20);

    public static Color HIGHLIGHT = Color.fromRGB(52, 127, 207, 50);

    public static Color PIPE = Color.fromRGB(255,255,255);

    public static Color TEXT = Color.fromRGB(255,255,255);
    public static Color UNSELECTED_TEXT = Color.fromRGB(105,105,105);
    public static Color SELECTED_TEXT = Color.fromRGB(255, 255, 255);
    public static Color UNSAFE_TEXT = Color.fromRGB(205,130,50);

    public static Color SELECTED = Color.fromRGB(255,80,95);

    public static Color GROUP_FILL = Color.fromRGB(28, 28, 28);
    public static Color GROUP_OUTLINE = Color.fromRGB(50, 50, 50);

    public static Color SCROLL_BAR = Color.fromRGB(67, 67, 67);
    public static Color ENABLED = Color.fromRGB(255,255,255, 13);
    public static Color ENABLED_TEXT = Color.fromRGB(230, 207, 209);

    public static Color WRITING_TEXT = Color.fromRGB(60, 60, 60);
    public static Color HOVERING_TEXT = Color.fromRGB(50, 50, 50);
    public static Color INPUT_TEXT = Color.fromRGB(40, 40, 40);

    public static Color SEARCH_FILL = Color.fromRGB(50, 50, 50);
    public static Color SEARCH_OUTLINE = Color.fromRGB(50, 50, 50);

    public void updateColors() {
        BACKGROUND = UniversalSettings.getOldBackground().getValue();
        SELECTED_BACKGROUND = UniversalSettings.getOldSelectedBackground().getValue();
        LINE = UniversalSettings.getOldLine().getValue();
        NAME1 = UniversalSettings.getOldName1().getValue();
        NAME2 = UniversalSettings.getOldName2().getValue();
        NAME3 = UniversalSettings.getOldName3().getValue();
        HIGHLIGHT = UniversalSettings.getOldHighlight().getValue();
        PIPE = UniversalSettings.getOldPipe().getValue();
        PANEL = UniversalSettings.getOldPanel().getValue();
        PANEL_LINES = UniversalSettings.getOldPanelLines().getValue();
        TEXT = UniversalSettings.getOldText().getValue();
        UNSELECTED_TEXT = UniversalSettings.getOldUnselectedText().getValue();
        SELECTED_TEXT = UniversalSettings.getOldSelectedText().getValue();
        SELECTED = UniversalSettings.getOldSelected().getValue();
        GROUP_FILL = UniversalSettings.getOldGroupFill().getValue();
        GROUP_OUTLINE = UniversalSettings.getOldGroupOutline().getValue();
        SCROLL_BAR = UniversalSettings.getOldScrollBar().getValue();
        ENABLED = UniversalSettings.getOldEnabledColor().getValue();
        ENABLED_TEXT = UniversalSettings.getOldEnabledText().getValue();
    }
}
