package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.client.player.LocalPlayer;

@UtilityClass
public class RotationState implements Accessor {
    private float yRot, yRot0, xRot, xRot0;

    public void push(LocalPlayer player) {
        yRot = player.getYRot();
        xRot = player.getXRot();
        yRot0 = player.yRotO;
        xRot0 = player.xRotO;
    }

    public void pop() {
        LocalPlayer player = mc.player;
        if (player == null) return;

        player.setYRot(yRot);
        player.setXRot(xRot);
        player.yRotO = yRot0;
        player.xRotO = xRot0;
    }
}
