package com.ricedotwho.rsm.mixins.accessor;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface LocalPlayerAccessor {
    @Accessor("yRotLast")
    void setYRotLast(float yRotLast);

    @Accessor("xRotLast")
    void setXRotLast(float xRotLast);

    @Accessor("crouching")
    boolean wasSneaking();

    @Accessor("positionReminder")
    int getPositionReminder();

    @Accessor("positionReminder")
    void setPositionReminder(int reminder);

}
