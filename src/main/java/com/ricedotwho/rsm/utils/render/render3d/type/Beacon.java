package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.component.impl.Renderer3D;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.mixins.accessor.AccessorBeaconBeam;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

@Getter
public class Beacon extends RenderTask implements Accessor {
    private static final ResourceLocation BEAM_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");

    private final Vec3 pos;
    private final Colour colour;
    private final boolean scoping;
    private final long gameTime;

    @Deprecated
    public Beacon(Pos pos, Colour colour) {
        this(pos.asVec3(), colour);
    }

    public Beacon(Vec3 pos, Colour colour) {
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

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {

    }

    public void renderBeacon(PoseStack stack, Vec3 camera) {
        stack.pushPose();
        stack.translate(this.getPos().x() - camera.x() - 0.5, this.getPos().y() - camera.y(), this.getPos().z() - camera.z() - 0.5);
        double cX = this.getPos().x();
        double cZ = this.getPos().z();
        double dx = camera.x() - cX;
        double dz = camera.z() - cZ;
        float length = (float)Math.sqrt(dx * dx + dz * dz);
        float scale = this.isScoping() ? 1.0F : Math.max(1.0F, length * 0.010416667F);
        AccessorBeaconBeam.renderBeaconBeam(
                stack,
                mc.gameRenderer.getFeatureRenderDispatcher().getSubmitNodeStorage(),
                BEAM_TEXTURE,
                1.0F,
                this.getGameTime(),
                0,
                319,
                this.getColour().getRGB(),
                0.2F * scale,
                0.25F * scale
        );
        stack.popPose();
    }
}