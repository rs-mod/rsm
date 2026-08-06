package com.ricedotwho.rsm.core;

import com.ricedotwho.rsm.addon.AddonLoader;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandManager;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.ui.chathider.ChatHiderGui;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.RSMGuiEditor;
import com.ricedotwho.rsm.ui.clickgui.api.SettingTypes;
import com.ricedotwho.rsm.ui.clickgui.impl.module.settings.impl.*;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.ui.keyshortcuts.KeyShortcutGui;
import com.ricedotwho.rsm.ui.visualwords.VisualWordGui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


public class Launch {
    private static final List<Class<? extends Module>> modules = new ArrayList<>();
    private static final List<Class<? extends Command>> command = new ArrayList<>();

    public static void addModules(List<Class<? extends Module>> list) {
        modules.addAll(list);
    }

    private static List<Module> initModules() {
        List<Module> list = new ArrayList<>();
        try {
            for (Class<? extends Module> c : modules) {
                list.add(c.getDeclaredConstructor().newInstance());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private static List<Command> initCommands() {
        List<Command> list = new ArrayList<>();
        try {
            for (Class<? extends Command> c : command) {
                list.add(c.getDeclaredConstructor().newInstance());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public static void addCommands(List<Class<? extends Command>> list) {
        command.addAll(list);
    }

    @SuppressWarnings("unchecked")
    public static void start() {
        RSM rsm = RSM.getInstance();

        // register config settings
        SettingTypes.register(BooleanSetting.class, BooleanValueComponent.class);
        SettingTypes.register(ModeSetting.class, ModeValueComponent.class);
        SettingTypes.register(MultiBoolSetting.class, MultiBoolValueComponent.class);
        SettingTypes.register(NumberSetting.class, NumberValueComponent.class);
        SettingTypes.register(StringSetting.class, StringValueComponent.class);
        SettingTypes.register(KeybindSetting.class, KeybindValueComponent.class);
        SettingTypes.register(ButtonSetting.class, ButtonValueComponent.class);
        SettingTypes.register(ColourSetting.class, ColourValueComponent.class);
        SettingTypes.register(DragSetting.class, EmptyValueComponent.class);
        SettingTypes.register((Class<? extends Setting<?>>) (Class<?>)  SaveSetting.class, SaveValueComponent.class);

        // Commands
        CommandManager commandManager = new CommandManager();
        commandManager.put(initCommands());

        EventBus.register(commandManager);
        rsm.setCommandManager(commandManager);
        // addons
        AddonLoader addonLoader = new AddonLoader();
        addonLoader.load(false);
        addonLoader.loadMixinUser();

        rsm.setAddonLoader(addonLoader);

        // Config
        RSMConfig gui = new RSMConfig();
        RSMGuiEditor guiEditor = new RSMGuiEditor();
        KeyShortcutGui keyShortcutGui = new KeyShortcutGui();
        VisualWordGui visualWordGui = new VisualWordGui();
        ChatHiderGui chatHiderGui = new ChatHiderGui();

        gui.init();

        rsm.setConfigGui(gui);
        rsm.setGUIEditor(guiEditor);
        rsm.setShortcutGui(keyShortcutGui);
        rsm.setVisualWordGui(visualWordGui);
        rsm.setChatHiderGui(chatHiderGui);

        Runtime.getRuntime().addShutdownHook(new Thread(Launch::end));
    }
    public static void end() {
        ModuleManager.saveModules();
    }
}
