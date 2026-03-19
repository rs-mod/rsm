package com.ricedotwho.rsm.utils.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.mixins.accessor.AccessorBeaconBeam;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

@Getter
public class Text extends RenderTask implements Accessor {
    private final String content;
    private final Vec3 pos;
    private final Font font;
    private final float scale;
    private final Quaternionf rotation;
    private final float width;

    public Text(String content, Vec3 pos, boolean depth) {
        super(RenderType.TEXT, depth);
        this.content = content;
        this.pos = pos;
        this.font = mc.font;
        this.scale = 1f;
        this.rotation = mc.gameRenderer.getMainCamera().rotation();
        this.width = font.width(content);
    }

    public Text(String content, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth) {
        super(RenderType.TEXT, depth);
        this.content = content;
        this.pos = pos;
        this.font = font;
        this.scale = scale;
        this.rotation = rotation;
        this.width = width;
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {

    }
}