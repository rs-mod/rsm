package com.ricedotwho.rsm.core;

import com.ricedotwho.rsm.addon.AddonLoader;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandManager;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import com.ricedotwho.rsm.ui.old.RSMGuiEditor;
import com.ricedotwho.rsm.ui.old.chathider.ChatHiderGui;
import com.ricedotwho.rsm.ui.old.keyshortcuts.KeyShortcutGui;
import com.ricedotwho.rsm.ui.old.visualwords.VisualWordGui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


public class Launch {
    private static final List<Class<? extends Command>> command = new ArrayList<>();

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

    public static void start(ModuleManager moduleManager) {
        RSM rsm = RSM.getInstance();

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

        RSMGuiEditor guiEditor = new RSMGuiEditor(moduleManager);
        KeyShortcutGui keyShortcutGui = new KeyShortcutGui();
        VisualWordGui visualWordGui = new VisualWordGui();
        ChatHiderGui chatHiderGui = new ChatHiderGui();


        rsm.setGUIEditor(guiEditor);
        rsm.setShortcutGui(keyShortcutGui);
        rsm.setVisualWordGui(visualWordGui);
        rsm.setChatHiderGui(chatHiderGui);

        Runtime.getRuntime().addShutdownHook(new Thread(Launch::end));
    }
    public static void end() {
        RSM.getInstance().getModuleManager().saveModules();
    }
}
