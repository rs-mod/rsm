package com.ricedotwho.rsm.module.impl.player.keyshortcuts;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.module.api.settings.impl.SaveSetting;
import com.ricedotwho.rsm.ui.impl.other.KeyShortcutGui;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Getter
@ModuleInfo(aliases = "Key Shortcuts", id = "key-shortcuts", category = Category.PLAYER, alwaysDisabled = true)
public class KeyShortcuts extends Module {
    @SuppressWarnings("unused")
    private static final KeyShortcuts instance = new KeyShortcuts();
    private static KeyShortcutGui gui;

    private final ButtonSetting openShortcuts = new ButtonSetting("Open Shortcuts" , "Open", KeyShortcuts::open);

    public static SaveSetting<List<Shortcut>> getData() {
        return instance.data;
    }

    private final SaveSetting<List<Shortcut>> data = new SaveSetting<>("Shortcuts", "player", "key_shortcuts.json", ArrayList::new,
            new TypeToken<@NotNull List<Shortcut>>() {}.getType(),
            new GsonBuilder()
                    .registerTypeHierarchyAdapter(Shortcut.class, (JsonDeserializer<Shortcut>) (json, _, _) -> new Shortcut(json.getAsJsonObject()))
                    .registerTypeHierarchyAdapter(Shortcut.class, (JsonSerializer<Shortcut>) (src, _, _) -> src.serialize())
                    .setPrettyPrinting().create(),
            false, KeyShortcuts::load);

    public static void open() {
        mc.player.closeContainer();
        mc.execute(() -> mc.gui.setScreen(gui));
    }

    public static void add(Shortcut shortcut) {
        shortcut.getKeybind().register();
        instance.data.getValue().add(shortcut);
        save();
    }

    private static void load() {
        instance.data.getValue().forEach(s -> {
            if (s.isEnabled()) s.getKeybind().register();
        });
        gui = new KeyShortcutGui(instance.data.getValue());
    }

    public static void save() {
        instance.data.save();
    }
}
