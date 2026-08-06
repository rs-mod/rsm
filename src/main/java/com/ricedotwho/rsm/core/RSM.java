package com.ricedotwho.rsm.core;

import com.ricedotwho.rsm.addon.AddonLoader;
import com.ricedotwho.rsm.command.api.CommandManager;
import com.ricedotwho.rsm.module.impl.dungeon.*;
import com.ricedotwho.rsm.module.impl.player.*;
import com.ricedotwho.rsm.module.impl.render.*;
import com.ricedotwho.rsm.packet.clientbound.ClientboundZeroHello;
import com.ricedotwho.rsm.ui.chathider.ChatHiderGui;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.RSMGuiEditor;
import com.ricedotwho.rsm.ui.keyshortcuts.KeyShortcutGui;
import com.ricedotwho.rsm.ui.visualwords.VisualWordGui;
import com.ricedotwho.rsm.utils.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;

@Getter
public class RSM implements ClientModInitializer {
    @Getter
    public static final Logger logger = LogManager.getLogger("rsm");
    @Getter
    private static RSM instance;

    @Setter
    @Getter
    private CommandManager commandManager;
    @Setter
    @Getter
    private RSMConfig configGui;
    @Setter
    @Getter
    private RSMGuiEditor GUIEditor;
    @Getter
    @Setter
    private AddonLoader addonLoader;
    @Setter
    @Getter
    private KeyShortcutGui shortcutGui;
    @Setter
    @Getter
    private VisualWordGui visualWordGui;
    @Setter
    @Getter
    private ChatHiderGui chatHiderGui;
    @Getter
    private static boolean zero = false;

    @Getter
    private static final MutableComponent prefix = Component.empty()
            .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("R").withColor(0x2E8343))
            .append(Component.literal("S").withColor(0x29A84F))
            .append(Component.literal("M").withColor(0x25CD5C))
            .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY));

    @Override
    public void onInitializeClient() {
        instance = this;
        for (Class<?> clazz : GeneratedInitList.INSTANCE.getInitClasses()) {
            initClass(clazz);
        }

        registerPackets();

        registerAll();

        RSM.getLogger().info("foodaholic7492657");
    }

    private void initClass(Class<?> clazz) {
        val singleton = ReflectionUtils.getSingleton(clazz);
        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            if (declaredMethod.getAnnotation(Init.class) == null) continue;
            val isStatic = ReflectionUtils.isStatic(declaredMethod);
            if (!isStatic && singleton == null) throw new IllegalArgumentException(clazz.getTypeName() + " function is annotated by @Init whilst not being static or the parent class isn't a singleton");
            val object = isStatic ? null : singleton;
            try {
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(object);
            } catch (Exception e) {
                throw new RuntimeException(clazz.getSimpleName(), e);
            }
        }
    }

    private void registerPackets() {
        PayloadTypeRegistry.clientboundPlay().register(ClientboundZeroHello.TYPE, ClientboundZeroHello.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ClientboundZeroHello.TYPE, (_, _) -> zero = true);
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> zero = false);
    }

    private void registerAll() {
        Launch.addCommands(GeneratedCommandList.INSTANCE.getCommands());
        Launch.start();
    }

    public static String getName() {
        return "RSM";
    }
}