package com.ricedotwho.rsm.core;

import com.ricedotwho.rsm.addon.AddonLoader;
import com.ricedotwho.rsm.command.api.CommandManager;
import com.ricedotwho.rsm.event.api.Scheduler;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.module.api.GeneratedModuleList;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.packet.clientbound.ClientboundZeroHello;
import com.ricedotwho.rsm.ui.api.Gui;
import com.ricedotwho.rsm.ui.api.UiElement;
import com.ricedotwho.rsm.ui.impl.clickgui.ClickGui;
import com.ricedotwho.rsm.ui.old.RSMGuiEditor;
import com.ricedotwho.rsm.ui.old.chathider.ChatHiderGui;
import com.ricedotwho.rsm.ui.old.keyshortcuts.KeyShortcutGui;
import com.ricedotwho.rsm.ui.old.visualwords.VisualWordGui;
import com.ricedotwho.rsm.utils.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

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
    private ModuleManager moduleManager = new ModuleManager();

    @Getter
    private ClickGui clickGui;

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

        moduleManager.init(GeneratedModuleList.modules);
        for (Class<?> clazz : GeneratedInitList.initClasses) {
            initClass(clazz);
        }

        registerPackets();

        registerAll(moduleManager);

        Scheduler.schedule(TickEvent.Start.class, this::onFirstTick);

        RSM.getLogger().info("foodaholic7492657");
        ClientLifecycleEvents.CLIENT_STOPPING.register((_) -> freeExternalMemory());
    }

    public void freeExternalMemory() {
        moduleManager.saveModules();
        for (UiElement popup : Gui.getPopups()) {
            try {
                popup.close();
            } catch (Exception e) {
                System.out.println("Failed to close popup " + popup.getClass().getSimpleName() + ": " + e);
            }
        }
        try {
            clickGui.close();
        } catch (Throwable e) {
            System.out.println("Failed to close ClickGui: " + e);
        }
        try {
            UniversalSettings.close();
        } catch (Exception e) {
            logger.info("Failed to close UniversalSettings: {}", String.valueOf(e));
        }
        //UiElement.printLeaked();
    }


    private void onFirstTick() {
        logger.info("Fisting atm! I LOVE FISTING");
        clickGui = new ClickGui(moduleManager, "RSM");
        UniversalSettings.setClickGui(clickGui);
        logger.info("ClickGui is null: {}", clickGui == null);
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

    @SuppressWarnings("UnstableApiUsage")
    private void registerPackets() {
        registerPayload(PayloadTypeRegistryImpl.CLIENTBOUND_PLAY, ClientboundZeroHello.TYPE, ClientboundZeroHello.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ClientboundZeroHello.TYPE, (_, _) -> zero = true);
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> zero = false);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static <T extends CustomPacketPayload> void registerPayload(PayloadTypeRegistryImpl<?> registry, CustomPacketPayload.Type<@NotNull T> type, StreamCodec<@NotNull FriendlyByteBuf, @NotNull T> codec) {
        // aw entry isnt valid I guess
        try {
            registry.register(type, codec);
        } catch (IllegalArgumentException e) {
            RSM.getLogger().warn("{} may already be registered!", type.id(), e);
        }
    }

    private void registerAll(ModuleManager moduleManager) {
        Launch.addCommands(GeneratedCommandList.commands);
        Launch.start(moduleManager);
    }

    public static String getName() {
        return "RSM";
    }
}