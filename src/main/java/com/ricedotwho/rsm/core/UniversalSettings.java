package com.ricedotwho.rsm.core;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.module.api.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.module.api.settings.impl.*;
import com.ricedotwho.rsm.render.render2d.Font;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Keybind;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import com.ricedotwho.rsm.ui.impl.clickgui.contents.ModuleTab;
import com.ricedotwho.rsm.ui.old.RSMGuiEditor;
import com.ricedotwho.rsm.ui.old.api.FatalityColors;
import com.ricedotwho.rsm.utils.FileUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.val;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.ricedotwho.rsm.type.Accessor.mc;

@SuppressWarnings("unused")
@UtilityClass
public class UniversalSettings {
    private final DefaultGroupSetting general = new DefaultGroupSetting("General", null);
    @Getter private final StringSetting commandPrefix = new StringSetting("Command Prefix", ".", null, false, false, 1);
    @Getter private final ModeSetting toggleContainerInput = new ModeSetting("Toggle Type", "Right", List.of("Left", "Right"));
    @Getter private final BooleanSetting openAnimation = new BooleanSetting("Open Animation", true);
    @Getter private final BooleanSetting interpolateCamera = new BooleanSetting("Interpolate Camera", true);
    @Getter private final BooleanSetting capes = new BooleanSetting("Show capes", true);
    private final KeybindSetting openGui = new KeybindSetting("Open GUI", new Keybind(InputConstants.KEY_RALT, false, () -> {
        assert mc.player != null;
        mc.player.closeContainer();
        Scheduler.schedule(TickEvent.ClientStart.class, RSM.getInstance().getClickGui()::open);
        return false;
    }));
    private final ButtonSetting editGui = new ButtonSetting("Edit Gui" , "Edit", () -> {
        assert mc.player != null;
        mc.player.closeContainer();
        Scheduler.schedule(TickEvent.ClientStart.class, RSMGuiEditor::open);
    });

    private final DefaultGroupSetting guiColors = new DefaultGroupSetting("Theme", null);

    private final ColorSetting backdrop = new ColorSetting("Backdrop", Color.fromHex(0x131313));
    private final ColorSetting foreground = new ColorSetting("Foreground", Color.fromHex(0x1A1A1A));
    private final ColorSetting stroke = new ColorSetting("Outline", Color.fromHex(0x303030));
    private final ColorSetting text = new ColorSetting("Text", Color.fromHex(0xFFFFFF));
    private final ColorSetting elementHighlight = new ColorSetting("Element Accent", Color.fromHex(0xFF5263));
    private final ColorSetting elementBackgroundLight = new ColorSetting("Element Background Light", Color.fromHex(0x282828));

    // Theme Colors
    @Getter private final DefaultGroupSetting oldThemeGroup = new DefaultGroupSetting("Old Theme", null);
    @Getter private final ColorSetting oldBackground = new ColorSetting("Background", Color.fromRGB(28,28,28));
    @Getter private final ColorSetting oldSelectedBackground = new ColorSetting("Selected Background", Color.fromRGB(35,35,35));
    @Getter private final ColorSetting oldLine = new ColorSetting("Line", Color.fromRGB(38,38,38));
    @Getter private final ColorSetting oldName1 = new ColorSetting("Name 1", Color.fromRGB(255,255,255));
    @Getter private final ColorSetting oldName2 = new ColorSetting("Name 2", Color.fromRGB(0,0,255));
    @Getter private final ColorSetting oldName3 = new ColorSetting("Name 3", Color.fromRGB(255,120,130));
    @Getter private final ColorSetting oldHighlight = new ColorSetting("Text Highlight", Color.fromRGB(52, 127, 207, 0.2f));
    @Getter private final ColorSetting oldPipe = new ColorSetting("Text Pipe", Color.fromRGB(255, 255, 255));
    @Getter private final ColorSetting oldPanel = new ColorSetting("Panel", Color.fromRGB(22,22,22));
    @Getter private final ColorSetting oldPanelLines = new ColorSetting("Panel Lines", Color.fromRGB(20,20,20));
    @Getter private final ColorSetting oldText = new ColorSetting("Text", Color.fromRGB(255,255,255));
    @Getter private final ColorSetting oldUnselectedText = new ColorSetting("Unselected Text", Color.fromRGB(105,105,105));
    @Getter private final ColorSetting oldSelectedText = new ColorSetting("Selected Text", Color.fromRGB(255, 255, 255));
    @Getter private final ColorSetting oldSelected = new ColorSetting("Selected ", Color.fromRGB(255,80,95));
    @Getter private final ColorSetting oldGroupFill = new ColorSetting("Group Fill", Color.fromRGB(28, 28, 28));
    @Getter private final ColorSetting oldGroupOutline = new ColorSetting("Group Outline ", Color.fromRGB(50, 50, 50));
    @Getter private final ColorSetting oldScrollBar = new ColorSetting("Scroll Bar", Color.fromRGB(67, 67, 67));
    @Getter private final ColorSetting oldEnabledColor = new ColorSetting("Enabled Color", Color.fromRGB(255,255,255, 0.05f));
    @Getter private final ColorSetting oldEnabledText = new ColorSetting("Enabled Text", Color.fromRGB(230, 207, 209));
    @Getter private final ModeSetting oldFontMode = new ModeSetting("Selected Font", "JoseFin", List.of("JoseFin", "JoseFin Bold", "Product Sans", "SF Pro Rounded", "Nunito", "Roboto Medium"));

