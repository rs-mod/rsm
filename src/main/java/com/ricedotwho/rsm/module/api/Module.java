package com.ricedotwho.rsm.module.api;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.managers.notification.NotificationManager;
import com.ricedotwho.rsm.module.api.settings.NotPersistent;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.module.api.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.module.api.settings.group.GroupSetting;
import com.ricedotwho.rsm.module.api.settings.impl.DragSetting;
import com.ricedotwho.rsm.type.Keybind;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import com.ricedotwho.rsm.utils.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@SuppressWarnings("unused")
public class Module extends ModuleBase {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    protected ModuleInfo info;

    private DefaultGroupSetting generalGroup = new DefaultGroupSetting("General", this);
    private ArrayList<GroupSetting<? extends SubModule<?>>> groupSettings = new ArrayList<>();

    public Module() {
        if (!this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            throw new RuntimeException("Module class is not annotated with @ModuleInfo");
        }

        this.info = this.getClass().getAnnotation(ModuleInfo.class);
        int key = info.defaultKey();
        boolean allowGui = info.isAllowGui();
        this.enabled = info.isEnabled();

        this.keybind = new Keybind(key, allowGui, this::onKeyToggle);

        if (!this.info.hasKeybind()) return;
        this.keybind.register();
    }

    public Category getCategory() {
        return info.category();
    }


