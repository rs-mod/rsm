package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.component.impl.location.Loc;
import com.ricedotwho.rsm.module.impl.dungeon.DungeonbreakerHelper;
import com.ricedotwho.rsm.utils.ItemUtils;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer {
    @Shadow
    public abstract Inventory getInventory();

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void modifyBreakSpeed(BlockState state, CallbackInfoReturnable<Float> cir) {
        if (Loc.area.is(Island.Dungeon) && "DUNGEONBREAKER".equals(ItemUtils.getID(this.getInventory().getSelectedItem())) && RSM.getModule(DungeonbreakerHelper.class).isEnabled()) {
            if (DungeonbreakerHelper.canInstantMine(state)) {
                cir.setReturnValue(1500f);
            } else {
                cir.setReturnValue(0f);
            }
        }
    }
}
