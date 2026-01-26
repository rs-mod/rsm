package com.ricedotwho.rsm.mixin.accessor;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerboundInteractPacket.class)
public interface AccessorServerboundInteractPacket {

    @Accessor("entityId")
    int getEntityId();

    @Accessor("action")
    Object getAction();
}