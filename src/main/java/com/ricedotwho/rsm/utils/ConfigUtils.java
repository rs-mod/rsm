package com.ricedotwho.rsm.utils;

import com.google.gson.*;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import lombok.experimental.UtilityClass;
import com.ricedotwho.rsm.module.Module;

import java.io.File;
import java.io.IOException;

@UtilityClass
public class ConfigUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();;
    // todo: each module should have its own .json file

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
                    JsonObject obj = new JsonObject();

                    // Types that don't save!
                    if (s2 instanceof ButtonSetting) continue;

                    obj.addProperty("name", s2.getName());

                    if (s2 instanceof BooleanSetting) {
                        obj.addProperty("type", "boolean");
                        obj.addProperty("value", ((BooleanSetting) s2).getValue());
                    } else if (s2 instanceof NumberSetting) {
                        obj.addProperty("type", "number");
                        obj.addProperty("value", ((NumberSetting) s2).getValue().toPlainString());
                    } else if (s2 instanceof ModeSetting) {
                        obj.addProperty("type", "mode");
                        obj.addProperty("value", ((ModeSetting) s2).getValue());
                    } else if (s2 instanceof MultiBoolSetting) {
                        obj.addProperty("type", "multibool");
                        MultiBoolSetting mbs = (MultiBoolSetting) s2;
                        JsonArray boolarray = new JsonArray();

                        for (String key : mbs.getValue().keySet()) {
                            JsonObject boolobject = new JsonObject();
                            boolobject.addProperty("name", key);
                            boolobject.addProperty("value", mbs.getValue().get(key));
                            boolarray.add(boolobject);
                        }
                        obj.add("values", boolarray);
                    } else if (s2 instanceof StringSetting) {
                        obj.addProperty("type", "string");
                        obj.addProperty("value", ((StringSetting) s2).getValue());
                    } else if (s2 instanceof DragSetting) {
                        obj.addProperty("type", "drag");
                        obj.addProperty("x", ((DragSetting) s2).getPosition().x);
                        obj.addProperty("y", ((DragSetting) s2).getPosition().y);
                        obj.addProperty("scaleX", ((DragSetting) s2).getScale().x);
                        obj.addProperty("scaleY", ((DragSetting) s2).getScale().y);
                    } else if (s2 instanceof KeybindSetting) {
                        obj.addProperty("type", "keybind");
                        obj.addProperty("value", ((KeybindSetting) s2).getValue().getKeyBind().getName());
                    } else if (s2 instanceof ColourSetting) {
                        obj.addProperty("type", "colour");
                        obj.addProperty("hue", ((ColourSetting) s2).getValue().getHue());
                        obj.addProperty("saturation", ((ColourSetting) s2).getValue().getSaturation());
                        obj.addProperty("brightness", ((ColourSetting) s2).getValue().getBrightness());
                        obj.addProperty("alpha", ((ColourSetting) s2).getValue().getAlpha());
                        obj.addProperty("dataBit", ((ColourSetting) s2).getValue().getDataBitRaw());
                    }

                    arr2.add(obj);
                }
                groupObj.add("settings", arr2);
                arr.add(groupObj);
            }
            moduleObj.add("settings", arr);

            File toSave = FileUtils.getSaveFileInCategory("config", m.getID() + ".json");
            toSave.getParentFile().mkdirs();

            try {
                org.apache.commons.io.FileUtils.write(toSave, gson.toJson(moduleObj));
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
                JsonObject obj = new JsonObject();

                // Types that dont save!
                if (s2 instanceof ButtonSetting) continue;

                obj.addProperty("name", s2.getName());

                if (s2 instanceof BooleanSetting) {
                    obj.addProperty("type", "boolean");
                    obj.addProperty("value", ((BooleanSetting) s2).getValue());
                } else if (s2 instanceof NumberSetting) {
                    obj.addProperty("type", "number");
                    obj.addProperty("value", ((NumberSetting) s2).getValue().toPlainString());
                } else if (s2 instanceof ModeSetting) {
                    obj.addProperty("type", "mode");
                    obj.addProperty("value", ((ModeSetting) s2).getValue());
                } else if (s2 instanceof MultiBoolSetting) {
                    obj.addProperty("type", "multibool");
                    MultiBoolSetting mbs = (MultiBoolSetting) s2;
                    JsonArray boolarray = new JsonArray();

                    for (String key : mbs.getValue().keySet()) {
                        JsonObject boolobject = new JsonObject();
                        boolobject.addProperty("name", key);
                        boolobject.addProperty("value", mbs.getValue().get(key));
                        boolarray.add(boolobject);
                    }
                    obj.add("values", boolarray);
                } else if (s2 instanceof StringSetting) {
                    obj.addProperty("type", "string");
                    obj.addProperty("value", ((StringSetting) s2).getValue());
                } else if (s2 instanceof DragSetting) {
                    obj.addProperty("type", "drag");
                    obj.addProperty("x", ((DragSetting) s2).getPosition().x);
                    obj.addProperty("y", ((DragSetting) s2).getPosition().y);
                    obj.addProperty("scaleX", ((DragSetting) s2).getScale().x);
                    obj.addProperty("scaleY", ((DragSetting) s2).getScale().y);
                } else if (s2 instanceof KeybindSetting) {
                    obj.addProperty("type", "keybind");
                    obj.addProperty("value", ((KeybindSetting) s2).getValue().getKeyBind().getName());
                } else if (s2 instanceof ColourSetting) {
                    obj.addProperty("type", "colour");
                    obj.addProperty("hue", ((ColourSetting) s2).getValue().getHue());
                    obj.addProperty("saturation", ((ColourSetting) s2).getValue().getSaturation());
                    obj.addProperty("brightness", ((ColourSetting) s2).getValue().getBrightness());
                    obj.addProperty("alpha", ((ColourSetting) s2).getValue().getAlpha());
                    obj.addProperty("dataBit", ((ColourSetting) s2).getValue().getDataBitRaw());
                }

                arr2.add(obj);
            }
            groupObj.add("settings", arr2);
            arr.add(groupObj);
        }
        moduleObj.add("settings", arr);

        File toSave = FileUtils.getSaveFileInCategory("config", m.getID() + ".json");
        toSave.getParentFile().mkdirs();

        try {
            org.apache.commons.io.FileUtils.write(toSave, gson.toJson(moduleObj));
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
            moduleObj = new JsonParser().parse(org.apache.commons.io.FileUtils.readFileToString(readf)).getAsJsonObject();
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

                    SubModule sub = groupSetting.getValue();

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

                            switch (type) {
                                case "boolean":
                                    if (setting instanceof BooleanSetting setting1) {
                                        setting1.setValue(settingObj.get("value").getAsBoolean());
                                    }
                                    break;
                                case "number":
                                    if (setting instanceof NumberSetting setting1) {
                                        String value = settingObj.get("value").getAsString();
                                        setting1.setValue(value);
                                        setting1.setStringValue(value);
                                    }
                                    break;
                                case "mode":
                                    if (setting instanceof ModeSetting setting1) {
                                        setting1.setValue(settingObj.get("value").getAsString());
                                    }
                                    break;
                                case "multibool":
                                    if (setting instanceof MultiBoolSetting setting1) {
                                        setting1.loadFromJson(settingObj);
                                    }
                                    break;
                                case "string":
                                    if (setting instanceof StringSetting setting1) {
                                        setting1.setValue(settingObj.get("value").getAsString());
                                    }
                                    break;
                                case "drag":
                                    if (setting instanceof DragSetting setting1) {
                                        setting1.loadFromJson(settingObj);
                                    }
                                    break;
                                case "keybind":
                                    if (setting instanceof KeybindSetting setting1) {
                                        setting1.loadFromJson(settingObj);
                                    }
                                case "colour":
                                    if (setting instanceof ColourSetting setting1) {
                                        setting1.loadFromJson(settingObj);
                                    }
                                    break;
                                default:
                                    modified = true;
                                    break;
                            }
                            setting.register();
                        } catch (Exception e) {
                            ChatUtils.chat("Skipped malformed setting.");
                            modified = true;
                        }
                    }
                } catch (Exception e) {
                    ChatUtils.chat("Skipped malformed group setting.");
                    modified = true;
                }
            }

        } catch (Exception e) {
            ChatUtils.chat("Skipped malformed module: " + moduleName);
            modified = true;
        }

        if (modified) {
            ChatUtils.chat("Some config entries were invalid and skipped. Saving cleaned config...");
            saveConfig(module);
        }
    }
}

