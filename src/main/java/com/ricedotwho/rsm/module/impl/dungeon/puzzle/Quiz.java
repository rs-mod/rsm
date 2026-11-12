package com.ricedotwho.rsm.module.impl.dungeon.puzzle;

import com.google.common.reflect.TypeToken;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.ColorSetting;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import com.ricedotwho.rsm.utils.StringUtils;
import com.ricedotwho.rsm.utils.api.HyApi;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Modified, original code Copyright (c) 2026, odtheking (https://github.com/odtheking/Odin/blob/main/src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/QuizSolver.kt)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */


@Getter
@SubModuleInfo(name = "Quiz", alwaysDisabled = false)
public class Quiz extends SubModule<Puzzles> {
    private static final String ANSWERS = "https://raw.githubusercontent.com/rs-mod/rsm/refs/heads/main/src/main/resources/assets/rsm/quiz_answers.json";
    private static final Pattern ANSWER_PATTERN = Pattern.compile("^§6 (.) §a(.*)$");
    private static Map<String, List<String>> allAnswers = null;
    private final ColorSetting color = new ColorSetting("Fill", Color.GREEN.withAlpha(100f));
    protected final List<Answer> options = List.of(new Answer(null, null, false), new Answer(null, null, false), new Answer(null, null, false));
    private List<String> answers = null;

    public Quiz(Puzzles puzzles) {
        super(puzzles);

        allAnswers = FileUtils.getGson().fromJson(HyApi.simpleGet(ANSWERS), new TypeToken<@NotNull Map<String, List<String>>>(){}.getType());
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

        if (StringUtils.startsWithAny(msg.trim(),"ⓐ", "ⓑ", "ⓒ") && answers != null) {;
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
            var room = event.getUnique();

            options.getFirst().pos = room.getRealPositionFixed(new Vec3(5, 70, -9)).toBlockPos();
            options.getFirst().button = room.getRealPositionFixed(new Vec3(4, 70, -9)).toBlockPos();

            options.get(1).pos = room.getRealPositionFixed(new Vec3(0, 70, -6)).toBlockPos();
            options.get(1).button = room.getRealPositionFixed(new Vec3(0, 70, -7)).toBlockPos();

            options.get(2).pos = room.getRealPositionFixed(new Vec3(-5, 70, -9)).toBlockPos();
            options.get(2).button = room.getRealPositionFixed(new Vec3(-4, 70, -9)).toBlockPos();
        }
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent.Extract event) {
        if (answers == null || answers.isEmpty()) return;
        options.forEach(a -> {
            if (!a.correct || a.pos == null) return;
            event.filled(a.pos, color.getValue(), false);
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
