package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal;

import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.TickEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.managers.Terminals;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.module.api.settings.impl.*;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.Term;
import com.ricedotwho.rsm.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ModuleInfo(aliases = "Terminal Solver", id = "TerminalSolver", category = Category.DUNGEONS)
public class TerminalSolver extends Module {
    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    private static TerminalSolver instance = new TerminalSolver();

    private final MultiBoolSetting terminals = new MultiBoolSetting("Terminals", List.of("Melody", "Order", "Panes", "Rubix", "Select", "Starts With"), List.of("Melody", "Order", "Panes", "Rubix", "Select", "Starts With"));

    // who up autoterming rn
    private final BooleanSetting blockAll = new BooleanSetting("Block All Clicks", false);

    private final NumberSetting<Integer> firstDelay = new NumberSetting<>("First Click", 0, 500, 400, 10);
    private final NumberSetting<Float> scale = new NumberSetting<>("Scale", 0.2f, 5f, 1f, 0.1f);
    private final EnumSetting<HideClicked> mode = new EnumSetting<>("Mode", HideClicked.HIDE_CLICKED);
    private final NumberSetting<Integer> clickDelay = new NumberSetting<>("Forced Delay", 0, 150, 120, 1);
    private final BooleanSetting canClick = new BooleanSetting("Can Click", false);
    private final NumberSetting<Integer> timeout = new NumberSetting<>("Timeout", 0, 1000, 500, 50);

    public enum HideClicked {
        NORMAL,
        HIDE_CLICKED,
        ZERO_PING,
        QUEUE
    }

    //private final NumberSetting forcedFirstClick = new NumberSetting("Forced Firstclick", 0, 500, 400, 10);

    private final BooleanSetting terminalTime = new BooleanSetting("Send terminal time", false);

    private final EnumSetSetting<ChatStats> stats = new EnumSetSetting<>("Chat Stats", ChatStats.class, List.of(ChatStats.PERSONAL_BEST), terminalTime::getValue);
    public enum ChatStats {
        PERSONAL_BEST,
        AVERAGE_CLICK,
        FIRST_CLICK,
        CPS
    }

    private final NumberSetting<Float> gap = new NumberSetting<>("Gap", 0f, 5f, 2f, 0.1f);

    private final BooleanSetting titles = new BooleanSetting("Render Titles", false);
    private final StringSetting orderTitle = new StringSetting("Order Title", "");
    private final StringSetting panesTitle = new StringSetting("Panes Title", "");
    private final StringSetting selectTitle = new StringSetting("Select Title", "");
    private final StringSetting rubixTitle = new StringSetting("Rubix Title", "");
    private final StringSetting startsTitle = new StringSetting("Starts With Title", "");
    private final StringSetting melodyTitle = new StringSetting("Melody Title", "");

    private final BooleanSetting lockRubix = new BooleanSetting("Lock Rubix", true);
    private final BooleanSetting orderNumbers = new BooleanSetting("Render order numbers", true);

    private final BooleanSetting melodyBlock = new BooleanSetting("Block melody clicks", false);
    private final BooleanSetting melodyEdges = new BooleanSetting("Allow Edges on melody", false);

    private final DefaultGroupSetting terminalColors = new DefaultGroupSetting("Colors", this);
    private final ColorSetting background = new ColorSetting("Background", Color.fromHSVA(0F, 0F, 12F, 217F));
    private final ColorSetting textColor = new ColorSetting("Text Color", Color.fromRGB(220, 220, 220));
    private final ColorSetting panesColor = new ColorSetting("Panes", Color.fromHSVA(144F, 76F, 56F,255F));
    private final ColorSetting rubix = new ColorSetting("Rubix", Color.fromHSVA(144F, 76F, 56F,255F));
    private final ColorSetting oppRubix = new ColorSetting("Opposite Rubix", Color.fromHSVA(184F, 76F, 56F, 255F));
    private final ColorSetting order = new ColorSetting("Order", Color.fromHSVA(144F, 76F, 56F,255F));
    private final ColorSetting order2 = new ColorSetting("Order 2", Color.fromHSVA(144F, 76F, 47F,128F));
    private final ColorSetting order3 = new ColorSetting("Order 3", Color.fromHSVA(145F, 77F, 40F,77F));
    private final ColorSetting startsWith = new ColorSetting("Starts With", Color.fromHSVA(144F, 76F, 56F,255F));
    private final ColorSetting select = new ColorSetting("Select", Color.fromHSVA(144F, 76F, 56F,255F));
    private final ColorSetting canClickColor = new ColorSetting("Can Click", Color.fromRGB(255, 192, 203));

