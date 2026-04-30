package com.ricedotwho.rsm.module.impl.dungeon.puzzle;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.component.impl.map.map.Room;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledBox;
import com.ricedotwho.rsm.utils.render.render3d.type.FilledOutlineBox;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Getter
@SubModuleInfo(name = "ThreeWeirdos", alwaysDisabled = false)
public class ThreeWeirdos extends SubModule<Puzzles> {
    private static final ColourSetting rightColour = new ColourSetting("Right Weirdo Colour", Colour.green);

    private static final Set<String> CORRECT_ANSWERS = Set.of(
            "The reward is not in my chest!",
            "At least one of them is lying, and the reward is not in",
            "My chest doesn't have the reward. We are all telling the truth.",
            "My chest has the reward and I'm telling the truth!",
            "The reward isn't in any of our chests.",
            "Both of them are telling the truth. Also,"
    );

    protected BlockPos correct = null;
    protected static Room weirdoRoom = null;

    public ThreeWeirdos(Puzzles puzzles) {
        super(puzzles);
        this.registerProperty(
                rightColour
        );
    }

    @SubscribeEvent
    public void onRoomEnter(DungeonEvent.ChangeRoom event) {
        if (event.unique == null) return;

        resetWeirdos();

        if ("Three Weirdos".equals(event.unique.getName())) weirdoRoom = event.room;
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (weirdoRoom == null) return;

        if (!event.getMessage().getString().startsWith("§e[NPC] §c")) return;

        String withoutPrefix = event.getMessage().getString().substring(10);
        int colonIndex = withoutPrefix.indexOf(":");
        if (colonIndex == -1) return;

        String name = withoutPrefix.substring(0, colonIndex - 2).trim();
        String npcMessage = withoutPrefix.substring(colonIndex + 1).trim();

        if (!CORRECT_ANSWERS.stream().anyMatch(npcMessage::contains)) return;

        Entity correctEntity = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false).filter(e -> e.getName().getString().contains(name)).findAny().orElse(null);
        if (correctEntity == null) {
            ChatUtils.chat("Couldnt find correct entity");
            return;
        }
        BlockPos relEntityPos = weirdoRoom.getRelativePosition(new Pos(correctEntity.position())).asBlockPos();

        correct = weirdoRoom.getRealPosition(relEntityPos.east());
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Last event) {
        if (correct != null) Renderer3D.addTask(new FilledBox(correct, rightColour.getValue(), false));
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        resetWeirdos();
    }

    protected void resetWeirdos() {
        correct = null;
        weirdoRoom = null;
    }
}
