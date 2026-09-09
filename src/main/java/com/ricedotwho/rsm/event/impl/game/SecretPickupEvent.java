package com.ricedotwho.rsm.event.impl.game;

import com.ricedotwho.rsm.event.Event;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.SecretType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.world.phys.Vec3;

@Getter
@AllArgsConstructor
public class SecretPickupEvent extends Event {
    private final Vec3 vec3;
    private final SecretType type;
}
