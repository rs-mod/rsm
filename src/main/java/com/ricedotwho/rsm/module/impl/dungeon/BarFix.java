package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

@Getter
@ModuleInfo(aliases = "Bar Fix", id = "bar-fix", category = Category.OTHER)
public class BarFix extends Module {
    @Getter
    private static final BarFix instance = new BarFix();
    private static final Direction[] UPDATE_SHAPE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN, Direction.UP};
    private static final Map<Block, BlockState> ALL_DIRECTION_STATE = new HashMap<>();

    private final BooleanSetting replaceVisually = new BooleanSetting("Replace Model", true);

    // hypixel just doesnt update the block states correctly, thanks
    public static void postSync(BlockPos pos, BlockState state) {
        if (mc.level == null || !instance.isEnabled() || !Location.getArea().is(Island.Dungeon)) return;
        updateState(pos, state);
    }

    private void onBlockChanged(BlockPos pos, BlockState state) {
        if (mc.level == null || !Location.getArea().is(Island.Dungeon)) return;
        updateState(pos, state);
    }

    public static void updateState(BlockPos pos, BlockState state) {
        var cross = isCrossSectionBlock(state);
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : UPDATE_SHAPE_ORDER) {
            blockPos.setWithOffset(pos, direction);
            var offsetState = mc.level.getBlockState(blockPos);
            if (isCrossSectionBlock(offsetState)) {
                mc.level.neighborShapeChanged(direction.getOpposite(), blockPos, pos, state, 2, 64);
            }

            if (cross) {
                mc.level.neighborShapeChanged(direction, pos, blockPos, offsetState, 2, 64);
            }
        }
    }

    private static boolean isCrossSectionBlock(BlockState state) {
        return state.getBlock() instanceof CrossCollisionBlock || state.getBlock() instanceof WallBlock;
    }

    @SubscribeEvent
    private void onBlockPacket(PacketEvent.MainReceivePost event, ClientboundBlockUpdatePacket packet) {
        if (mc.level == null || !Location.getArea().is(Island.Dungeon)) return;
        onBlockChanged(packet.getPos(), packet.getBlockState());
    }

    public static boolean test(BlockState state, boolean value) {
        return Location.getArea().is(Island.Dungeon) && instance.isEnabled()
                && state.getValue(CrossCollisionBlock.NORTH) == value
                && state.getValue(CrossCollisionBlock.SOUTH) == value
                && state.getValue(CrossCollisionBlock.EAST) == value
                && state.getValue(CrossCollisionBlock.WEST) == value;
    }

    // This can be stained-glass pane and iron bars
    public static BlockState getState(BlockState state) {
        return ALL_DIRECTION_STATE.computeIfAbsent(state.getBlock(), k ->
                k.defaultBlockState()
                        .setValue(CrossCollisionBlock.NORTH, true)
                        .setValue(CrossCollisionBlock.SOUTH, true)
                        .setValue(CrossCollisionBlock.EAST, true)
                        .setValue(CrossCollisionBlock.WEST, true)
        );
    }
}