    private final ColorSetting melodyColumn = new ColorSetting("Mel Column", Color.fromRGB(138,43,226));
    private final ColorSetting melodyRow = new ColorSetting("Mel Row", Color.fromRGB(0, 255, 0));
    private final ColorSetting melodyRowLine = new ColorSetting("Mel Row Line", Color.fromRGB(255, 255, 255));
    private final ColorSetting melodyClay = new ColorSetting("Mel Clay", Color.fromRGB(255, 0, 0));
    private final ColorSetting melodyClayCorrect = new ColorSetting("Mel Clay Correct", Color.fromRGB(255, 200, 0));

    private final SaveSetting<Map<TerminalType, Long>> personalBests = new SaveSetting<>("Personal Bests", "dungeon", "terminal_personal_bests.json", HashMap::new, new TypeToken<Map<TerminalType, Long>>(){}.getType());
    private final SaveSetting<Map<TerminalType, Long>> simPersonalBests = new SaveSetting<>("Sim Personal Bests", "dungeon", "terimsim_terminal_personal_bests.json", HashMap::new, new TypeToken<Map<TerminalType, Long>>(){}.getType());

    public TerminalSolver() {

        terminalColors.add(
                background,
                textColor,
                panesColor,
                rubix,
                oppRubix,
                order,
                order2,
                order3,
                startsWith,
                select,
                canClickColor,
                melodyColumn,
                melodyRow,
                melodyRowLine,
                melodyClay,
                melodyClayCorrect
        );

        if (personalBests.getValue().isEmpty()) {
            personalBests.getValue().put(TerminalType.PANES, 100_000L);
            personalBests.getValue().put(TerminalType.RUBIX, 100_000L);
            personalBests.getValue().put(TerminalType.ORDER, 100_000L);
            personalBests.getValue().put(TerminalType.STARTS_WITH, 100_000L);
            personalBests.getValue().put(TerminalType.SELECT, 100_000L);
            personalBests.getValue().put(TerminalType.MELODY, 100_000L);
        }

        if (simPersonalBests.getValue().isEmpty()) {
            simPersonalBests.getValue().put(TerminalType.PANES, 100_000L);
            simPersonalBests.getValue().put(TerminalType.RUBIX, 100_000L);
            simPersonalBests.getValue().put(TerminalType.ORDER, 100_000L);
            simPersonalBests.getValue().put(TerminalType.STARTS_WITH, 100_000L);
            simPersonalBests.getValue().put(TerminalType.SELECT, 100_000L);
            simPersonalBests.getValue().put(TerminalType.MELODY, 100_000L);
        }
    }

    protected boolean renderThis() {
        return Terminals.getCurrent() != null && !Terminals.isScreenCancelled() && Terminals.getCurrent().shouldRender() && mc.player != null;
    }

    public Term create(TerminalType type, String title) {
        return type.create(title);
    }

    @SubscribeEvent
    private void onTick(TickEvent.ClientStart event) {
        if (renderThis()) Terminals.getCurrent().update();
    }

    @SubscribeEvent
    private void onDraw(GuiEvent.Draw event) {
        if (!renderThis()) return;
        NVGSpecialRenderer.draw(event.getGfx(), 0, 0, event.getGfx().guiWidth(), event.getGfx().guiHeight(), () -> {
            // this is slightly delayed and might crash if the gui closes between the call and this runnable
            if (renderThis()) Terminals.getCurrent().setupRender();
        });
        event.setCancelled(true);
    }

    @SubscribeEvent
    private void onMouse(GuiEvent.Click event) {
        if (!renderThis()) return;
        Terminals.getCurrent().mouseClick(event.getInput().button() == 0 ? GLFW.GLFW_MOUSE_BUTTON_3 : event.getInput().button());
        event.setCancelled(true);
    }
}
