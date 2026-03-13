package com.ricedotwho.rsm.component.impl.moddedplayer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public record ModdedPlayer(Component name, float xScale, float yScale, float zScale, Identifier cape) {
}
