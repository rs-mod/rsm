package com.ricedotwho.rsm.module.dungeon;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.DungeonEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;

@Getter
@ModuleInfo(aliases = "ThreeWeirdos", id = "ThreeWeirdos", category = Category.DUNGEONS)
public class ThreeWeirdos extends Module {
    private final BooleanSetting enable = new BooleanSetting("Solver Enable", false, () -> true);
    static boolean inRoom;
    static boolean answerFound;

    public final HashSet<String> correctAnswers = new HashSet<>();
    private String correctWeirdo = null;

    @Getter
    public AABB chestPos;

    public ThreeWeirdos() {
        this.registerProperty(
                enable
        );
        correctAnswers.add("The reward is not in my chest!");
        correctAnswers.add("At least one of them is lying, and the reward is not in");
        correctAnswers.add("My chest doesn't have the reward we are all telling the truth.");
        correctAnswers.add("My chest has the reward and I'm telling the truth!");
        correctAnswers.add("The reward isn't in any of our chests.");
        correctAnswers.add("Both of them are telling the truth. Also,");
    }

    @SubscribeEvent
    public void onDungeonEvent(DungeonEvent.ChangeRoom event){
        ClientLevel level = Minecraft.getInstance().level;
        if(event.unique == null || level == null) return;
        inRoom = event.room.getCore() == -2056613688;
    }


    @SubscribeEvent
    public void findWeirdos(Render3DEvent.Extract event) {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null) return;
        if (!inRoom) return;

        AABB searchBox = player.getBoundingBox().inflate(16);
        List<Entity> entities = level.getEntities(null, searchBox);

        for (Entity entity : entities) {
            String name = entity.getName().getString();
            if (entity instanceof ArmorStand) {
                if (answerFound && correctWeirdo != null && name.contains(correctWeirdo)) {
                    BlockPos armorPos = entity.blockPosition();
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                BlockPos checkPos = armorPos.offset(dx, dy, dz);
                                BlockState state = level.getBlockState(checkPos);

                                if (state.getBlock() instanceof ChestBlock) {
                                    chestPos = new AABB(checkPos);
                                    Renderer3D.addTask(new OutlineBox(chestPos, Colour.GREEN, false));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onCorrectMsg(ChatEvent event){
        if (!inRoom) return;
        String unformatted = StringUtil.stripColor(event.getMessage().getString());

        boolean matched = correctAnswers.stream().anyMatch(unformatted::contains);
        if (matched) {
            answerFound = true;
            String cleaned = unformatted.replaceAll("\\[NPC]\\s*", "");
            String[] parts = cleaned.split(":");
            if (parts.length > 0) {
                correctWeirdo = parts[0].trim();
            }
            ChatUtils.chat("Correct weirdo: " + correctWeirdo);
        }
    }
}
