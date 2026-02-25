package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.RSMGuiEditor;
import com.ricedotwho.rsm.ui.clickgui.api.FatalityColours;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Getter
@ModuleInfo(aliases = {"Click GUI", "menu", "gui"}, id = "ClickGUI", category = Category.RENDER, defaultKey = GLFW.GLFW_KEY_RIGHT, alwaysDisabled = true, hasKeybind = true)
public class ClickGUI extends Module {
    private final StringSetting commandPrefix = new StringSetting("Command Prefix", "`", null, false, false, 1);
    private final ModeSetting toggleClickType = new ModeSetting("Toggle Type", "Left", List.of("Left", "Right"));

    // Theme Colours
    private final DefaultGroupSetting theme = new DefaultGroupSetting("Theme", this);
    private final ColourSetting background = new ColourSetting("Background", new Colour(28,28,28));
    private final ColourSetting selectedBackground = new ColourSetting("Selected Background", new Colour(35,35,35));
    private final ColourSetting line = new ColourSetting("Line", new Colour(38,38,38));
    private final ColourSetting name1 = new ColourSetting("Name 1", new Colour(255,255,255));
    private final ColourSetting name2 = new ColourSetting("Name 2", new Colour(0,0,255));
    private final ColourSetting name3 = new ColourSetting("Name 3", new Colour(255,120,130));
    private final ColourSetting highlight = new ColourSetting("Text Highlight", new Colour(52, 127, 207, 50));
    private final ColourSetting pipe = new ColourSetting("Text Pipe", new Colour(255, 255, 255));
    private final ColourSetting panel = new ColourSetting("Panel", new Colour(22,22,22));
    private final ColourSetting panelLines = new ColourSetting("Panel Lines", new Colour(20,20,20));
    private final ColourSetting text = new ColourSetting("Text", new Colour(255,255,255));
    private final ColourSetting unselectedText = new ColourSetting("Unselected Text", new Colour(105,105,105));
    private final ColourSetting selectedText = new ColourSetting("Selected Text", new Colour(255, 255, 255));
    private final ColourSetting selected = new ColourSetting("Selected ", new Colour(255,80,95));
    private final ColourSetting groupFill = new ColourSetting("Group Fill", new Colour(28, 28, 28));
    private final ColourSetting groupOutline = new ColourSetting("Group Outline ", new Colour(50, 50, 50));
    private final ColourSetting scrollBar = new ColourSetting("Scroll Bar", new Colour(67, 67, 67));
    private final ColourSetting enabledColour = new ColourSetting("Enabled Colour", new Colour(255,255,255, 13));
    private final ColourSetting enabledText = new ColourSetting("Enabled Text", new Colour(230, 207, 209));

    private final DefaultGroupSetting devGroup = new DefaultGroupSetting("Dev", this);
    private final BooleanSetting forceDev = new BooleanSetting("Force Dev", false);
    private final BooleanSetting truePlayerModifier = new BooleanSetting("True Modifier", true);
    private final BooleanSetting devOverride = new BooleanSetting("Override", false);
    private final BooleanSetting devInfo = new BooleanSetting("Info", false);
    private final BooleanSetting forceSkyBlock = new BooleanSetting("Force SkyBlock", false);

    private final ButtonSetting editGui = new ButtonSetting("Edit Gui" , "Edit", () -> {
        assert mc.player != null;
        mc.player.closeContainer();
        TaskComponent.onTick(0, RSMGuiEditor::open);
    });

    public ClickGUI() {
        this.registerProperty(
                commandPrefix,
                toggleClickType,
                editGui,
                theme,
                devGroup
        );
        devGroup.add(forceDev, truePlayerModifier, devOverride, devInfo, forceSkyBlock);
        theme.add(background, selectedBackground, line, name1, name2, name3, highlight, pipe, panel, panelLines, text, unselectedText, selectedText, selected, groupFill, groupOutline, scrollBar, enabledColour, enabledText);
    }

    @Override
    public void onEnable() {
        if (mc.screen == null) {
            FatalityColours.setColours(this);
            mc.setScreen(RSM.getInstance().getConfigGui());
        }
        toggle();
    }
}