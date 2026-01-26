package com.ricedotwho.rsm.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;


@Getter
@Setter
@AllArgsConstructor
public class RotaPos {
    private Vec3 vec3;
    private float[] rotation;
}
