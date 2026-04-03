package com.ricedotwho.rsm.module.impl.player.keyshortcuts;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.SaveSetting;
import com.ricedotwho.rsm.ui.keyshortcuts.KeyShortcutGui;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@ModuleInfo(aliases = "Key Shortcuts", id = "KeyShortcuts", category = Category.PLAYER, alwaysDisabled = true)
public class KeyShortcuts extends Module {

    private final ButtonSetting openShortcuts = new ButtonSetting("Open Shortcuts" , "Open", () -> {
        assert mc.player != null;
        mc.player.closeContainer();
        TaskComponent.onTick(0, KeyShortcutGui::open);
    });

    @Getter
    private static final SaveSetting<List<Shortcut>> data = new SaveSetting<>("Shortcuts", "player", "key_shortcuts.json", ArrayList::new,
            new TypeToken<List<Shortcut>>() {}.getType(),
            new GsonBuilder()
                    .registerTypeHierarchyAdapter(Shortcut.class, (JsonDeserializer<Shortcut>) (json, typeOfT, context) -> new Shortcut(json.getAsJsonObject()))
                    .registerTypeHierarchyAdapter(Shortcut.class, (JsonSerializer<Shortcut>) (src, typeOfT, context) -> src.serialize())
                    .setPrettyPrinting().create(),
            false, KeyShortcuts::load, null);

    public KeyShortcuts() {
        this.registerProperty(openShortcuts, data);
    }

    public static void add(Shortcut shortcut) {
        shortcut.getKeybind().register();
        data.getValue().add(shortcut);
        save();
    }

    private static void load() {
        data.getValue().forEach(s -> {
            if (s.isEnabled()) s.getKeybind().register();
        });
    }

    public static void save() {
        data.save();
    }
}
