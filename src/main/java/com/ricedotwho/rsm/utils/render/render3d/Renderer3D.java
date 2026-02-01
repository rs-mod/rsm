package com.ricedotwho.rsm.utils.render.render3d;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ricedotwho.rsm.component.ModComponent;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.RenderEvent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class Renderer3D extends ModComponent {
    public Renderer3D() {
        super("Renderer3D");
    }

    @SubscribeEvent
    public void onRender3D(RenderEvent.Last event) {
        PoseStack stack = event.getContext().matrices();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();

        stack.pushPose();
        stack.translate(-camera.x, -camera.y, -camera.z);

        // render here


        stack.popPose();
    }

    private void renderLines(PoseStack stack, List<List<LineData>> lines, List<List<BoxData>> boxes, MultiBufferSource.BufferSource buffer) {

    }

    private record LineData(Vec3 from, Vec3 to, Colour colour, float thickness) { }
    private record BoxData(AABB aabb, Colour colour, float thickness) { }
}
