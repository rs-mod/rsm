package com.ricedotwho.rsm;

import com.ricedotwho.rsm.addon.AddonLoader;
import com.ricedotwho.rsm.command.Command;
import com.ricedotwho.rsm.command.api.CommandInfo;
import com.ricedotwho.rsm.command.api.CommandManager;
import com.ricedotwho.rsm.command.fabric.FabricCommands;
import com.ricedotwho.rsm.component.api.ComponentManager;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.event.api.EventBus;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.ModuleManager;
import com.ricedotwho.rsm.ui.chathider.ChatHiderGui;
import com.ricedotwho.rsm.ui.clickgui.RSMConfig;
import com.ricedotwho.rsm.ui.clickgui.RSMGuiEditor;
import com.ricedotwho.rsm.ui.keyshortcuts.KeyShortcutGui;
import com.ricedotwho.rsm.ui.launch.Launch;
import com.ricedotwho.rsm.ui.visualwords.VisualWordGui;
import com.ricedotwho.rsm.utils.CustomSounds;
import com.ricedotwho.rsm.utils.EtherUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
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
    private static final MutableComponent prefix = Component.empty()
            .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal("R").withColor(0x2E8343))
            .append(Component.literal("S").withColor(0x29A84F))
            .append(Component.literal("M").withColor(0x25CD5C))
            .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY));

//    private final List<Class<? extends Module>> MODULES = Arrays.asList(
//            ClickGUI.class,
//            NullBinds.class,
//            Ether.class,
//            Puzzles.class,
//            HidePlayers.class,
//            Trail.class,
//            Abilities.class,
//            ModuleList.class,
//            Jesus.class,
//            ManaStar.class,
//            TerminalSolver.class,
//            ChestHitFix.class,
//            P3Qol.class,
//            VisualWords.class,
//            OpSec.class,
//            Hud.class,
//            ImageHud.class,
//            KeyShortcuts.class,
//            PosMsg.class,
//            SimonSays.class,
//            Chat.class,
//            DungeonWaypoint.class,
//            MaskStatus.class,
//            EquipmentHelper.class,
//            ItemModifier.class
//    );

//    private final List<Class<? extends Command>> COMMANDS = Arrays.asList(
//            ConfigCommand.class,
//            CopyCommand.class,
//            OpenGuiCommand.class,
//            OpenGuiEditCommand.class,
//            AddonCommand.class,
//            DevCommand.class,
//            VisualWordCommand.class,
//            ItemModifierCommand.class,
//            ImageHudCommand.class,
//            KeyShortcutCommand.class,
//            PosMsgCommand.class,
//            ToggleCommand.class,
//            ChatHiderCommand.class,
//            DungeonWaypointCommand.class,
//            EquipmentHelperCommand.class
//    );

//    private final List<Class<? extends ModComponent>> COMPONENTS = Arrays.asList(
//            Scheduler.class,
//            KeybindComponent.class,
//            Timer.class,
//            NotificationComponent.class,
//            EventComponent.class,
//            Location.class,
//            Map.class,
//            Dungeon.class,
//            Renderer3D.class,
//            CameraHandler.class,
//            ClientRotationHandler.class,
//            SbStatTracker.class,
//            SwapManager.class,
//            Terminals.class,
//            Ping.class,
//            Sneak.class,
//            NoRotateManager.class
//    );

    private <T> List<Class<? extends T>> getAllFromPath(String path, Class<? extends Annotation> annotation, Class<T> type) {
        try (ScanResult result = new ClassGraph()
                .acceptPackages("com.ricedotwho.rsm" + path)
                .enableAnnotationInfo()
                .scan()) {
            // noinspection unchecked
            return (List<Class<? extends T>>) (List<?>) result.getClassesWithAnnotation(annotation).loadClasses(type);
        }
    }

    private List<Class<? extends ModComponent>> getComponents() {
        try (ScanResult result = new ClassGraph()
                .acceptPackages("com.ricedotwho.rsm.component.impl")
                .enableAnnotationInfo()
                .enableMethodInfo()
                .scan()) {
            // noinspection unchecked
            return (List<Class<? extends ModComponent>>) (List<?>) result
                    .getSubclasses(ModComponent.class)
                    .filter(info -> info.hasMethodAnnotation(SubscribeEvent.class))
                    .loadClasses(ModComponent.class);
        }
    }

    @Override
    public void onInitializeClient() {
        instance = this;

        EtherUtils.initIDs();
        SpecialGuiElementRegistry.register(context -> new NVGSpecialRenderer(context.vertexConsumers()));

        ScanUtils.init();

        registerAll();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> FabricCommands.register(dispatcher));

        CustomSounds.init();
    }

    private void registerAll() {
        Launch.addModules(getAllFromPath(".module.impl", ModuleInfo.class, Module.class));
        Launch.addCommands(getAllFromPath(".command.impl", CommandInfo.class, Command.class));
        Launch.addComponents(getComponents());

        Launch.start();
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

    public static String getName() {
        return "RSM";
    }
}