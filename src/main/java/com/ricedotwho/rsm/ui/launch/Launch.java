package com.ricedotwho.rsm.ui.launch;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.addon.AddonLoader;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandManager;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.api.ComponentManager;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.RSMGuiEditor;
import com.ricedotwho.rsm.utils.ConfigUtils;
import com.ricedotwho.rsm.module.Module;

import java.util.ArrayList;
import java.util.List;


public class Launch {
    private static final List<Class<? extends Module>> modules = new ArrayList<>();
    private static final List<Class<? extends Command>> command = new ArrayList<>();
    private static final List<Class<? extends ModComponent>> components = new ArrayList<>();

    public static void addModules(List<Class<? extends Module>> list) {
        modules.addAll(list);
    }

    private static List<Module> initModules() {
        List<Module> list = new ArrayList<>();
        try {
            for (Class<? extends Module> c : modules) {
                list.add(c.newInstance());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private static List<Command> initCommands() {
        List<Command> list = new ArrayList<>();
        try {
            for (Class<? extends Command> c : command) {
                list.add(c.newInstance());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private static List<ModComponent> initComponents() {
        List<ModComponent> list = new ArrayList<>();
        try {
            for (Class<? extends ModComponent> c : components) {
                list.add(c.newInstance());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public static void addCommands(List<Class<? extends Command>> list) {
        command.addAll(list);
    }
    public static void addComponents(List<Class<? extends ModComponent>> list) {
        components.addAll(list);
    }

    public static void start() {
        RSM rsm = RSM.getInstance();

        rsm.setEventBus(new EventBus());

        // modules
        ModuleManager moduleManager = new ModuleManager();
        moduleManager.put(initModules());

        moduleManager.getModules().forEach(ConfigUtils::loadConfig);

        rsm.getEventBus().register(moduleManager);
        rsm.setModuleManager(moduleManager);

        // Commands
        CommandManager commandManager = new CommandManager();
        commandManager.put(initCommands());

        rsm.getEventBus().register(commandManager);
        rsm.setCommandManager(commandManager);

        // Components
        ComponentManager componentManager = new ComponentManager();

        componentManager.put(initComponents());

        rsm.getEventBus().register(componentManager);
        rsm.setComponentManager(componentManager);

        // addons
        AddonLoader addonLoader = new AddonLoader();
        rsm.setAddonLoader(addonLoader);
        addonLoader.load(false);
        addonLoader.loadMixinUser();

        // Config
        RSMConfig gui = new RSMConfig();
        RSMGuiEditor guiEditor = new RSMGuiEditor();

        gui.init();

        rsm.setConfigGui(gui);
        rsm.setGUIEditor(guiEditor);

        Runtime.getRuntime().addShutdownHook(new Thread(Launch::end));
    }
    public static void end() {
        ConfigUtils.saveConfig();
    }
}
