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

@Getter // please don't use spaces in the id
@ModuleInfo(aliases = "Trail", id = "Trail", category = Category.RENDER)
public class Trail extends Module {
    public static final ModeSetting mode = new ModeSetting("Trail Type", "Tick", Arrays.asList("Tick", "Line"));
    public static final ModeSetting Colormode = new ModeSetting("Color", "Blue", Arrays.asList("Blue", "White", "Gray", "Black", "Red", "Pink", "Orange", "Yellow", "Green", "Purple", "Cyan"));
    public static final NumberSetting trailLength = new NumberSetting("Trail Length", 5, 100, 20, 1);
    public static final NumberSetting trailWidth = new NumberSetting("Trail Width", 1, 10, 5, 1);

    public Trail() {
        this.registerProperty(
                trailLength,
                trailWidth,
                mode,
                Colormode
        );
    }

    public static Vec3 playerPos() {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) return null;
        double posX = player.getX();
        double posY = player.getY();
        double posZ = player.getZ();

        return new Vec3(posX, posY, posZ);
    }

    public static Vec3 playerPosOld() {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) return null;
        double posXOld = player.xOld;
        double posYOld = player.yOld;
        double posZOld = player.zOld;

        return new Vec3(posXOld, posYOld, posZOld);
    }

    public static Colour getColour() {
        return switch (Colormode.getValue()) {
            case "Blue" -> blue;
            case "White" -> white;
            case "Gray" -> gray;
            case "Black" -> black;
            case "Red" -> red;
            case "Pink" -> pink;
            case "Orange" -> orange;
            case "Yellow" -> yellow;
            case "Green" -> green;
            case "Purple" -> magenta;
            case "Cyan" -> cyan;
            default -> blue;
        };
    }

    @SubscribeEvent
    public void onRender3d(Render3DEvent.Extract event) {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if (level == null || player == null) return;
        switch (mode.getValue()) {
            case "Tick" -> TickTrail.renderBox();
            case "Line" -> {
                LineTrail.renderTrail();
                LineTrail.registerTrailPos();
            }
        }
    }

    @SubscribeEvent
    public void onMovePacket(PacketEvent.Send event) {
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket)) return;
        if(playerPos() == playerPosOld()) return;
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;
        if(level == null || player == null) return;
        switch (mode.getValue()) {
            case "Tick" -> TickTrail.onTick();
        }
    }
}
