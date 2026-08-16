package com.ricedotwho.rsm.render.render3d.type;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import net.minecraft.client.gui.Font;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

@Getter
@SuppressWarnings("unused")
public class Text extends RenderTask implements Accessor {
    private final String content;
    private final Vec3 pos;
    private final Font font;
    private final float scale;
    private final Quaternionf rotation;
    private final float width;
    private final boolean dropShadow;
    private final int color;

    public Text(String content, int color, Vec3 pos, boolean depth, boolean dropShadow) {
        super(RenderType.TEXT, depth);
        this.content = content;
        this.pos = pos;
        this.font = mc.font;
        this.scale = 1f;
        this.rotation = mc.gameRenderer.getMainCamera().rotation();
        this.width = font.width(content);
        this.dropShadow = dropShadow;
        this.color = color;
    }

    public Text(String content, Vec3 pos, boolean depth) {
        this(content, Color.WHITE, pos, depth, true);
    }

    public Text(String content, int color, Vec3 pos, boolean depth) {
        this(content, color, pos, depth, true);
    }

    public Text(String content, Vec3 pos, boolean depth, boolean dropShadow) {
        this(content, Color.WHITE, pos, depth, dropShadow);
    }

    public Text(String content, int color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth, boolean dropShadow) {
        super(RenderType.TEXT, depth);
        this.content = content;
        this.pos = pos;
        this.font = font;
        this.scale = scale;
        this.rotation = rotation;
        this.width = width;
        this.dropShadow = dropShadow;
        this.color = color;
    }

    public Text(String content, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth, boolean dropShadow) {
        this(content, Color.WHITE, pos, scale, rotation, font, width, depth, dropShadow);
    }

    public Text(String content, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth) {
        this(content, Color.WHITE, pos, scale, rotation, font, width, depth, true);
    }

    public Text(String content, int color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth) {
        this(content, color, pos, scale, rotation, font, width, depth, true);
    }

    public Text(String content, Color color, Vec3 pos, boolean depth, boolean dropShadow) {
        this(content, color.getARGB(), pos, depth, dropShadow);
    }

    public Text(String content, Color color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth, boolean dropShadow) {
        this(content, color.getARGB(), pos, scale, rotation, font, width, depth, dropShadow);
    }

    public Text(String content, Color color, Vec3 pos, float scale, Quaternionf rotation, Font font, float width, boolean depth) {
        this(content, color, pos, scale, rotation, font, width, depth, true);
    }

    @Override
    public void render(PoseStack stack, VertexConsumer buffer, RenderType source) {

    }
}