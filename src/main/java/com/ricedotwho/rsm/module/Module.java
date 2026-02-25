package com.ricedotwho.rsm.module;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.notification.NotificationComponent;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
public class Module extends ModuleBase {

    protected ModuleInfo info;
    private DefaultGroupSetting group = new DefaultGroupSetting("General", this);
    private ArrayList<GroupSetting<? extends SubModule<?>>> settings = new ArrayList<>();


    public Module() {
        int key;
        boolean allowGui;

        if (this.getClass().isAnnotationPresent(ModuleInfo.class)) {
            this.info = this.getClass().getAnnotation(ModuleInfo.class);
            key = info.defaultKey();
            allowGui = info.isAllowGui();
            this.enabled = info.isEnabled();
        } else {
            throw new RuntimeException("Module class is not annotated with @ModuleInfo");
        }

        this.keybind = new Keybind(key, allowGui, this::onKeyToggle);
        if (this.info.hasKeybind()) {
            this.keybind.register();
        }
    }

    public Category getCategory() {
        return info.category();
    }

    public void registerProperty(Setting<?>... args) {
        for (Setting<?> setting : args) {
            if (setting instanceof GroupSetting<?> g) {
                Optional<GroupSetting<?>> dupe = settings.stream().filter(s -> s.getName().equalsIgnoreCase(g.getName())).findFirst();
                if (dupe.isPresent()) {
                    settings.remove(dupe.get());
                    dupe.get().getValue().onModuleToggled(false);
                }
                settings.add(g);
            } else {
                group.add(setting);
            }
        }

        if (group.getValue().getSettings().isEmpty()) {
            settings.remove(group);
        } else if (!settings.contains(group)) {
            settings.addFirst(group);
        }
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

    public <T extends SubModule<?>> T getSubModule(Class<T> subModule) {
        Optional<GroupSetting<?>> opt = this.settings.stream().filter(s ->  subModule.isAssignableFrom(s.getClass())).findFirst();
        return opt.map(g -> subModule.cast(g.getValue())).orElse(null);
    }

    public List<DragSetting> getDragSettings() {
        return this.settings.stream()
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
            if (mc.player != null) {
                onEnable();
            }
            RSM.getInstance().getEventBus().register(this);
            this.settings.forEach(s -> s.getValue().onModuleToggled(true));
            //this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().register());
        } else {
            if (mc.player != null) {
                onDisable();
                reset();
            }
            RSM.getInstance().getEventBus().unregister(this);
            this.settings.forEach(s -> s.getValue().onModuleToggled(false));
            //this.settings.stream().filter(s -> s instanceof KeybindSetting k && !k.isPersistent()).map(s -> (KeybindSetting) s).forEach(s -> s.getValue().unregister());
        }

    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public List<Setting<?>> getShownSettings() {
        return settings.stream().filter(Setting::isShown).collect(Collectors.toList());
    }

    public void onKeyToggle() {
        this.toggle();
        if (this.getInfo().alwaysDisabled()) return;
        NotificationComponent.showNotification(this.getName() + (this.isEnabled() ? " enabled" : " disabled"), "", false, 2000);
    }

    protected void onEnable() {

    }

    protected void onDisable() {

    }

    protected void reset() {

    }

    public void onGuiClosed() {

    }
}
