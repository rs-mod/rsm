package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import net.minecraft.network.PacketProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PacketProcessor.class)
public class MixinPacketProcessor {

    @WrapOperation(method = "processQueuedPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketProcessor$ListenerAndPacket;handle()V"))
    public void onHandle(PacketProcessor.ListenerAndPacket<?> instance, Operation<Void> original) {
        if (new PacketEvent.MainReceivePre(instance.packet()).post()) return;
        original.call(instance);
        new PacketEvent.MainReceivePost(instance.packet()).post();
    }
}
