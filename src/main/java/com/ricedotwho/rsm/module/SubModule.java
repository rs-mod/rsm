package com.ricedotwho.rsm.module;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.notification.NotificationComponent;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class SubModule<T extends Module> extends ModuleBase {
    protected final T module;
    private final String name;
    protected SubModuleInfo info;
    private ArrayList<Setting> settings = new ArrayList<>();

    public SubModule(T module) {
        this.module = module;

        if (this.getClass().isAnnotationPresent(SubModuleInfo.class)) {
            this.info = this.getClass().getAnnotation(SubModuleInfo.class);
            this.name = info.name();
            this.enabled = info.isEnabled();
        } else {
            throw new RuntimeException("SubModule class is not annotated with @SubModuleInfo");
        }

        this.keybind = new Keybind(info.defaultKey(), info.isAllowGui(), this::onKeyToggle);
    }

    public SubModule(T module, String nameOverride) {
        this.module = module;

        if (this.getClass().isAnnotationPresent(SubModuleInfo.class)) {
            this.info = this.getClass().getAnnotation(SubModuleInfo.class);
            this.name = nameOverride;
            this.enabled = info.isEnabled();
        } else {
            throw new RuntimeException("SubModule class is not annotated with @SubModuleInfo");
        }

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
            RSM.getInstance().getEventBus().register(this);
            this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().register());
        } else {
            if (mc.player != null) {
                onDisable();
                reset();
            }
            RSM.getInstance().getEventBus().unregister(this);
            this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().unregister());
        }

    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public List<Setting> getShownSettings() {
        return settings.stream().filter(Setting::isShown).collect(Collectors.toList());
    }

    public void onKeyToggle() {
        this.toggle();
        if (this.getInfo().alwaysDisabled()) return;
        NotificationComponent.showNotification(this.name + (this.isEnabled() ? " enabled" : " disabled"), "", false, 2000);
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
            RSM.getInstance().getEventBus().register(this);
            this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().register());
        } else {
            if (keybind != null) keybind.unregister();
            reset();
            RSM.getInstance().getEventBus().unregister(this);
            this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().unregister());
        }
    }
}
