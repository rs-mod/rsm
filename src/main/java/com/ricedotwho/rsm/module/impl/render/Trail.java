package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.event.api.EventPriority;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.managers.Renderer3D;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.ColorSetting;
import com.ricedotwho.rsm.module.api.settings.impl.ModeSetting;
import com.ricedotwho.rsm.module.api.settings.impl.NumberSetting;
import com.ricedotwho.rsm.render.render3d.type.LineList;
import com.ricedotwho.rsm.render.render3d.type.OutlineBox;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@ModuleInfo(aliases = "Trail", id = "Trail", category = Category.RENDER, hasKeybind = true)
public class Trail extends Module {
    @SuppressWarnings("unused")
    private static final Trail instance = new Trail();

    private final ModeSetting mode = new ModeSetting("Trail Type", "Line", Arrays.asList("Tick", "Line"));
    private final ColorSetting color = new ColorSetting("Start Color", Color.fromRGB(0, 0, 255), () -> mode.getValue().equals("Line"));
    private final ColorSetting endColor = new ColorSetting("End Color", Color.fromRGB(0, 0, 255), () -> mode.getValue().equals("Line"));
    private final ColorSetting airColor = new ColorSetting("Air Color", Color.fromRGB(0, 255, 255), () -> mode.getValue().equals("Tick"));
    private final ColorSetting groundColor = new ColorSetting("Ground Color", Color.fromRGB(255, 0, 0), () -> mode.getValue().equals("Tick"));
    private final NumberSetting<Integer> trailLength = new NumberSetting<>("Trail Length", 5, 400, 40, 1);
    private final NumberSetting<Float> trailWidth = new NumberSetting<>("Trail Width", 0.01f, 0.2f, 0.05f, 0.01f);
    private final BooleanSetting depth = new BooleanSetting("Depth", false);

    private record C04(Vec3 pos, boolean onGround) {}

    private C04 delayedC04 = null;

    private final ArrayList<C04> packets = new ArrayList<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private void onMove(PacketEvent.Send event, ServerboundMovePlayerPacket packet) {

        if (delayedC04 != null) {
            packets.add(delayedC04);
            while (packets.size() > trailLength.getValue()) {
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
    private void onRender(Render3DEvent.Extract event) {
        switch (mode.getValue()) {
            case "Tick" -> drawTicks();
            case "Line" -> drawLine();
        }
    }

    private void drawTicks() {
        float boxSize = trailWidth.getValue() * 0.5f;
        for (C04 packet : packets) {
            Vec3 pos = packet.pos;
            AABB aabb = new AABB(pos.x - boxSize, pos.y, pos.z - boxSize, pos.x + boxSize, pos.y + boxSize * 2, pos.z + boxSize);
            Renderer3D.addTask(new OutlineBox(aabb, packet.onGround ? groundColor.getValue() : airColor.getValue(), depth.getValue()));
        }
    }

    private void drawLine() {
        List<Vec3> vec3s = packets.stream().map(packet -> packet.pos).toList();
        Renderer3D.addTask(new LineList(vec3s, color.getValue(), endColor.getValue(), depth.getValue()));
    }

    @SubscribeEvent
    private void onLoad(WorldEvent.Load event) {
        packets.clear();
    }
}