    @Getter private final DefaultGroupSetting devGroup = new DefaultGroupSetting("Dev", null);
    @Getter private final BooleanSetting forceDev = new BooleanSetting("Force Dev", false);
    @Getter private final BooleanSetting truePlayerModifier = new BooleanSetting("True Modifier", true);
    @Getter private final BooleanSetting devOverride = new BooleanSetting("Override", false);
    @Getter private final BooleanSetting devInfo = new BooleanSetting("Info", false);
    @Getter private final BooleanSetting forceSkyBlock = new BooleanSetting("Force SkyBlock", false);
    @Getter private final BooleanSetting logErrors = new BooleanSetting("Send listener errors in chat", false);

    public Font getOldFont() {
        return NVGUtils.getFont(oldFontMode.getValue());
    }

    @Init
    private void init() {
        Scheduler.schedule(TickEvent.Start.class, 2, UniversalSettings::onFirstTick);
    }

    //I need to do this on the first tick to initialize the Palette when the thread has GL capabilities
    private void onFirstTick() {
        try {
            general.add(commandPrefix, toggleContainerInput, openAnimation, interpolateCamera, capes, openGui, editGui);
            openGui.getValue().register();

            backdrop.setValue(Palette.backdrop);
            foreground.setValue(Palette.foreground);
            stroke.setValue(Palette.stroke);
            text.setValue(Palette.text);
            elementHighlight.setValue(Palette.elementHighlight);
            elementBackgroundLight.setValue(Palette.elementBackgroundLight);

            guiColors.add(backdrop, foreground, stroke, text, elementHighlight, elementBackgroundLight);

            devGroup.add(forceDev, truePlayerModifier, devOverride, devInfo, forceSkyBlock, logErrors);
            oldThemeGroup.add(oldBackground, oldSelectedBackground, oldLine, oldName1, oldName2, oldName3, oldHighlight, oldPipe, oldPanel, oldPanelLines, oldText, oldUnselectedText, oldSelectedText, oldSelected, oldGroupFill, oldGroupOutline, oldScrollBar, oldEnabledColor, oldEnabledText);

            addGroupSetting(general, "general");
            addGroupSetting(guiColors, "gui_colors");
            addGroupSetting(oldThemeGroup, "old_theme");
            addGroupSetting(devGroup, "dev_settings");

            FatalityColors.updateColors();
            loadFavoriteColors();
            ClientLifecycleEvents.CLIENT_STOPPING.register((_) -> save());
        } catch (Exception e) {
            RSM.getLogger().error(e);
        }
        EventBus.unregister(UniversalSettings.class);
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private record DefaultGroupSettingWrapper(DefaultGroupSetting group, String id) {}
    @Setter
    @Getter
    private ClickGui clickGui;

    private final ArrayList<ModuleTab> tabs = new ArrayList<>();
    private ModuleTab selectedTab = null;
    @Getter
    private final ArrayList<Color> favoriteColors = new ArrayList<>();
    private final ArrayList<DefaultGroupSettingWrapper> groupSettings = new ArrayList<>();


    public void close() {
        for (ModuleTab tab : tabs) {
            tab.close();
        }
    }

    public void loadFavoriteColors() {
        val readFile = FileUtils.getSaveFileInCategory("universal", "favoritecolors.json");
        if (!readFile.exists()) return;
        JsonArray colors;

        try {
            colors = JsonParser.parseString(org.apache.commons.io.FileUtils.readFileToString(readFile, StandardCharsets.UTF_8)).getAsJsonArray();

            for (JsonElement jsonElement : colors) {
                try {
                    val hex = jsonElement.getAsString();
                    Color.parseHex(hex, true).ifPresent(argb -> {
                        val color = Color.WHITE.clone(); // I can't use the function in Palette because that tries to initialize fonts
                        color.setToColor(argb);
                        favoriteColors.add(color);
                    });
                } catch (Exception _) {
                }

            }
        } catch (Exception e) {
            RSM.getLogger().info("Failed to read or parse favorite colors: {}", e.getMessage());
        }
    }

    public void saveFavoriteColors() {
        val colorsArray = new JsonArray();
        for (Color color : favoriteColors) {
            colorsArray.add(color.getHexCode(true));
        }

        File toSave = FileUtils.getSaveFileInCategory("universal",  "favoritecolors.json");

        //noinspection ResultOfMethodCallIgnored
        toSave.getParentFile().mkdirs();

        try {
            org.apache.commons.io.FileUtils.write(toSave, gson.toJson(colorsArray), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        for (DefaultGroupSettingWrapper groupSetting : groupSettings) {
            saveGroupSetting(groupSetting);
        }
        saveFavoriteColors();
    }

    public void addGroupSetting(DefaultGroupSetting setting, String id) {
        val wrapper = new DefaultGroupSettingWrapper(setting, id);
        groupSettings.add(wrapper);
        loadGroupSetting(wrapper);
        val moduleTab = new ModuleTab(
                setting.getValue(),
                () -> selectedTab,
                (tab) -> selectedTab = tab,
                clickGui.getContents(),
                clickGui
        );

        tabs.add(moduleTab);
        if (selectedTab == null) selectedTab = moduleTab;
    }

    public void openPage() {
        clickGui.getContents().openContainer(tabs, () -> selectedTab);
    }

    public void removeGroupSetting(DefaultGroupSetting setting) {
        val optionalSettingWrapper = groupSettings.stream().filter(wrapper -> wrapper.group == setting).findFirst();
        if (optionalSettingWrapper.isEmpty()) {
            RSM.getLogger().info("Setting: {}, not found", setting.getName());
            return;
        }
        val settingWrapper = optionalSettingWrapper.get();

        groupSettings.remove(settingWrapper);
        saveGroupSetting(settingWrapper);
    }

    private void saveGroupSetting(DefaultGroupSettingWrapper wrapper) {
        val groupObj = new JsonObject();
        val setting = wrapper.group;

        val id = wrapper.id;
        groupObj.addProperty("id", id);

        val sub = setting.getValue();
        JsonArray arr2 = new JsonArray();
        for (Setting<?> s2 : sub.getSettings()) {
            if (!s2.savesToConfig() || s2.isNotPersistent()) continue;
            JsonObject obj = new JsonObject();

            s2.writeToJson(obj);

            arr2.add(obj);
        }
        groupObj.add("settings", arr2);
        File toSave = FileUtils.getSaveFileInCategory("universal", id + ".json");

        //noinspection ResultOfMethodCallIgnored
        toSave.getParentFile().mkdirs();

        try {
            org.apache.commons.io.FileUtils.write(toSave, gson.toJson(groupObj), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void loadGroupSetting(DefaultGroupSettingWrapper groupSettingWrapper) {
        val groupSetting = groupSettingWrapper.group;
        val id = groupSettingWrapper.id;

        val readFile = FileUtils.getSaveFileInCategory("universal", id + ".json");
        if (!readFile.exists()) {
            saveGroupSetting(groupSettingWrapper);
            return;
        }

        JsonObject groupObject;

        try {
            groupObject = JsonParser.parseString(org.apache.commons.io.FileUtils.readFileToString(readFile, StandardCharsets.UTF_8)).getAsJsonObject();

            JsonArray groupSettingsArr = groupObject.getAsJsonArray("settings");
            for (JsonElement settingElement : groupSettingsArr) {
                try {
                    JsonObject settingObj = settingElement.getAsJsonObject();
                    String settingName = settingObj.get("name").getAsString();
                    Setting<?> setting = groupSetting.get(settingName);
                    if (setting == null || setting.isNotPersistent()) {
                        continue;
                    }

                    setting.readFromJson(settingObj);

                } catch (Exception e) {
                    RSM.getLogger().error("Failed to load group: {}", settingElement, e);
                }
            }
        } catch (Exception e) {
            RSM.getLogger().info("Failed to read or parse config: {}", e.getMessage());
        }

    }
}
