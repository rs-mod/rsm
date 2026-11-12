package com.ricedotwho.rsm.module.api;

import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.managers.notification.NotificationManager;
import com.ricedotwho.rsm.managers.notification.NotificationType;
import com.ricedotwho.rsm.module.api.settings.NotPersistent;
import com.ricedotwho.rsm.module.api.settings.Setting;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.module.api.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.type.Keybind;
import com.ricedotwho.rsm.utils.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
public class SubModule<T extends Module> extends ModuleBase {
    protected final T module;
    private final String name;
    protected SubModuleInfo info;

    private ArrayList<Setting<?>> settings = new ArrayList<>();
    @Getter
    private HashSet<HudSetting> hudSettings = new HashSet<>();

    public SubModule(T module) {
        this.module = module;

        if (!this.getClass().isAnnotationPresent(SubModuleInfo.class)) {
            throw new RuntimeException("SubModule class is not annotated with @SubModuleInfo");
        }

        this.info = this.getClass().getAnnotation(SubModuleInfo.class);
        this.name = info.name();
        this.enabled = info.isEnabled();
        if (!info.hasKeybind()) return;
        this.keybind = new Keybind(info.defaultKey(), info.isAllowGui(), this::onKeyToggle);
        settings.add(new KeybindSetting("Toggle", keybind));
    }

    public SubModule(T module, String nameOverride) {
        this.module = module;

        if (!this.getClass().isAnnotationPresent(SubModuleInfo.class)) {
            throw new RuntimeException("SubModule class is not annotated with @SubModuleInfo");
        }

        this.info = this.getClass().getAnnotation(SubModuleInfo.class);
        this.name = nameOverride;
        this.enabled = info.isEnabled();
        this.keybind = new Keybind(info.defaultKey(), info.isAllowGui(), this::onKeyToggle);
    }


    @ApiStatus.Internal
    public void internalRegisterProperty(List<Setting<?>> settingsList) {
        Set<String> names = settingsList.stream().map(Setting::getName).collect(Collectors.toSet());

        this.settings.removeIf(s -> names.contains(s.getName()));
        this.hudSettings.removeIf(s -> names.contains(s.getName()));

        this.settings.addAll(settingsList);
        this.hudSettings.addAll(settingsList.stream().filter(HudSetting.class::isInstance).map(HudSetting.class::cast).toList());
    }

    @ApiStatus.Internal
    public void internalRegisterProperty(Setting<?>... settingsList) {
        Set<String> names = Arrays.stream(settingsList).map(Setting::getName).collect(Collectors.toSet());

        this.settings.removeIf(s -> names.contains(s.getName()));
        this.hudSettings.removeIf(s -> names.contains(s.getName()));

        this.settings.addAll(Arrays.asList(settingsList));
        this.hudSettings.addAll(settingsList.stream().filter(HudSetting.class::isInstance).map(HudSetting.class::cast).toList());
    }

    @SuppressWarnings("unused")
    public Setting<?> getSettingFromName(String name) {
        if (settings == null || settings.isEmpty()) {
            return null;
        }

        return settings.stream()
                .filter(setting -> setting.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) {
            if (mc.player != null) {
                onEnable();
            }
            EventBus.register(this);
            this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().register());
            RSM.getInstance().getModuleManager().onRegister(this);
        } else {
            if (mc.player != null) {
                onDisable();
                reset();
            }
            EventBus.unregister(this);
            this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().unregister());
            RSM.getInstance().getModuleManager().onUnregister(this);
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public boolean onKeyToggle() {
        this.toggle();
        if (this.getInfo().alwaysDisabled()) return false;
        NotificationManager.showNotification((this.isEnabled() ? "Enabled " : "Disabled ") + this.name, "", (this.isEnabled() ? NotificationType.CHECK : NotificationType.CROSS), 2000);
        return false;
    }

    protected void onEnable() {

    }

    protected void onDisable() {

    }

    protected void reset() {

    }

    public void onModuleToggled(boolean state) {
        if (state && (this.enabled || this.info.alwaysDisabled())) {
            if (keybind != null) keybind.register();
            EventBus.register(this);
            this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().register());
            RSM.getInstance().getModuleManager().onRegister(this);
        } else {
            if (keybind != null) keybind.unregister();
            reset();
            EventBus.unregister(this);
            this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().unregister());
            RSM.getInstance().getModuleManager().onUnregister(this);
        }
    }

    @ApiStatus.Internal
    public void registerFields() {
        Class<?> currentClass = this.getClass();
        while (currentClass != null && currentClass != SubModule.class) {
            for (Field declaredField : currentClass.getDeclaredFields()) {
                val isSetting = ReflectionUtils.inheritsClass(Setting.class, declaredField.getType());
                if (!isSetting) continue;
                declaredField.setAccessible(true);
                val notPersistent = declaredField.isAnnotationPresent(NotPersistent.class);

                try {
                    val setting = (Setting<?>) declaredField.get(this);
                    if (setting.isAttached()) continue;
                    setting.setNotPersistent(notPersistent);

                    this.internalRegisterProperty(setting);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }
}
