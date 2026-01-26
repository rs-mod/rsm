package com.ricedotwho.rsm;

import com.ricedotwho.rsm.addon.AddonLoader;
import com.ricedotwho.rsm.command.api.CommandManager;
import com.ricedotwho.rsm.component.ComponentManager;
import com.ricedotwho.rsm.component.ModComponent;
import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.event.EventBus;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.RSMGuiEditor;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.ricedotwho.rsm.module.Module;

import java.awt.*;

@Getter
public class RSM implements ClientModInitializer {
    @Getter
    public static final Logger logger = LogManager.getLogger("rsm");
    @Getter
    private static RSM instance;
    @Setter
    @Getter
    private EventBus eventBus;
    @Setter
    @Getter
    private ModuleManager moduleManager;
    @Setter
    @Getter
    private CommandManager commandManager;
    @Setter
    @Getter
    private ComponentManager componentManager;
    @Setter
    @Getter
    private RSMConfig configGui;
    @Setter
    @Getter
    private RSMGuiEditor GUIEditor;
    @Getter
    @Setter
    private AddonLoader addonLoader;
    @Getter
    private static final MutableComponent prefix = Component.literal("§8[§2RSM§8] §r");

	@Override
	public void onInitializeClient() {
        instance = this;
	}

    public static <T extends Module> T getModule(Class<T> module) {
        if (instance == null || instance.getModuleManager() == null) return null;
        Module m = instance.getModuleManager().get(module);
        return module.cast(m);
    }

    public static <T extends ModComponent> T getComponent(Class<T> component) {
        if (instance == null || instance.getModuleManager() == null) return null;
        ModComponent c = instance.getComponentManager().get(component);
        return component.cast(c);
    }
}