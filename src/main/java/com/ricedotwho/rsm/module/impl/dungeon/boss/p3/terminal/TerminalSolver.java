package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal;

import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.managers.Terminals;
import com.ricedotwho.rsm.managers.dungeon.TerminalType;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.Term;
import com.ricedotwho.rsm.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.type.Colour;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
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

    private final NumberSetting firstDelay = new NumberSetting("First Click", 0, 500, 400, 10);
    private final NumberSetting scale = new NumberSetting("Scale", 0.2, 5, 1, 0.1);
    private final ModeSetting mode = new ModeSetting("Mode", "Hide Clicked", List.of("Normal", "Hide Clicked", "Zero Ping", "Queue"));
    private final NumberSetting clickDelay = new NumberSetting("Forced Delay", 0, 150, 120, 1);
    private final BooleanSetting canClick = new BooleanSetting("Can Click", false);
    private final NumberSetting timeout = new NumberSetting("Timeout", 0, 1000, 500, 50);

    //private final NumberSetting forcedFirstClick = new NumberSetting("Forced Firstclick", 0, 500, 400, 10);

    private final BooleanSetting terminalTime = new BooleanSetting("Send terminal time", false);
    private final MultiBoolSetting stats = new MultiBoolSetting("Chat Stats", List.of("Personal Best", "Average Click", "First Click", "CPS"), List.of("Personal Best"), terminalTime::getValue);

    private final NumberSetting gap = new NumberSetting("Gap", 0, 5, 2, 0.1);

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

    private final DefaultGroupSetting terminalColours = new DefaultGroupSetting("Colours", this);
    private final ColourSetting background = new ColourSetting("Background", new Colour(0F, 0F, 12F, 217F));
    private final ColourSetting textColour = new ColourSetting("Text Colour", new Colour(220, 220, 220));
    private final ColourSetting panesColour = new ColourSetting("Panes", new Colour(144F, 76F, 56F,255F));
    private final ColourSetting rubix = new ColourSetting("Rubix", new Colour(144F, 76F, 56F,255F));
    private final ColourSetting oppRubix = new ColourSetting("Opposite Rubix", new Colour(184F, 76F, 56F, 255F));
    private final ColourSetting order = new ColourSetting("Order", new Colour(144F, 76F, 56F,255F));
    private final ColourSetting order2 = new ColourSetting("Order 2", new Colour(144F, 76F, 47F,128F));
    private final ColourSetting order3 = new ColourSetting("Order 3", new Colour(145F, 77F, 40F,77F));
    private final ColourSetting startsWith = new ColourSetting("Starts With", new Colour(144F, 76F, 56F,255F));
    private final ColourSetting select = new ColourSetting("Select", new Colour(144F, 76F, 56F,255F));
    private final ColourSetting canClickColour = new ColourSetting("Can Click", new Colour(255, 192, 203));

    private final ColourSetting melodyColumn = new ColourSetting("Mel Column", new Colour(138,43,226));
    private final ColourSetting melodyRow = new ColourSetting("Mel Row", new Colour(0, 255, 0));
    private final ColourSetting melodyRowLine = new ColourSetting("Mel Row Line", new Colour(255, 255, 255));
    private final ColourSetting melodyClay = new ColourSetting("Mel Clay", new Colour(255, 0, 0));
    private final ColourSetting melodyClayCorrect = new ColourSetting("Mel Clay Correct", new Colour(255, 200, 0));

    private final SaveSetting<Map<TerminalType, Long>> personalBests = new SaveSetting<>("Personal Bests", "dungeon", "terminal_personal_bests.json", HashMap::new, new TypeToken<Map<TerminalType, Long>>(){}.getType());
    private final SaveSetting<Map<TerminalType, Long>> simPersonalBests = new SaveSetting<>("Sim Personal Bests", "dungeon", "terimsim_terminal_personal_bests.json", HashMap::new, new TypeToken<Map<TerminalType, Long>>(){}.getType());

    public TerminalSolver() {

        terminalColours.add(
                background,
                textColour,
                panesColour,
                rubix,
                oppRubix,
                order,
                order2,
                order3,
                startsWith,
                select,
                canClickColour,
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
    private void onTick(ClientTickEvent.Start event) {
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
