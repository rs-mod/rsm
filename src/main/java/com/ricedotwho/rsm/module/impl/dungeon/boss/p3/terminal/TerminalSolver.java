package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.Terminals;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.TerminalType;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.Term;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.FileUtils;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Getter
@ModuleInfo(aliases = "Terminal Solver", id = "TerminalSolver", category = Category.DUNGEONS)
public class TerminalSolver extends Module {

    @Getter private static final BooleanSetting melodyEnabled = new BooleanSetting("Melody", true);
    @Getter private static final BooleanSetting orderEnabled = new BooleanSetting("Order", true);
    @Getter private static final BooleanSetting panesEnabled = new BooleanSetting("Panes", true);
    @Getter private static final BooleanSetting rubixEnabled = new BooleanSetting("Rubix", true);
    @Getter private static final BooleanSetting selectEnabled = new BooleanSetting("Select", true);
    @Getter private static final BooleanSetting startsWithEnabled = new BooleanSetting("Starts With", true);

    // who up autoterming rn
    @Getter private static final BooleanSetting blockAll = new BooleanSetting("Block All Clicks", false);

    // Don't worry I hate my code too

    @Getter private static final NumberSetting firstDelay = new NumberSetting("First Click", 0, 500, 400, 10);
    @Getter private static final NumberSetting scale = new NumberSetting("Scale", 0.2, 5, 1, 0.1);
    @Getter private static final ModeSetting mode = new ModeSetting("Mode", "Hide Clicked", List.of("Normal", "Hide Clicked", "Zero Ping"));
    @Getter private static final NumberSetting clickDelay = new NumberSetting("Forced Delay", 100, 150, 120, 1);
    @Getter private static final BooleanSetting canClick = new BooleanSetting("Can Click", false);
    @Getter private static final NumberSetting timeout = new NumberSetting("Timeout", 0, 1000, 500, 50);

    private final NumberSetting forcedFirstClick = new NumberSetting("Forced Firstclick", 0, 500, 400, 10);

    @Getter private static final BooleanSetting terminalTime = new BooleanSetting("Send terminal time", false);

    @Getter private static final NumberSetting gap = new NumberSetting("Gap", 0, 5, 2, 0.1);

    @Getter private static final BooleanSetting titles = new BooleanSetting("Render Titles", false);
    @Getter private static final StringSetting orderTitle = new StringSetting("Order Title", "");
    @Getter private static final StringSetting panesTitle = new StringSetting("Panes Title", "");
    @Getter private static final StringSetting selectTitle = new StringSetting("Select Title", "");
    @Getter private static final StringSetting rubixTitle = new StringSetting("Rubix Title", "");
    @Getter private static final StringSetting startsTitle = new StringSetting("Starts With Title", "");
    @Getter private static final StringSetting melodyTitle = new StringSetting("Melody Title", "");

    @Getter private static final BooleanSetting lockRubix = new BooleanSetting("Lock Rubix", true);
    @Getter private static final BooleanSetting orderNumbers = new BooleanSetting("Render order numbers", true);

    @Getter private static final BooleanSetting melodyBlock = new BooleanSetting("Block melody clicks", false);
    //@Getter private static final BooleanSetting melodyMiddleClick = new BooleanSetting("Middle click melody", false);
    @Getter private static final BooleanSetting melodyEdges = new BooleanSetting("Allow Edges on melody", false);

    private final DefaultGroupSetting terminalColours = new DefaultGroupSetting("Colours", this);
    @Getter private static final ColourSetting background = new ColourSetting("Background", new Colour(0F, 0F, 12F, 217F));
    @Getter private static final ColourSetting textColour = new ColourSetting("Text Colour", new Colour(220, 220, 220));
    @Getter private static final ColourSetting panesColour = new ColourSetting("Panes", new Colour(144F, 76F, 56F,255F));
    @Getter private static final ColourSetting rubix = new ColourSetting("Rubix", new Colour(144F, 76F, 56F,255F));
    @Getter private static final ColourSetting oppRubix = new ColourSetting("Opposite Rubix", new Colour(184F, 76F, 56F, 255F));
    @Getter private static final ColourSetting order = new ColourSetting("Order", new Colour(144F, 76F, 56F,255F));
    @Getter private static final ColourSetting order2 = new ColourSetting("Order 2", new Colour(144F, 76F, 47F,128F));
    @Getter private static final ColourSetting order3 = new ColourSetting("Order 3", new Colour(145F, 77F, 40F,77F));
    @Getter private static final ColourSetting startsWith = new ColourSetting("Starts With", new Colour(144F, 76F, 56F,255F));
    @Getter private static final ColourSetting select = new ColourSetting("Select", new Colour(144F, 76F, 56F,255F));
    @Getter private static final ColourSetting canClickColour = new ColourSetting("Can Click", new Colour(255, 192, 203));

