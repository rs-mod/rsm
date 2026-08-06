package com.ricedotwho.rsm.core;

import com.ricedotwho.rsm.GeneratedInitList;
import com.ricedotwho.rsm.addon.AddonLoader;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandManager;
import com.ricedotwho.rsm.command.impl.*;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.module.impl.dungeon.*;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.LeapGui;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.simonsays.SimonSays;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.P3Qol;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TerminalSolver;
import com.ricedotwho.rsm.module.impl.dungeon.posmsg.PosMsg;
import com.ricedotwho.rsm.module.impl.dungeon.puzzle.Puzzles;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.DungeonWaypoint;
import com.ricedotwho.rsm.module.impl.movement.Ether;
import com.ricedotwho.rsm.module.impl.movement.NullBinds;
import com.ricedotwho.rsm.module.impl.other.SphinxAnswer;
import com.ricedotwho.rsm.module.impl.player.*;
import com.ricedotwho.rsm.module.impl.player.keyshortcuts.KeyShortcuts;
import com.ricedotwho.rsm.module.impl.render.*;
import com.ricedotwho.rsm.module.impl.render.hud.Hud;
import com.ricedotwho.rsm.module.impl.render.itemmodifier.ItemModifier;
import com.ricedotwho.rsm.module.impl.render.opsec.OpSec;
import com.ricedotwho.rsm.module.impl.render.visualwords.VisualWords;
import com.ricedotwho.rsm.packet.clientbound.ClientboundZeroHello;
import com.ricedotwho.rsm.ui.chathider.ChatHiderGui;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.RSMGuiEditor;
import com.ricedotwho.rsm.ui.keyshortcuts.KeyShortcutGui;
import com.ricedotwho.rsm.ui.launch.Launch;
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
import java.util.Arrays;
import java.util.List;

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

    private final List<Class<? extends Module>> MODULES = Arrays.asList(
            ClickGUI.class,
            NullBinds.class,
            Ether.class,
            Puzzles.class,
            HidePlayers.class,
            Trail.class,
            Abilities.class,
            ModuleList.class,
            Jesus.class,
            ManaStar.class,
            TerminalSolver.class,
            ChestHitFix.class,
            P3Qol.class,
            VisualWords.class,
            OpSec.class,
            Hud.class,
            ImageHud.class,
            KeyShortcuts.class,
            PosMsg.class,
            SimonSays.class,
            Chat.class,
            DungeonWaypoint.class,
            MaskStatus.class,
            EquipmentHelper.class,
            ItemModifier.class,
            SphinxAnswer.class,
            ScreenTint.class,
            ProtectItem.class,
            SlotBinding.class,
            NoPlace.class,
            WorldBorderFix.class,
            LeapRotateFix.class,
            CrouchAnimation.class,
            Waypoints.class,
            PotionBag.class,
            //AutoKick.class,
            LeapGui.class,
            FullBright.class,
            DungeonBreaker.class,
            BreakingTexture.class,
            BarFix.class,
            SecretClicked.class
    );

    private final List<Class<? extends Command>> COMMANDS = Arrays.asList(
            ConfigCommand.class,
            CopyCommand.class,
            OpenGuiCommand.class,
            OpenGuiEditCommand.class,
            AddonCommand.class,
            DevCommand.class,
            VisualWordCommand.class,
            ItemModifierCommand.class,
            ImageHudCommand.class,
            KeyShortcutCommand.class,
            PosMsgCommand.class,
            ToggleCommand.class,
            ChatHiderCommand.class,
            DungeonWaypointCommand.class,
            EquipmentHelperCommand.class,
            EtherCommand.class,
            WaypointCommand.class,
            LeapOrderCommand.class,
            TermSimCommand.class,
            ProtectItemCommand.class
    );

    @Override
    public void onInitializeClient() {
        instance = this;

        registerAll();

        registerPackets();

        for (Class<?> clazz : GeneratedInitList.INSTANCE.getInitClasses()) {
            initClass(clazz);
        }

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
                throw new RuntimeException(e);
            }
        }
    }

    private void registerPackets() {
        PayloadTypeRegistry.clientboundPlay().register(ClientboundZeroHello.TYPE, ClientboundZeroHello.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ClientboundZeroHello.TYPE, (_, _) -> zero = true);
        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> zero = false);
    }

    private void registerAll() {
        Launch.addModules(MODULES);
        Launch.addCommands(COMMANDS);
        Launch.start();
    }

    public static <T extends Module> T getModule(Class<T> module) {
        if (instance == null || instance.getModuleManager() == null) return null;
        Module m = instance.getModuleManager().get(module);
        return module.cast(m);
    }

    public static String getName() {
        return "RSM";
    }
}