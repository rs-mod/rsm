package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ModeSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.render3d.type.LineList;
import com.ricedotwho.rsm.utils.render.render3d.type.OutlineBox;
import lombok.Getter;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@ModuleInfo(aliases = "Trail", id = "Trail", category = Category.RENDER, hasKeybind = true)
public class Trail extends Module {
    private final ModeSetting mode = new ModeSetting("Trail Type", "Line", Arrays.asList("Tick", "Line"));
    private final ColourSetting colour = new ColourSetting("Start Colour", new Colour(0, 0, 255), () -> mode.getValue().equals("Line"));
    private final ColourSetting endColour = new ColourSetting("End Colour", new Colour(0, 0, 255), () -> mode.getValue().equals("Line"));
    private final ColourSetting airColour = new ColourSetting("Air Colour", new Colour(0, 255, 255), () -> mode.getValue().equals("Tick"));
    private final ColourSetting groundColour = new ColourSetting("Ground Colour", new Colour(255, 0, 0), () -> mode.getValue().equals("Tick"));
    private final NumberSetting trailLength = new NumberSetting("Trail Length", 5, 400, 40, 1);
    private final NumberSetting trailWidth = new NumberSetting("Trail Width", 0.01, 0.2, 0.05, 0.01);
    private final BooleanSetting depth = new BooleanSetting("Depth", false);

    private record C04(Vec3 pos, boolean onGround) {}

    private C04 delayedC04 = null;

    public Trail() {
        this.registerProperty(
                trailLength,
                trailWidth,
                mode,
                colour,
                endColour,
                airColour,
                groundColour,
                depth
        );
    }

    private final ArrayList<C04> packets = new ArrayList<C04>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMove(PacketEvent.Send event) {
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket packet)) return;

        if (delayedC04 != null) {
            packets.add(delayedC04);
            while (packets.size() > trailLength.getValue().intValue()) {
                packets.removeFirst();
            }
            delayedC04 = null;
        }

        if (!packet.hasPosition()) return;

        Vec3 pos = new Vec3(packet.getX(0.0), packet.getY(0.0), packet.getZ(0.0));

        if (!packets.isEmpty() && packets.getLast().pos.equals(pos)) return;

        delayedC04 = new C04(pos, packet.isOnGround());
    }

    @SubscribeEvent
    public void onRender(Render3DEvent.Extract event) {
        switch (mode.getValue()) {
            case "Tick" -> drawTicks();
            case "Line" -> drawLine();
        }
    }

    private void drawTicks() {
        float boxSize = trailWidth.getValue().floatValue() * 0.5f;
        for (C04 packet : packets) {
            Vec3 pos = packet.pos;
            AABB aabb = new AABB(pos.x - boxSize, pos.y, pos.z - boxSize, pos.x + boxSize, pos.y + boxSize * 2, pos.z + boxSize);
            Renderer3D.addTask(new OutlineBox(aabb, packet.onGround ? groundColour.getValue() : airColour.getValue(), depth.getValue()));
        }
    }

    private void drawLine() {
        List<Vec3> vec3s = packets.stream().map(packet -> packet.pos).toList();
        Renderer3D.addTask(new LineList(vec3s, colour.getValue(), endColour.getValue(), depth.getValue()));
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        packets.clear();
    }
}
