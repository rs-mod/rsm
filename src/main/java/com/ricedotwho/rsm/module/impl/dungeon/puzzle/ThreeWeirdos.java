package com.ricedotwho.rsm.module.impl.dungeon.puzzle;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.module.api.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.ColorSetting;
import com.ricedotwho.rsm.module.api.settings.impl.EnumSetting;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.RenderMode;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Getter
@SubModuleInfo(name = "ThreeWeirdos", alwaysDisabled = false)
public class ThreeWeirdos extends SubModule<Puzzles> {
    private static final Pattern NPC = Pattern.compile("^\\[NPC] (\\w+): (.*)$");
    private final ColorSetting rightColor = new ColorSetting("Right Weirdo Color", Color.green);
    private final ColorSetting wrongColor = new ColorSetting("Wrong Weirdo Color", Color.red);
    private final EnumSetting<RenderMode> renderMode = new EnumSetting<>("Render Mode", RenderMode.FILLED_OUTLINE);

    private static final Set<String> CORRECT_ANSWERS = Set.of(
            "The reward is not in my chest!",
            "At least one of them is lying, and the reward is not in",
            "My chest doesn't have the reward. We are all telling the truth.",
            "My chest has the reward and I'm telling the truth!",
            "The reward isn't in any of our chests.",
            "Both of them are telling the truth. Also,"
    );

    private static final Set<String> INCORRECT_ANSWERS = Set.of(
            "One of us is telling the truth!",
            "They are both telling the truth. The reward isn't in",
            "We are all telling the truth!",
            "is telling the truth and the reward is in his chest",
            "My chest doesn't have the reward. At least one of the others is telling the truth!",
            "One of the others is lying",
            "They are both telling the truth, the reward is in",
            "They are both lying, the reward is in my chest!",
            "The reward is in my chest",
            "The reward is not in my chest. They are both lying",
            "is telling the truth",
            "My chest has the reward"
    );

    protected BlockPos correct = null;
    private final List<BlockPos> incorrect = new ArrayList<>();

    public ThreeWeirdos(Puzzles puzzles) {
        super(puzzles);
    }

    @SubscribeEvent
    private void onRoomEnter(DungeonEvent.ChangeRoom event) {
        reset();
    }

    @SubscribeEvent
    public void onEnterBoss(DungeonEvent.EnterBoss event) {
        reset();
    }

    @SubscribeEvent
    private void onChat(ChatEvent.Chat event) {
        if (mc.level == null || Dungeon.current() == null || !"Three Weirdos".equals(Dungeon.current().getName())) return;
        var match = NPC.matcher(event.getString());
        if (!match.find()) return;

        var name = match.group(1);
        var message = match.group(2);

        if (CORRECT_ANSWERS.stream().noneMatch(message::contains)) {
            if (INCORRECT_ANSWERS.stream().anyMatch(message::contains)) {
                var pos = getPosition(name);
                if (pos != null) incorrect.add(pos);
            }
            return;
        }

        correct = getPosition(name);
    }

    private BlockPos getPosition(String name) {
        Entity correctEntity = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false).filter(e -> e.getName().getString().contains(name)).findAny().orElse(null);
        if (correctEntity == null) {
            ChatUtils.chat("Couldn't find correct entity");
            return null;
        }
        BlockPos relEntityPos = Dungeon.current().getRelativePosition(correctEntity.position()).toBlockPos();

        return Dungeon.current().getRealPosition(relEntityPos.east());
    }

    @SubscribeEvent
    private void onRender(Render3DEvent.Extract event) {
        if (!Location.getArea().is(Island.Dungeon) || Dungeon.current() == null || !"Three Weirdos".equals(Dungeon.current().getName())) return;
        if (correct != null) {
            draw(event, correct, rightColor.getValue());
        }
        for (var pos : incorrect) {
            draw(event, pos, wrongColor.getValue());
        }
    }

    private void draw(Render3DEvent.Extract event, BlockPos pos, Color color) {
        switch (renderMode.getValue()) {
            case RenderMode.FILLED->  event.filledShape(pos, color, false);
            case RenderMode.OUTLINE ->  event.outlineShape(pos, color, false);
            default -> event.filledOutlineShape(pos, color.withAlpha(0.2F), color, false);
        }
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        reset();
    }

    @Override
    protected void reset() {
        super.reset();
        correct = null;
        incorrect.clear();
    }
}
