package com.ricedotwho.rsm.module.impl.dungeon.puzzle;

import com.google.common.reflect.TypeToken;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.component.impl.map.map.RoomData;
import com.ricedotwho.rsm.component.impl.map.utils.RoomUtils;
import com.ricedotwho.rsm.component.impl.map.utils.ScanUtils;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.Secret;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import com.ricedotwho.rsm.utils.StringUtils;
import com.ricedotwho.rsm.utils.api.HyApi;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledBox;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineBox;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// literally just copied [Odin](https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/QuizSolver.kt) but it updates from online thing

@Getter
@SubModuleInfo(name = "Quiz", alwaysDisabled = false)
public class Quiz extends SubModule<Puzzles> {
    private static final String ANSWERS = "https://raw.githubusercontent.com/rs-mod/rsm/refs/heads/main/src/main/resources/assets/rsm/quiz_answers.json";
    private static final Pattern ANSWER_PATTERN = Pattern.compile("^§6 (.) §a(.*)$");
    private static Map<String, List<String>> allAnswers = null;
    private final ColourSetting colour = new ColourSetting("Fill", Colour.GREEN.alpha(100f));
    protected final List<Answer> options = List.of(new Answer(null, null, false), new Answer(null, null, false), new Answer(null, null, false));
    private List<String> answers = null;

    public Quiz(Puzzles puzzles) {
        super(puzzles);
        this.registerProperty(
                colour
        );

        allAnswers = FileUtils.getGson().fromJson(new HyApi().simpleGet(ANSWERS), new TypeToken<@NotNull Map<String, List<String>>>(){}.getType());
        if (allAnswers == null) {
            allAnswers = FileUtils.getGson().fromJson(new InputStreamReader(Objects.requireNonNull(Quiz.class.getResourceAsStream("/assets/rsm/quiz_answers.json"))), new TypeToken<@NotNull Map<String, List<String>>>(){}.getType());
        }
    }

    @Override
    public void reset() {
        options.forEach(Answer::reset);
        answers = null;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.Dungeon)) return;
        String msg = event.getString();

        if (msg.startsWith("[STATUE] Oruo the Omniscient: ") && msg.endsWith("correctly!")) {
            if (msg.contains("answered the final question")) {
                reset();
                return;
            }

            if (msg.contains("answered Question #")) options.forEach(a -> a.correct = false);
        }

        if (StringUtils.startsWithAny(msg.trim(),"ⓐ", "ⓑ", "ⓒ") && answers != null) {
            RSM.getLogger().info(event.getMessage());
            if (answers.stream().anyMatch(msg::endsWith)) {
                switch (msg.trim().charAt(0)) {
                    case 'ⓐ' -> options.getFirst().correct = true;
                    case 'ⓑ' -> options.get(1).correct = true;
                    case 'ⓒ' -> options.get(2).correct = true;
                }
            } else if (event.getMessage() instanceof Component comp && comp.getSiblings().size() == 1) {
                Component sib = comp.getSiblings().getFirst();
                Matcher matcher = ANSWER_PATTERN.matcher(sib.getString());
                if (matcher.find()) {
                    comp.getSiblings().remove(sib);
                    MutableComponent replacement = Component.literal("§6 " + matcher.group(1) + " §c" + matcher.group(2));
                    replacement.setStyle(sib.getStyle());
                    comp.getSiblings().add(replacement);
                }
            }
            if (msg.trim().charAt(0) == 'ⓒ' && options.stream().noneMatch(a -> a.correct)) {
                ChatUtils.chat("Unable to find quiz correct answer!");
            }
        }

        if ("What SkyBlock year is it?".equals(msg.trim())) {
            answers = List.of("Year " + ((int) (((System.currentTimeMillis() / 1000) - 1560276000) / 446400) + 1));
        } else {
            Map.Entry<String, List<String>> entry = allAnswers.entrySet().stream().filter(e  -> msg.contains(e.getKey())).findFirst().orElse(null);
            if (entry != null) {
                answers = entry.getValue();
            }
        }
    }

    @SubscribeEvent
    public void onRoomScanned(DungeonEvent.RoomScanned event) {
        if (event.getUnique().getName().equals("Quiz")) {
            Room room = event.getUnique().getMainRoom();

            options.getFirst().pos = RoomUtils.getRealPositionFixed(new Pos(5, 70, -9), room).asBlockPos();
            options.getFirst().button = RoomUtils.getRealPositionFixed(new Pos(4, 70, -9), room).asBlockPos();

            options.get(1).pos = RoomUtils.getRealPositionFixed(new Pos(0, 70, -6), room).asBlockPos();
            options.get(1).button = RoomUtils.getRealPositionFixed(new Pos(0, 70, -7), room).asBlockPos();

            options.get(2).pos = RoomUtils.getRealPositionFixed(new Pos(-5, 70, -9), room).asBlockPos();
            options.get(2).button = RoomUtils.getRealPositionFixed(new Pos(-4, 70, -9), room).asBlockPos();
        }
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent.Extract event) {
        if (answers == null || answers.isEmpty()) return;
        options.forEach(a -> {
            if (!a.correct) return;
            Renderer3D.addTask(new FilledBox(a.pos, colour.getValue(), false));
        });
    }

    @AllArgsConstructor
    protected static class Answer {
        public BlockPos pos;
        public BlockPos button;
        public boolean correct;

        public void reset() {
            this.pos = null;
            this.button = null;
            this.correct = false;
        }
    }
}
