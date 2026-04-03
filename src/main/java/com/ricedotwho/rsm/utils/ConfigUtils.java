package com.ricedotwho.rsm.utils;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class ConfigUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();;

    public void saveConfig(){
        for (Module m : RSM.getInstance().getModuleManager().getMap().values()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("toggled", m.isEnabled());
            moduleObj.addProperty("keybind", m.getKeybind().getKeyBind().getName());

            JsonArray arr = new JsonArray();
            for (GroupSetting<?> s : m.getSettings()) {
                SubModule<?> sub = s.getValue();
                JsonObject groupObj = new JsonObject();
                groupObj.addProperty("name", s.getName());
                groupObj.addProperty("toggled", sub.isEnabled());
                groupObj.addProperty("keybind", sub.getKeybind().getKeyBind().getName());

                JsonArray arr2 = new JsonArray();
                for (Setting<?> s2 : sub.getSettings()) {
                    if (!s2.savesToConfig()) continue;
                    JsonObject obj = new JsonObject();

                    s2.saveToJson(obj);

                    arr2.add(obj);
                }
                groupObj.add("settings", arr2);
                arr.add(groupObj);
            }
            moduleObj.add("settings", arr);

            File toSave = FileUtils.getSaveFileInCategory("config", m.getID() + ".json");
            //noinspection ResultOfMethodCallIgnored
            toSave.getParentFile().mkdirs();

            try {
                org.apache.commons.io.FileUtils.write(toSave, gson.toJson(moduleObj), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void saveConfig(Module m){
        JsonObject moduleObj = new JsonObject();
        moduleObj.addProperty("toggled", m.isEnabled());
        moduleObj.addProperty("keybind", m.getKeybind().getKeyBind().getName());

        JsonArray arr = new JsonArray();
        for (GroupSetting<?> s : m.getSettings()) {
            SubModule<?> sub = s.getValue();
            JsonObject groupObj = new JsonObject();
            groupObj.addProperty("name", s.getName());
            groupObj.addProperty("toggled", sub.isEnabled());
            groupObj.addProperty("keybind", sub.getKeybind().getKeyBind().getName());


            JsonArray arr2 = new JsonArray();
            for (Setting<?> s2 : sub.getSettings()) {
                if (!s2.savesToConfig()) continue;
                JsonObject obj = new JsonObject();

                s2.saveToJson(obj);

                arr2.add(obj);
            }
            groupObj.add("settings", arr2);
            arr.add(groupObj);
        }
        moduleObj.add("settings", arr);

        File toSave = FileUtils.getSaveFileInCategory("config", m.getID() + ".json");
        //noinspection ResultOfMethodCallIgnored
        toSave.getParentFile().mkdirs();

        try {
            org.apache.commons.io.FileUtils.write(toSave, gson.toJson(moduleObj), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadConfig(Module module) {
        String moduleName = module.getID();
        File readf = FileUtils.getSaveFileInCategory("config", moduleName + ".json");
        if (!readf.exists()) return;

        JsonObject moduleObj;
        boolean modified = false;

        try {
            moduleObj = JsonParser.parseString(org.apache.commons.io.FileUtils.readFileToString(readf, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            ChatUtils.chat("Failed to read or parse config: " + e.getMessage());
            return;
        }

        try {
            boolean moduleState = moduleObj.get("toggled").getAsBoolean();
            try {
                if (module.isEnabled() != moduleState) {
                    module.toggle();
                }
                module.getKeybind().setKeyBind(InputConstants.getKey(moduleObj.get("keybind").getAsString()));
            } catch (Exception e) {
                ChatUtils.chat("Failed to load keybind for " + moduleName);
                modified = true;
            }

            JsonArray settingsArr = moduleObj.getAsJsonArray("settings");
            for (JsonElement groupElement : settingsArr) {
                try {
                    JsonObject groupObj = groupElement.getAsJsonObject();
                    String groupName = groupObj.get("name").getAsString();
                    GroupSetting<?> groupSetting = (GroupSetting<?>) module.getSettingFromName(groupName);
                    if (groupSetting == null) {
                        modified = true;
                        continue;
                    }
                    groupSetting.register();

                    SubModule<?> sub = groupSetting.getValue();

                    try {
                        if (sub.isEnabled() != groupObj.get("toggled").getAsBoolean()) {
                            sub.toggle();
                        }
                        sub.getKeybind().setKeyBind(InputConstants.getKey(groupObj.get("keybind").getAsString()));
                    } catch (Exception e) {
                        // ignored
                    }

                    sub.onModuleToggled(moduleState);

                    JsonArray groupSettingsArr = groupObj.getAsJsonArray("settings");
                    for (JsonElement settingElement : groupSettingsArr) {
                        try {
                            JsonObject settingObj = settingElement.getAsJsonObject();
                            String settingName = settingObj.get("name").getAsString();
                            String type = settingObj.get("type").getAsString();
                            Setting<?> setting = groupSetting.get(settingName);
                            if (setting == null) {
                                modified = true;
                                continue;
                            }

                            setting.loadFromJson(settingObj);

                            setting.register();
                        } catch (Exception e) {
                            ChatUtils.chat("Skipped malformed setting. (%s)", settingElement);
                            RSM.getLogger().error("Failed to load setting: {}", settingElement, e);
                            modified = true;
                        }
                    }
                } catch (Exception e) {
                    ChatUtils.chat("Skipped malformed group setting. (%s)", groupElement);
                    RSM.getLogger().error("Failed to load grounp setting: {}", groupElement, e);
                    modified = true;
                }
            }

        } catch (Exception e) {
            RSM.getLogger().error("Error while loading config for {}", moduleName, e);
            ChatUtils.chat("Skipped malformed module: " + moduleName);
            modified = true;
        }

        // todo: fix
//        if (modified) {
//            ChatUtils.chat("Some config entries were invalid and skipped. Saving cleaned config...");
//            saveConfig(module);
//        }
        module.onLoaded();
    }
}

