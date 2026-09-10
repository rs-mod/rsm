package com.ricedotwho.rsm.module.impl.dungeon.waypoint;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Getter
public class Secret {
    @Setter
    private Vec3 pos;
    @Setter
    private transient Vec3 translated;
    @Setter
    private transient AABB renderBox;
    private final SecretType type;
    @Setter
    private transient boolean found = false;

    public Secret(Vec3 pos, SecretType type) {
        this.pos = pos;
        this.type = type;
    }
}
