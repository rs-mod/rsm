package com.ricedotwho.rsm.utils.render.render3d.type;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;

@Getter
public class BeaconTask extends RenderTask implements Accessor {
    private final Pos pos;
    private final Colour colour;
    private final boolean scoping;
    private final long gameTime;

    public BeaconTask(Pos pos, Colour colour) {
        super(RenderType.BEACON, false);
        this.pos = pos;
        this.colour = colour;
        if (mc.level == null || mc.player == null) {
            this.scoping = false;
            this.gameTime = 0L;
        } else {
            this.scoping = mc.player.isScoping();
            this.gameTime = mc.level.getGameTime();
        }
    }
}