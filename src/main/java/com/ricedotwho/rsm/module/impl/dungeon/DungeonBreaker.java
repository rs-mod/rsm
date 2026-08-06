package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.managers.location.Island;
import com.ricedotwho.rsm.managers.location.Location;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Getter
@ModuleInfo(aliases = "ZPDB", id = "DungeonBreaker", category = Category.DUNGEONS)
public class DungeonBreaker extends Module {
    private static DungeonBreaker INSTANCE;
    private static final String REDSTONE_KEY_ID = "fed95410-aba1-39df-9b95-1d4f361eb66e";

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
            Blocks.NETHER_PORTAL,
            Blocks.LADDER,
            Blocks.PLAYER_HEAD,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.LEVER
    );

    private static final List<TagKey<@NotNull Block>> TAGS = List.of(
            BlockTags.BUTTONS,
            BlockTags.COPPER_CHESTS
    );

    private static final List<Class<?>> CLASSES = List.of(
            RedstoneTorchBlock.class,
            BushBlock.class,
            CauldronBlock.class,
            HopperBlock.class,
            BaseEntityBlock.class,
            CropBlock.class,
            FlowerBlock.class,
            TrapDoorBlock.class
    );

    private static final Set<BlockPos> P3_LEVERS = Set.of(
            new BlockPos(94, 124, 113),
            new BlockPos(106, 124, 113),
            new BlockPos(23, 132, 138),
            new BlockPos(27, 124, 127),
            new BlockPos(2, 122, 55),
            new BlockPos(14, 122, 55),
            new BlockPos(84, 121, 34),
            new BlockPos(86, 128, 46)
    );

    private final BooleanSetting removeMiss = new BooleanSetting("Remove Miss", false);
    private final BooleanSetting cancelBreakSecrets = new BooleanSetting("Don't Break Secrets", false);
    private static final BooleanSetting stopOnDesync = new BooleanSetting("Cancel if item desynced", false);

    public DungeonBreaker() {
        INSTANCE = this;
        this.registerProperty(removeMiss, cancelBreakSecrets, stopOnDesync);
    }

    @SubscribeEvent
    public void onAttack(PlayerInputEvent.Attack event) {
        if (shouldCancel(event.getResult()) && isItemSynced()) {
            event.setCancelled(true);
        }
    }

    public static boolean shouldNotContinueAttack(boolean bl) {
        return INSTANCE.isEnabled() && INSTANCE.cancelBreakSecrets.getValue() && bl && shouldCancel(mc.hitResult);
    }

    public static void handleDigSpeed(ItemStack held, CallbackInfoReturnable<Float> cir) {
        if (INSTANCE.isEnabled() && Location.getArea().is(Island.Dungeon) && "DUNGEONBREAKER".equals(ItemUtils.getID(held)) && isItemSynced()) {
            cir.setReturnValue(1500f);
        }
    }

    private static boolean shouldCancel(HitResult hit) {
        return hit instanceof BlockHitResult result
                && result.getType() == HitResult.Type.BLOCK
                && Location.getArea().is(Island.Dungeon)
                && "DUNGEONBREAKER".equals(ItemUtils.getID(mc.player.getMainHandItem()))
                && !canInstantMine(result.getBlockPos());
    }

    public static boolean canInstantMine(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        if (state.is(Blocks.PLAYER_HEAD) && isRedstoneSkull(pos) || P3_LEVERS.contains(pos)) return true;
        return !BLACKLIST.contains(state.getBlock())
                && TAGS.stream().noneMatch(state::is)
                && CLASSES.stream().noneMatch(c -> c.isInstance(state.getBlock()));
    }

    private static boolean isRedstoneSkull(BlockPos blockPos) {
        BlockEntity entity = mc.level.getBlockEntity(blockPos);
        if (!(entity instanceof SkullBlockEntity skullBlockEntity)) return false;
        String uuid = skullBlockEntity.getOwnerProfile().partialProfile().id().toString();
        return uuid.equals(REDSTONE_KEY_ID);
    }

    public static void onPreHandleKeybinds() {
        if (INSTANCE.isEnabled() && INSTANCE.removeMiss.getValue() && mc.player != null && "DUNGEONBREAKER".equals(ItemUtils.getID(mc.player.getMainHandItem()))) {
            mc.missTime = 0;
        }
    }

    private static boolean isItemSynced() {
        return !stopOnDesync.getValue() || mc.gameMode.carriedIndex == mc.player.getInventory().getSelectedSlot();
    }
}