    @Getter private static final ColourSetting melodyColumn = new ColourSetting("Mel Column", new Colour(138,43,226));
    @Getter private static final ColourSetting melodyRow = new ColourSetting("Mel Row", new Colour(0, 255, 0));
    @Getter private static final ColourSetting melodyRowLine = new ColourSetting("Mel Row Line", new Colour(255, 255, 255));
    @Getter private static final ColourSetting melodyClay = new ColourSetting("Mel Clay", new Colour(255, 0, 0));
    @Getter private static final ColourSetting melodyClayCorrect = new ColourSetting("Mel Clay Correct", new Colour(255, 200, 0));

    private static final File personalBestsFile = FileUtils.getSaveFileInCategory("dungeon", "terminal_personal_bests.json");
    public static Map<TerminalType, Long> personalBests = new HashMap<>();

    public TerminalSolver() {
        this.registerProperty(
                melodyEnabled, orderEnabled, panesEnabled, rubixEnabled, selectEnabled, startsWithEnabled,
                blockAll,
                firstDelay,
                scale,
                mode,
                clickDelay,
                canClick,
                timeout,
                titles,
                orderTitle,
                panesTitle,
                selectTitle,
                rubixTitle,
                startsTitle,
                lockRubix,
                orderNumbers,
                melodyBlock,
                //melodyMiddleClick,
                melodyEdges,
                forcedFirstClick,
                terminalTime,
                gap,
                terminalColours
        );

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

        loadPersonalBests();
    }

    private boolean renderThis() {
        return Terminals.getCurrent() != null && Terminals.getCurrent().shouldRender();
    }

    // no questions
    public static Term createTerm(TerminalType type, String title) {
        TerminalSolver solver = RSM.getModule(TerminalSolver.class);
        return solver == null ? null : solver.create(type, title);
    }

    public Term create(TerminalType type, String title) {
        return type.create(title);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        if (renderThis()) Terminals.getCurrent().update();
    }

    @SubscribeEvent
    public void onDraw(GuiEvent.Draw event) {
        if (renderThis()) event.setCancelled(true);
    }

    @SubscribeEvent
    public void onDrawBg(GuiEvent.DrawBackground event) {
        if (!renderThis()) return;
        NVGSpecialRenderer.draw(event.getGfx(), 0, 0, event.getGfx().guiWidth(), event.getGfx().guiHeight(), () -> {
            // this is slightly delayed and might crash if the gui closes between the call and this runnable
            if (renderThis()) Terminals.getCurrent().setupRender();
        });
        event.setCancelled(true);
    }

    @SubscribeEvent
    public void onKey(GuiEvent.Key event) {
        if (!renderThis() || !mc.options.keyDrop.matches(event.getInput())) return;
        Terminals.getCurrent().mouseClick(event.getInput().hasControlDown() ? GLFW.GLFW_MOUSE_BUTTON_2 : GLFW.GLFW_MOUSE_BUTTON_3);
        event.setCancelled(true);
    }

    @SubscribeEvent
    public void onMouse(GuiEvent.Click event) {
        if (!renderThis()) return;
        Terminals.getCurrent().mouseClick(event.getInput().button() == 0 ? GLFW.GLFW_MOUSE_BUTTON_3 : event.getInput().button());
        event.setCancelled(true);
    }

    public void loadPersonalBests() {
        if(FileUtils.checkDir(personalBestsFile, new HashSet<>())) {
            try {
                Map<TerminalType, Long> temp;
                try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(personalBestsFile.toPath()), StandardCharsets.UTF_8)) {
                    Gson gson = new Gson();
                    temp = gson.fromJson(reader, new TypeToken<Map<TerminalType, Long>>(){}.getType());

                    personalBests = temp;
                }
            } catch (IOException | JsonSyntaxException | JsonIOException e) {
                throw new RuntimeException(e);
            }
        } else {
            personalBests.put(TerminalType.PANES, 100_000L);
            personalBests.put(TerminalType.RUBIX, 100_000L);
            personalBests.put(TerminalType.ORDER, 100_000L);
            personalBests.put(TerminalType.STARTS_WITH, 100_000L);
            personalBests.put(TerminalType.SELECT, 100_000L);
            personalBests.put(TerminalType.MELODY, 100_000L);
            savePersonalBests();
        }
    }

    public static void savePersonalBests() {
        FileUtils.writeJson(personalBests, personalBestsFile);
    }
}
