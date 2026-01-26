package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.RSMGuiEditor;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColors;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import lombok.Getter;

@Getter
@ModuleInfo(aliases = {"Click GUI", "menu", "gui"}, id = "ClickGUI", category = Category.RENDER, defaultKey = Keyboard.KEY_RSHIFT, alwaysDisabled = true, hasKeybind = true)
public class ClickGUI extends Module {
    private final StringSetting commandPrefix = new StringSetting("Command Prefix", ".", null, false, false, 1);

    // THEME COLOURS
    private final GroupSetting theme = new GroupSetting("Theme");
    private final ColourSetting background = new ColourSetting("Background", new Colour(28,28,28));
    private final ColourSetting selectedBackground = new ColourSetting("Selected Background", new Colour(35,35,35));
    private final ColourSetting line = new ColourSetting("Line", new Colour(38,38,38));
    private final ColourSetting name1 = new ColourSetting("Name 1", new Colour(255,255,255));
    private final ColourSetting name2 = new ColourSetting("Name 2", new Colour(0,0,255));
    private final ColourSetting name3 = new ColourSetting("Name 3", new Colour(255,120,130));
    private final ColourSetting panel = new ColourSetting("Panel", new Colour(22,22,22));
    private final ColourSetting panelLines = new ColourSetting("Panel Lines", new Colour(20,20,20));
    private final ColourSetting text = new ColourSetting("Text", new Colour(255,255,255));
    private final ColourSetting unselectedText = new ColourSetting("Unselected Text", new Colour(105,105,105));
    private final ColourSetting selectedText = new ColourSetting("Selected Text", new Colour(255, 255, 255));
    private final ColourSetting selected = new ColourSetting("Selected ", new Colour(132, 25, 25));
    private final ColourSetting icon = new ColourSetting("Icon ", new Colour(-1));

    private final GroupSetting devGroup = new GroupSetting("Dev");
    private final BooleanSetting forceDev = new BooleanSetting("Force Dev", false);
    private final BooleanSetting truePlayerModifier = new BooleanSetting("True Modifier", true);
    private final BooleanSetting devOverride = new BooleanSetting("Override", false);
    private final BooleanSetting devInfo = new BooleanSetting("Info", false);
    private final BooleanSetting forceSkyBlock = new BooleanSetting("Force SkyBlock", false);

    private final ButtonSetting editGui = new ButtonSetting("Edit Gui" , "Edit", () -> {
        mc.thePlayer.closeScreen();
        TaskComponent.onTick(0, RSMGuiEditor::open);
    });

    public ClickGUI() {
        this.registerProperty(
                commandPrefix,
                editGui,
                theme,
                devGroup
        );
        devGroup.add(forceDev, truePlayerModifier, devOverride, devInfo, forceSkyBlock);
        theme.add(background, selectedBackground, line, name1, name2, name3, panel, panelLines, text, unselectedText, selectedText, selected, icon);
    }

    @Override
    public void onEnable() {
        if (mc.currentScreen == null) {
            FatalityColors.setColours(this);
            mc.displayGuiScreen(RSM.getInstance().getConfigGui());
        }
        toggle();
    }
}