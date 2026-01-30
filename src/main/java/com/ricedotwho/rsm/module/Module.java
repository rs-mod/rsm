package com.ricedotwho.rsm.module;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.GroupSetting;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.ChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Module implements Accessor {

    protected ModuleInfo info;
    private boolean enabled;
    private GroupSetting group = new GroupSetting("General", () -> true);
    private Keybind keybind;

    private ArrayList<Setting<?>> settings = new ArrayList<>();


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

        settings.add(group);
        this.keybind = new Keybind(key, allowGui, this::onKeyToggle);
        this.keybind.register();
    }

    public Category getCategory() {
        return info.category();
    }

    public void registerProperty(Setting<?>... setting) {
        settings.addAll(Arrays.asList(setting));

        for (Setting<?> s : setting) {
            if (!(s instanceof GroupSetting)) {

                if (settings.stream()
                        .filter(g -> g instanceof GroupSetting)
                        .map(g -> (GroupSetting) g)
                        .noneMatch(g -> g.getValue().contains(s))) {
                    group.add(s);
                }
            }
        }
    }

    public List<GroupSetting> getGroupsSetting() {
        return settings.stream()
                .filter(s -> s instanceof GroupSetting)
                .map(s -> (GroupSetting) s)
                .collect(Collectors.toList());
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
        } else {
            if (mc.player != null) {
                onDisable();
                reset();
            }
            RSM.getInstance().getEventBus().unregister(this);
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
        // placeholder until notifs are fixed
        ChatUtils.chat(this.getName() + (this.isEnabled() ? ChatFormatting.GREEN + " enabled" : ChatFormatting.RED + " disabled"));
        //NotificationComponent.showNotification(this.getName() + (this.isEnabled() ? " enabled" : " disabled"), "", false, 2000);
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