    @ApiStatus.Internal
    public void registerFields() {
        Class<?> currentClass = this.getClass();
        while (currentClass != null && currentClass != Module.class) {
            for (Field declaredField : currentClass.getDeclaredFields()) {
                val isSetting = ReflectionUtils.inheritsClass(Setting.class, declaredField.getType());
                if (!isSetting) continue;
                if (ReflectionUtils.isStatic(declaredField)) {
                    throw new IllegalArgumentException("Field " + declaredField.getName() + " is static in " + this.getClass().getSimpleName());
                }

                declaredField.setAccessible(true);
                val notPersistent = declaredField.isAnnotationPresent(NotPersistent.class);
                try {
                    val value = declaredField.get(this);
                    if (value == null) throw new IllegalArgumentException("Field is null! field: " + declaredField.getName() + ", module: " + this.getClass().getSimpleName());
                    val setting = (Setting<?>) value;
                    if (setting.isAttached()) continue;
                    setting.setNotPersistent(notPersistent);

                    this.registerProperty(setting);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private void registerProperty(Setting<?>... args) {
        for (Setting<?> setting : args) {
            if (!(setting instanceof GroupSetting<?> g)) {
                generalGroup.add(setting);
                continue;
            }

            Optional<GroupSetting<?>> dupe = groupSettings.stream()
                    .filter(s -> s.getName().equalsIgnoreCase(g.getName()))
                    .findFirst();

            if (dupe.isPresent()) {
                groupSettings.remove(dupe.get());
                dupe.get().getValue().onModuleToggled(false);

                g.add(dupe.get().getValue().getSettings());
            }

            groupSettings.add(g);
            g.getValue().registerFields();

        }
        syncGeneralGroup();
    }

    @ApiStatus.Internal
    public void syncGeneralGroup() {
        if (generalGroup.getValue().getSettings().isEmpty()) {
            groupSettings.remove(generalGroup);
        } else if (!groupSettings.contains(generalGroup)) {
            groupSettings.addFirst(generalGroup);
        }
    }

    public Setting<?> getSettingFromName(String name) {
        if (groupSettings == null || groupSettings.isEmpty()) {
            return null;
        }

        return groupSettings.stream()
                .filter(setting -> setting.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @SuppressWarnings("unused")
    public <T extends com.ricedotwho.rsm.module.api.SubModule<?>> T getSubModule(Class<T> subModule) {
        Optional<GroupSetting<?>> opt = this.groupSettings.stream().filter(s ->  subModule.isAssignableFrom(s.getClass())).findFirst();
        return opt.map(g -> subModule.cast(g.getValue())).orElse(null);
    }

    public List<DragSetting> getDragSettings() {
        return this.groupSettings.stream()
                .flatMap(s -> s.getValue().getSettings().stream())
                .filter(DragSetting.class::isInstance)
                .map(DragSetting.class::cast)
                .toList();
    }

    public String getName() {
        return info.aliases()[0];
    }
    public String getID() {
        return info.id();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            if (mc.player != null) onEnable();

            syncRegistrationState();
            this.groupSettings.forEach(s -> s.getValue().onModuleToggled(true));
            return;
        }

        if (mc.player != null) {
            onDisable();
            reset();
        }
        syncRegistrationState();
        this.groupSettings.forEach(s -> s.getValue().onModuleToggled(false));
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public boolean onKeyToggle() {
        this.toggle();
        if (this.getInfo().alwaysDisabled()) return false;
        NotificationManager.showNotification((this.isEnabled() ? "Enabled " : "Disabled ") + this.getName(), "", false, 2000);
        return false;
    }


    public void saveConfig() {
        val moduleObject = new JsonObject();
        moduleObject.addProperty("toggled", enabled);
        moduleObject.addProperty("keybind", keybind.getKey().getName());

        JsonArray arr = new JsonArray();
        for (GroupSetting<?> s : groupSettings) {
            val sub = s.getValue();
            val groupObj = new JsonObject();
            groupObj.addProperty("name", s.getName());
            groupObj.addProperty("toggled", sub.isEnabled());
            groupObj.addProperty("keybind", sub.getKeybind().getKey().getName());

            JsonArray arr2 = new JsonArray();
            for (Setting<?> s2 : sub.getSettings()) {
                if (!s2.savesToConfig() || s2.isNotPersistent()) continue;
                JsonObject obj = new JsonObject();

                s2.writeToJson(obj);

                arr2.add(obj);
            }
            groupObj.add("settings", arr2);
            arr.add(groupObj);
        }
        moduleObject.add("settings", arr);

        File toSave = FileUtils.getSaveFileInCategory("config", getID() + ".json");
        //noinspection ResultOfMethodCallIgnored
        toSave.getParentFile().mkdirs();

        try {
            org.apache.commons.io.FileUtils.write(toSave, gson.toJson(moduleObject), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadConfig() {
        val moduleName = getID();
        val readFile = FileUtils.getSaveFileInCategory("config", moduleName + ".json");
        if (!readFile.exists()) saveConfig();

        JsonObject moduleObject;

        try {
            moduleObject = JsonParser.parseString(org.apache.commons.io.FileUtils.readFileToString(readFile, StandardCharsets.UTF_8)).getAsJsonObject();
            boolean moduleState = moduleObject.get("toggled").getAsBoolean();

            try {
                if (enabled != moduleState) {
                    toggle();
                }

                //I need to call this just in case the module is enabled by default.
                syncRegistrationState();

                keybind.setKey(InputConstants.getKey(moduleObject.get("keybind").getAsString()));
            } catch (Exception e) {
                ChatUtils.chat("Failed to load keybind for " + moduleName);
            }

            val settingsArray = moduleObject.getAsJsonArray("settings");
            for (JsonElement element : settingsArray) {
                try {
                    loadGroup(element.getAsJsonObject());
                } catch (Exception e) {
                    ChatUtils.chat("Skipped malformed group setting. (%s)", element);
                    RSM.getLogger().error("Failed to load grounp setting: {}", element, e);
                }
            }

        } catch (Exception e) {
            ChatUtils.chat("Failed to read or parse config: " + e.getMessage());
            return;
        }

        onLoaded();
    }


    private void loadGroup(JsonObject group) {
        String groupName = group.get("name").getAsString();
        GroupSetting<?> groupSetting = (GroupSetting<?>) getSettingFromName(groupName);
        if (groupSetting == null) {
            return;
        }

        SubModule<?> sub = groupSetting.getValue();

        try {
            if (!sub.getInfo().alwaysDisabled() && sub.isEnabled() != group.get("toggled").getAsBoolean()) {
                sub.toggle();
            }
            sub.getKeybind().setKey(InputConstants.getKey(group.get("keybind").getAsString()));
        } catch (Exception e) {
            // ignored
        }

        sub.onModuleToggled(enabled);

        JsonArray groupSettingsArr = group.getAsJsonArray("settings");
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
                ChatUtils.chat("Skipped malformed group. (%s)", settingElement);
                RSM.getLogger().error("Failed to load group: {}", settingElement, e);
            }
        }
    }

    private boolean isRegistered = false;
    public void syncRegistrationState() {
        if (isRegistered && !enabled) {
            EventBus.unregister(this);
            isRegistered = false;
            return;
        }
        if (!isRegistered && enabled) {
            EventBus.register(this);
            isRegistered = true;
        }
    }

    public void init() {

    }

    public void loadDefaults() {

    }

    protected void onEnable() {

    }

    protected void onDisable() {

    }

    protected void reset() {

    }

    public void onGuiClosed() {

    }

    public void onLoaded() {

    }


}
