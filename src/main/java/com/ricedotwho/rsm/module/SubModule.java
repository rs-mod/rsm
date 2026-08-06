package com.ricedotwho.rsm.module;

import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.managers.notification.NotificationComponent;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.module.api.settings.NotPersistent;
import com.ricedotwho.rsm.type.Keybind;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.utils.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

@Setter
@Getter
public class SubModule<T extends Module> extends ModuleBase {
    protected final T module;
    private final String name;
    protected SubModuleInfo info;

    private ArrayList<Setting<?>> settings = new ArrayList<>();

    public SubModule(T module) {
        this.module = module;

        if (!this.getClass().isAnnotationPresent(SubModuleInfo.class)) {
            throw new RuntimeException("SubModule class is not annotated with @SubModuleInfo");
        }

        this.info = this.getClass().getAnnotation(SubModuleInfo.class);
        this.name = info.name();
        this.enabled = info.isEnabled();
        this.keybind = new Keybind(info.defaultKey(), info.isAllowGui(), this::onKeyToggle);
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

    public void registerProperty(Setting<?>... setting) {
        settings.addAll(Arrays.asList(setting));
    }

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
        } else {
            if (mc.player != null) {
                onDisable();
                reset();
            }
            EventBus.unregister(this);
            this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().unregister());
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public boolean onKeyToggle() {
        this.toggle();
        if (this.getInfo().alwaysDisabled()) return false;
        NotificationComponent.showNotification((this.isEnabled() ? "Enabled " : "Disabled ") + this.name, "", false, 2000);
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
        } else {
            if (keybind != null) keybind.unregister();
            reset();
            EventBus.unregister(this);
            this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().unregister());
        }
    }

    @ApiStatus.Internal
    public void registerSettings() {
        for (Field declaredField : this.getClass().getDeclaredFields()) {
            val isSetting = ReflectionUtils.inheritsClass(Setting.class, declaredField.getType());
            if (!isSetting) continue;
            declaredField.setAccessible(true);
            val notPersistent = declaredField.isAnnotationPresent(NotPersistent.class);

            try {
                val setting = (Setting<?>) declaredField.get(this);
                if (setting.isAttached()) continue;
                setting.setNotPersistent(notPersistent);

                this.registerProperty(setting);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
