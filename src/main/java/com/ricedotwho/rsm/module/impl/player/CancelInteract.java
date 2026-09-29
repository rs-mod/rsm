package com.ricedotwho.rsm.module.impl.player;

import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.EnumSetSetting;
import com.ricedotwho.rsm.module.impl.movement.Ether;
import com.ricedotwho.rsm.utils.ItemUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Predicate;

@Getter
@ModuleInfo(aliases = "Cancel Interact", id = "cancel-interact", category = Category.PLAYER)
public class CancelInteract extends Module {
    @Getter
    private static final CancelInteract instance = new CancelInteract();
    private final EnumSetSetting<Mode> mode = new EnumSetSetting<>("Mode", Mode.class, List.of());

    private static final List<Class<?>> WHITELIST = List.of(
            LeverBlock.class,
            SkullBlock.class,
            AbstractCauldronBlock.class,
            ChestBlock.class
    );

    private static final List<TagKey<Block>> WHITELIST_TAGS = List.of(
            BlockTags.BUTTONS,
            BlockTags.COPPER_CHESTS
    );

    public boolean shouldCancelInteract(BlockHitResult hit, LocalPlayer player, ItemStack item) {
        if (!instance.isEnabled() || !Location.isInSkyblock() || mode.getValue().isEmpty()) return false;
        BlockState state = player.level().getBlockState(hit.getBlockPos());
        if (WHITELIST.stream().anyMatch(c -> c.isInstance(state.getBlock())) || WHITELIST_TAGS.stream().anyMatch(state::is)) return false;
        return mode.getValue().stream().anyMatch(mode -> mode.predicate.test(item));
    }

    @AllArgsConstructor
    private enum Mode {
        TELEPORT(Ether::isTpItem),
        SCEPTRE(item -> ItemUtils.getID(item).contains("BAT_WAND")),
        ENDER_PEARL(item -> ItemUtils.getID(item).equals("ENDER_PEARL"));

        private final Predicate<ItemStack> predicate;
    }
}
