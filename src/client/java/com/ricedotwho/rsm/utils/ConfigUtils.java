package com.ricedotwho.rsm.utils;

import com.google.gson.*;

import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import lombok.experimental.UtilityClass;
import com.ricedotwho.rsm.module.Module;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class ConfigUtils {

    // todo: each module should have its own .json file

    public void saveConfig(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for (Module m : RSM.getInstance().getModuleManager().getMap().values()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("toggled", m.isEnabled());
            moduleObj.addProperty("keybind", m.getKeybind().getKeyBind().getValue());

            JsonArray arr = new JsonArray();
            for (GroupSetting s : m.getGroupsSetting()) {
                JsonObject groupObj = new JsonObject();
                groupObj.addProperty("name", s.getName());

                JsonArray arr2 = new JsonArray();
                for (Setting<?> s2 : s.getValue()) {
                    JsonObject obj = new JsonObject();

                    // Types that dont save!
                    if (s2 instanceof ButtonSetting) continue;

                    obj.addProperty("name", s2.getName());

                    if (s2 instanceof BooleanSetting) {
                        obj.addProperty("type", "boolean");
                        obj.addProperty("value", ((BooleanSetting) s2).getValue());
                    } else if (s2 instanceof NumberSetting) {
                        obj.addProperty("type", "number");
                        obj.addProperty("value", ((NumberSetting) s2).getValue());
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
                        obj.addProperty("value", ((KeybindSetting) s2).getValue().getKeyBind().getValue());
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
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject moduleObj = new JsonObject();
        moduleObj.addProperty("toggled", m.isEnabled());
        moduleObj.addProperty("keybind", m.getKeybind().getKeyBind().getValue());

        JsonArray arr = new JsonArray();
        for (GroupSetting s : m.getGroupsSetting()) {
            JsonObject groupObj = new JsonObject();
            groupObj.addProperty("name", s.getName());

            JsonArray arr2 = new JsonArray();
            for (Setting<?> s2 : s.getValue()) {
                JsonObject obj = new JsonObject();

                // Types that dont save!
                if (s2 instanceof ButtonSetting) continue;

                obj.addProperty("name", s2.getName());

                if (s2 instanceof BooleanSetting) {
                    obj.addProperty("type", "boolean");
                    obj.addProperty("value", ((BooleanSetting) s2).getValue());
                } else if (s2 instanceof NumberSetting) {
                    obj.addProperty("type", "number");
                    obj.addProperty("value", ((NumberSetting) s2).getValue());
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
                    obj.addProperty("value", ((KeybindSetting) s2).getValue().getKeyBind().getValue());
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

//    public void configSave(String name) {
//        JsonObject js = new JsonObject();
//        //js.addProperty("client_version", RSM.getInstance().getVersion());
//        js.addProperty("name", name);
//        js.addProperty("date_saved", getFormattedDateTime());
//
//        JsonObject modulesObj = new JsonObject();
//        for (Module m : RSM.getInstance().getModuleManager().getMap().values()) {
//            JsonObject moduleObj = new JsonObject();
//            moduleObj.addProperty("toggled", m.isEnabled());
//            moduleObj.addProperty("keybind", m.getKeybind().getKeyBind());
//
//            JsonArray arr = new JsonArray();
//            for (GroupSetting s : m.getGroupsSetting()) {
//                JsonObject groupObj = new JsonObject();
//                groupObj.addProperty("name", s.getName());
//
//                JsonArray arr2 = new JsonArray();
//                for (Setting<?> s2 : s.getValue()) {
//                    JsonObject obj = new JsonObject();
//
//                    // Types that dont save!
//                    if (s2 instanceof ButtonSetting) continue;
//
//                    obj.addProperty("name", s2.getName());
//
//                    if (s2 instanceof BooleanSetting) {
//                        obj.addProperty("type", "boolean");
//                        obj.addProperty("value", ((BooleanSetting) s2).getValue());
//                    } else if (s2 instanceof NumberSetting) {
//                        obj.addProperty("type", "number");
//                        obj.addProperty("value", ((NumberSetting) s2).getValue());
//                    } else if (s2 instanceof ModeSetting) {
//                        obj.addProperty("type", "mode");
//                        obj.addProperty("value", ((ModeSetting) s2).getValue());
//                    } else if (s2 instanceof MultiBoolSetting) {
//                        obj.addProperty("type", "multibool");
//                        MultiBoolSetting mbs = (MultiBoolSetting) s2;
//                        JsonArray boolarray = new JsonArray();
//
//                        for (String key : mbs.getValue().keySet()) {
//                            JsonObject boolobject = new JsonObject();
//                            boolobject.addProperty("name", key);
//                            boolobject.addProperty("value", mbs.getValue().get(key));
//                            boolarray.add(boolobject);
//                        }
//                        obj.add("values", boolarray);
//                    } else if (s2 instanceof StringSetting) {
//                        obj.addProperty("type", "string");
//                        obj.addProperty("value", ((StringSetting) s2).getValue());
//                    } else if (s2 instanceof DragSetting) {
//                        obj.addProperty("type", "drag");
//                        obj.addProperty("x", ((DragSetting) s2).getPosition().x);
//                        obj.addProperty("y", ((DragSetting) s2).getPosition().y);
//                        obj.addProperty("scaleX", ((DragSetting) s2).getScale().x);
//                        obj.addProperty("scaleY", ((DragSetting) s2).getScale().y);
//                    } else if (s2 instanceof KeybindSetting) {
//                        obj.addProperty("type", "keybind");
//                        obj.addProperty("value", ((KeybindSetting) s2).getValue().getKeyBind());
//                    } else if (s2 instanceof ColourSetting) {
//                        obj.addProperty("type", "colour");
//                        obj.addProperty("hue", ((ColourSetting) s2).getValue().getHue());
//                        obj.addProperty("saturation", ((ColourSetting) s2).getValue().getSaturation());
//                        obj.addProperty("brightness", ((ColourSetting) s2).getValue().getBrightness());
//                        obj.addProperty("alpha", ((ColourSetting) s2).getValue().getAlpha());
//                        obj.addProperty("dataBit", ((ColourSetting) s2).getValue().getDataBitRaw());
//                    }
//
//                    arr2.add(obj);
//                }
//                groupObj.add("settings", arr2);
//                arr.add(groupObj);
//            }
//            moduleObj.add("settings", arr);
//            modulesObj.add(m.getInfo().id(), moduleObj);
//        }
//        js.add("modules", modulesObj);
//
//        File toSave = FileUtils.getSaveFileInCategory("config", name + ".json");
//        toSave.getParentFile().mkdirs();
//
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//        try {
//            FileUtils.write(toSave, gson.toJson(js));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

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
            boolean moduleEnabled = false;
            try {
                if (module.isEnabled() != moduleObj.get("toggled").getAsBoolean()) {
                    module.toggle();
                    moduleEnabled = true;
                }
                module.getKeybind().setKeyBind(InputConstants.Type.KEYSYM.getOrCreate(moduleObj.get("keybind").getAsInt()));
            } catch (Exception e) {
                ChatUtils.chat("Failed to load keybind for " + moduleName);
                modified = true;
            }

            JsonArray settingsArr = moduleObj.getAsJsonArray("settings");
            for (JsonElement groupElement : settingsArr) {
                try {
                    JsonObject groupObj = groupElement.getAsJsonObject();
                    String groupName = groupObj.get("name").getAsString();
                    GroupSetting groupSetting = (GroupSetting) module.getSettingFromName(groupName);
                    if (groupSetting == null) {
                        modified = true;
                        continue;
                    }
                    groupSetting.register();

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
                                    if (setting instanceof BooleanSetting) {
                                        ((BooleanSetting) setting).setValue(settingObj.get("value").getAsBoolean());
                                    }
                                    break;
                                case "number":
                                    if (setting instanceof NumberSetting) {
                                        ((NumberSetting) setting).setValue(settingObj.get("value").getAsDouble());
                                        ((NumberSetting) setting).setStringValue(settingObj.get("value").getAsString());
                                    }
                                    break;
                                case "mode":
                                    if (setting instanceof ModeSetting) {
                                        ((ModeSetting) setting).setValue(settingObj.get("value").getAsString());
                                    }
                                    break;
                                case "multibool":
                                    if (setting instanceof MultiBoolSetting) {
                                        ((MultiBoolSetting) setting).loadFromJson(settingObj);
                                    }
                                    break;
                                case "string":
                                    if (setting instanceof StringSetting) {
                                        ((StringSetting) setting).setValue(settingObj.get("value").getAsString());
                                    }
                                    break;
                                case "drag":
                                    if (setting instanceof DragSetting) {
                                        ((DragSetting) setting).loadFromJson(settingObj);
                                    }
                                    break;
                                case "keybind":
                                    if (setting instanceof KeybindSetting) {
                                        ((KeybindSetting) setting).loadFromJson(settingObj);
                                        if(moduleEnabled) ((KeybindSetting) setting).getValue().register(); //freakazoid
                                    }
                                case "colour":
                                    if (setting instanceof ColourSetting) {
                                        ((ColourSetting) setting).loadFromJson(settingObj);
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

//    public void configLoad(String name) {
//        File readf = FileUtils.getSaveFileInCategory("config", name + ".json");
//
//        if (!readf.exists()) {
//            ChatUtils.chat("Config doesn't exist! Use .config list to see saved configs.");
//            return;
//        }
//
//        JsonObject js;
//        boolean modified = false;
//
//        try {
//            js = new JsonParser().parse(FileUtils.readFileToString(readf)).getAsJsonObject();
//        } catch (Exception e) {
//            ChatUtils.chat("Failed to read or parse config: " + e.getMessage());
//            return;
//        }
//
////        try {
////            // unused
////            String clientVersion = js.get("client_version").getAsString();
////            String configName = js.get("name").getAsString();
////            String dateSaved = js.get("date_saved").getAsString();
////        } catch (Exception e) {
////            ChatUtils.chat("Config header is malformed: " + e.getMessage());
////            modified = true;
////        }
//
//        JsonObject modulesObj = js.getAsJsonObject("modules");
//        for (Map.Entry<String, JsonElement> moduleEntry : modulesObj.entrySet()) {
//            try {
//                String moduleName = moduleEntry.getKey();
//                Module module = RSM.getInstance().getModuleManager().getModuleFromID(moduleName);
//                if (module == null) {
//                    modified = true;
//                    continue;
//                }
//
//                JsonObject moduleObj = modulesObj.getAsJsonObject(moduleName);
//                boolean moduleEnabled = false;
//                try {
//                    if (module.isEnabled() != moduleObj.get("toggled").getAsBoolean()) {
//                        module.toggle();
//                        moduleEnabled = true;
//                    }
//                    module.getKeybind().setKeyBind(moduleObj.get("keybind").getAsInt());
//                } catch (Exception e) {
//                    ChatUtils.chat("Failed to load keybind for " + moduleName);
//                    modified = true;
//                }
//
//                JsonArray settingsArr = moduleObj.getAsJsonArray("settings");
//                for (JsonElement groupElement : settingsArr) {
//                    try {
//                        JsonObject groupObj = groupElement.getAsJsonObject();
//                        String groupName = groupObj.get("name").getAsString();
//                        GroupSetting groupSetting = (GroupSetting) module.getSettingFromName(groupName);
//                        if (groupSetting == null) {
//                            modified = true;
//                            continue;
//                        }
//                        groupSetting.register();
//
//                        JsonArray groupSettingsArr = groupObj.getAsJsonArray("settings");
//                        for (JsonElement settingElement : groupSettingsArr) {
//                            try {
//                                JsonObject settingObj = settingElement.getAsJsonObject();
//                                String settingName = settingObj.get("name").getAsString();
//                                String type = settingObj.get("type").getAsString();
//                                Setting<?> setting = groupSetting.get(settingName);
//                                if (setting == null) {
//                                    modified = true;
//                                    continue;
//                                }
//
//                                switch (type) {
//                                    case "boolean":
//                                        if (setting instanceof BooleanSetting) {
//                                            ((BooleanSetting) setting).setValue(settingObj.get("value").getAsBoolean());
//                                        }
//                                        break;
//                                    case "number":
//                                        if (setting instanceof NumberSetting) {
//                                            ((NumberSetting) setting).setValue(settingObj.get("value").getAsDouble());
//                                            ((NumberSetting) setting).setStringValue(settingObj.get("value").getAsString());
//                                        }
//                                        break;
//                                    case "mode":
//                                        if (setting instanceof ModeSetting) {
//                                            ((ModeSetting) setting).setValue(settingObj.get("value").getAsString());
//                                        }
//                                        break;
//                                    case "multibool":
//                                        if (setting instanceof MultiBoolSetting) {
//                                            ((MultiBoolSetting) setting).loadFromJson(settingObj);
//                                        }
//                                        break;
//                                    case "string":
//                                        if (setting instanceof StringSetting) {
//                                            ((StringSetting) setting).setValue(settingObj.get("value").getAsString());
//                                        }
//                                        break;
//                                    case "drag":
//                                        if (setting instanceof DragSetting) {
//                                            ((DragSetting) setting).loadFromJson(settingObj);
//                                        }
//                                        break;
//                                    case "keybind":
//                                        if (setting instanceof KeybindSetting) {
//                                            ((KeybindSetting) setting).loadFromJson(settingObj);
//                                            if(moduleEnabled) ((KeybindSetting) setting).getValue().register(); //freakazoid
//                                        }
//                                    case "colour":
//                                        if (setting instanceof ColourSetting) {
//                                            ((ColourSetting) setting).loadFromJson(settingObj);
//                                        }
//                                        break;
//                                    default:
//                                        modified = true;
//                                        break;
//                                }
//                                setting.register();
//                            } catch (Exception e) {
//                                ChatUtils.chat("Skipped malformed setting.");
//                                modified = true;
//                            }
//                        }
//                    } catch (Exception e) {
//                        ChatUtils.chat("Skipped malformed group setting.");
//                        modified = true;
//                    }
//                }
//
//            } catch (Exception e) {
//                ChatUtils.chat("Skipped malformed module: " + moduleEntry.getKey());
//                modified = true;
//            }
//        }
//
//        ChatUtils.chat("Config \"" + name + "\" loaded successfully.");
//
//        if (modified) {
//            ChatUtils.chat("Some config entries were invalid and skipped. Saving cleaned config...");
//            configSave(name);
//        }
//    }

    public static String getFormattedDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
        return now.format(formatter);
    }
}

