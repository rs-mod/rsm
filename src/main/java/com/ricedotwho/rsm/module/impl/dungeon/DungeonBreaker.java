package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.component.impl.SwapManager;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.player.PlayerInputEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;

@Getter
@ModuleInfo(aliases = "ZPDB", id = "DungeonBreaker", category = Category.DUNGEONS)
public class DungeonBreaker extends Module {
    private static DungeonBreaker INSTANCE;

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

    private final BooleanSetting removeMiss = new BooleanSetting("Remove Miss", false);
    private final BooleanSetting cancelBreakSecrets = new BooleanSetting("Don't Break Secrets", false);

    public DungeonBreaker() {
        INSTANCE = this;
        this.registerProperty(removeMiss, cancelBreakSecrets);
    }

    @SubscribeEvent
    public void onAttack(PlayerInputEvent.Attack event) {
        if (shouldCancel(event.getResult())) {
            event.setCancelled(true);
        }
    }

    public static boolean shouldContinueAttack(boolean bl) {
        return INSTANCE.isEnabled() && INSTANCE.cancelBreakSecrets.getValue() && bl && shouldCancel(mc.hitResult) && !SwapManager.isDesynced();
    }

    public static void handleDigSpeed(ItemStack held, CallbackInfoReturnable<Float> cir) {
        if (INSTANCE.isEnabled() && Location.getArea().is(Island.Dungeon) && "DUNGEONBREAKER".equals(ItemUtils.getID(held)) && !SwapManager.isDesynced()) {
            cir.setReturnValue(1500f);
        }
    }

    private static boolean shouldCancel(HitResult hit) {
        return hit instanceof BlockHitResult result
                && result.getType() == HitResult.Type.BLOCK
                && Location.getArea().is(Island.Dungeon)
                && "DUNGEONBREAKER".equals(ItemUtils.getID(mc.player.getMainHandItem()))
                && !canInstantMine(mc.level.getBlockState(result.getBlockPos()));
    }

    public static boolean canInstantMine(BlockState state) {
        return !BLACKLIST.contains(state.getBlock())
                && TAGS.stream().noneMatch(state::is)
                && CLASSES.stream().noneMatch(c -> c.isInstance(state.getBlock()));
    }

    // this will work when the module is disabled, kinda intentional
    public static void onPreHandleKeybinds() {
        if (INSTANCE.removeMiss.getValue() && mc.player != null && "DUNGEONBREAKER".equals(ItemUtils.getID(mc.player.getMainHandItem()))) {
            mc.missTime = 0;
        }
    }
}
