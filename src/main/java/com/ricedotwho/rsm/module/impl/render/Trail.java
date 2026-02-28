package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.render.trails.LineTrail;
import com.ricedotwho.rsm.module.impl.render.trails.TickTrail;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ModeSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

import static com.ricedotwho.rsm.data.Colour.*;
import static com.ricedotwho.rsm.data.Colour.black;
import static com.ricedotwho.rsm.data.Colour.blue;
import static com.ricedotwho.rsm.data.Colour.cyan;
import static com.ricedotwho.rsm.data.Colour.green;
import static com.ricedotwho.rsm.data.Colour.magenta;
import static com.ricedotwho.rsm.data.Colour.orange;
import static com.ricedotwho.rsm.data.Colour.pink;
import static com.ricedotwho.rsm.data.Colour.red;
import static com.ricedotwho.rsm.data.Colour.yellow;

@Getter
@ModuleInfo(aliases = "Trail", id = "Trail", category = Category.RENDER)
public class Trail extends Module {
    private final ModeSetting mode = new ModeSetting("Trail Type", "Line", Arrays.asList("Tick", "Line"));
    private final ColourSetting colour = new ColourSetting("Colour", new Colour(0, 0, 255));
    private final NumberSetting trailLength = new NumberSetting("Trail Length", 5, 200, 40, 1);
    private final NumberSetting trailWidth = new NumberSetting("Trail Width", 1, 10, 5, 1);
    private final BooleanSetting depth = new BooleanSetting("Depth", false);

    private final LineTrail lineTrail;
    private final TickTrail tickTrail;

    public Trail() {
        this.registerProperty(
                trailLength,
                trailWidth,
                mode,
                colour,
                depth
        );
        this.lineTrail = new LineTrail(this);
        this.tickTrail = new TickTrail(this);
    }

    public Vec3 playerPos() {
        LocalPlayer player = mc.player;
        if(player == null) return null;
        double posX = player.getX();
        double posY = player.getY();
        double posZ = player.getZ();

        return new Vec3(posX, posY, posZ);
    }

    public Vec3 playerPosOld() {
        LocalPlayer player = mc.player;
        if (player == null) return null;
        double posXOld = player.xOld;
        double posYOld = player.yOld;
        double posZOld = player.zOld;

        return new Vec3(posXOld, posYOld, posZOld);
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent.Extract event) {
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) return;
        switch (mode.getValue()) {
            case "Tick" -> tickTrail.renderBox();
            case "Line" -> {
                lineTrail.renderTrail();
                lineTrail.registerTrailPos();
            }
        }
    }

    @SubscribeEvent
    public void onMovePacket(PacketEvent.Send event) {
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket)) return;
        if (playerPos() == playerPosOld()) return;
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if(level == null || player == null) return;
        if (mode.getValue().equals("Tick")) {
            tickTrail.onTick();
        }
    }
}
