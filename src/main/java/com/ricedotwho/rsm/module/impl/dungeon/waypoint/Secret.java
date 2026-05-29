package com.ricedotwho.rsm.module.impl.dungeon.waypoint;

import com.ricedotwho.rsm.data.Pos;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.AABB;

@Getter
public class Secret {
    private final Pos pos;
    @Setter
    private transient Pos translated;
    @Setter
    private transient AABB renderBox;
    private final SecretType type;
    @Setter
    private transient boolean found = false;

    public Secret(Pos pos, SecretType type) {
        this.pos = pos;
        this.type = type;
    }
}
