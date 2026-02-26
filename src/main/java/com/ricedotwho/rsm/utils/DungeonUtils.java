package com.ricedotwho.rsm.utils;

import com.ricedotwho.rsm.data.Phase7;
import lombok.experimental.UtilityClass;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static com.ricedotwho.rsm.data.Phase7.*;

@UtilityClass
public class DungeonUtils implements Accessor {

    private final AABB S1Box = new AABB(
            89, 0, 30,
            113, 255, 122
    );

    private final AABB S2Box = new AABB(
            19, 0, 121,
            111, 255, 145
    );

    private final AABB S3Box = new AABB(
            -6, 0, 50,
            19, 255, 143
    );

    private final AABB S4Box = new AABB(
            -2, 0, 27,
            90, 255, 51
    );

    private final AABB BossBox = new AABB(
            134, 0, 147,
            -8, 254, -8
    );
    
    public Phase7 getF7Phase() {
        double posY = mc.player.position().y();
        if (posY > 210) return P1;
        if (posY > 155) return P2;
        if (posY > 100) return P3;
        if (posY > 45) return P4;
        return P5;
    }

    public boolean isPositionInF7Boss(Vec3 vec3) {
        return BossBox.contains(vec3);
    }
    
    public Phase7 getP3Section() {
        return getP3Section(mc.player.position());
    }
    
    public Phase7 getP3Section(Vec3 pos) {
        if (S1Box.contains(pos)) {
            return S1;
        }

        if (S2Box.contains(pos)) {
            return S2;
        }

        if (S3Box.contains(pos)) {
            return S3;
        }

        if (S4Box.contains(pos)) {
            return S4;
        }

        return UNKNOWN;
    }
    
    public boolean isPhase(Phase7 phase) {
        return getF7Phase() == phase;
    }
    
    public boolean isP3Section(Phase7 section) {
        return getP3Section() == section;
    }
}