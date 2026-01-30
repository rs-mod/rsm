package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;

@Getter
@ModuleInfo(aliases = "DB", id = "DbHelper", category = Category.DUNGEONS)
public class DungeonbreakerHelper extends Module {

    private static final List<Block> BLACKLIST = Arrays.asList(
            Blocks.BARRIER,
            Blocks.COMMAND_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.BEDROCK,
            Blocks.PISTON,
            Blocks.PISTON_HEAD,
            Blocks.MOVING_PISTON,
            Blocks.STICKY_PISTON,
            Blocks.TNT,
            Blocks.END_PORTAL,
            Blocks.END_PORTAL_FRAME,
            Blocks.END_GATEWAY,
            Blocks.NETHER_PORTAL
    );

    private static final List<TagKey<Block>> TAGS = List.of(
            BlockTags.BUTTONS
    );

    private static final List<Class<?>> CLASSES = List.of(
            MenuProvider.class,
            LeverBlock.class,
            RedstoneTorchBlock.class,
            BushBlock.class,
            CauldronBlock.class,
            SkullBlock.class,
            ChestBlock.class,
            HopperBlock.class,
            BaseEntityBlock.class
    );

    private static int maxCharges = 20;
    private static int charges = 20;

    public DungeonbreakerHelper() {
        this.registerProperty(
                // todo: register settings
        );
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void reset() {
        charges = 20;
    }

//    @SubscribeEvent
//    public void onSetSlot(PacketEvent.Receive event) {
//        if (!(event.getPacket() instanceof ClientboundContainerSetSlotPacket packet) || !Loc.area.is(Island.Dungeon) || "DUNGEONBREAKER".equals(ItemUtils.getID(packet.getItem()))) return;
//        Pair<Integer, Integer> chargeData = ItemUtils.getDbCharges(packet.getItem());
//        if (chargeData.getFirst() == 0 && chargeData.getSecond() == 0) return;
//        charges = chargeData.getFirst();
//        maxCharges = chargeData.getSecond();
//    }

    public static boolean canInstantMine(BlockState state) {
        return !BLACKLIST.contains(state.getBlock())
                && TAGS.stream().noneMatch(state::is)
                && CLASSES.stream().noneMatch(c -> c.isInstance(state.getBlock()));
    }
}
