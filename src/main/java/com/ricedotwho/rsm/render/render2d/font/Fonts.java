package com.ricedotwho.rsm.render.render2d.font;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.render.render2d.RenderState;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class Fonts {
    public final VertexFormat FORMAT = VertexFormat.builder(0)
            .addAttribute(DefaultVertexFormat.POSITION_SEMANTIC_NAME, GpuFormat.RGB32_FLOAT)
            .addAttribute(DefaultVertexFormat.COLOR_SEMANTIC_NAME, GpuFormat.RGBA8_UNORM)
            .addAttribute(DefaultVertexFormat.UV0_SEMANTIC_NAME, GpuFormat.RG32_FLOAT)
            .build();

    // lombok and manifold fight a bit with @UtilityClass and extensions
    public static final Color SHADOW_COLOUR = Color.fromRGB(60, 60, 60, 180F / 255F);
    public static final float SHADOW_OFFSET = 0.5F;

    private final Map<String, FontFace> faces = new HashMap<>();
    private final Map<FontFace, Font> fonts = new HashMap<>();

    public final String ROBOTO = "Roboto Medium";
    public final String NUNITO = "Nunito";
    public final String SF_PRO = "SF Pro Rounded";
    public final String PRODUCT_SANS = "Product Sans";
    public final String JOSEFIN_BOLD = "JoseFin Bold";
    public final String JOSEFIN = "JoseFin";

    public static void align(TextAlignment textAlignment) {
        RenderState.align(textAlignment);
    }

    @Init
    public void init() {
        register(ROBOTO, "roboto-medium");
        register(NUNITO, "nunito");
        register(SF_PRO, "sf-pro-rounded");
        register(PRODUCT_SANS, "product-sans");
        register(JOSEFIN_BOLD, "josefin-bold");
        register(JOSEFIN, "josefin");
    }

    public static void register(String id, String path, String prefix = "/assets/rsm/fonts/") {
        faces.put(id, new FontFace(prefix + path));
    }

    private Font getFont(String id) {
        var face = faces.get(id);
        if (face == null) {
            throw new IllegalArgumentException("Tried to draw unregistered fonts '$id'");
        }
        return fonts.computeIfAbsent(face, k -> new Font(k.buffer(), k.info()));
    }

    public float width(String content, float size) {
        return width(JOSEFIN, content, size);
    }

    public float width(String font, String content, float size) {
        if (content.isBlank()) return 0F;
        return getFont(font).width(content,size);
    }

    public float height(float size) {
        return height(JOSEFIN, size);
    }

    public float height(String font, float size) {
        return getFont(font).height(size);
    }

    public Vector2f measure(String font, String content, float size) {
        var f = getFont(font);
        return f.measure(content, size);
    }

    public List<String> split(String font, String content, float size, float maxWidth) {
        var lines = new ArrayList<String>();
        var f = getFont(font);

        for (var paragraph : content.split("\n")) {
            var line = "";

            for (var word : paragraph.split(" ")) {
                var candidate = line.isEmpty() ? word : "$line $word";

                if (!line.isEmpty() && f.width(candidate, size) > maxWidth) {
                    lines.add(line);
                    line = word;
                } else {
                    line = candidate;
                }
            }

            lines.add(line);
        }
        return lines;
    }

    public static Vector2f text(GuiGraphicsExtractor gfx, String font, String content, float x, float y, float size, Color colour = Color.WHITE, boolean shadow = false, Color shadowColour = SHADOW_COLOUR, float shadowOffset = SHADOW_OFFSET) {
        var f = getFont(font);
        var pose = RenderState.snapshotPose(gfx.pose());
        if (shadow) {
            f.render(gfx, content, x + shadowOffset, y + shadowOffset, size, shadowColour, pose, true);
        }
        return f.render(gfx, content, x, y, size, colour, pose, false);
    }

    public static float wrappedText(GuiGraphicsExtractor gfx, String font, String content, float x, float y, float size, float maxWidth, Color colour = Color.WHITE, boolean shadow = false, Color shadowColour = SHADOW_COLOUR, float shadowOffset = SHADOW_OFFSET) {
        var f = getFont(font);
        var pose = RenderState.snapshotPose(gfx.pose());
        var lines = split(font, content, size, maxWidth);
        var lineHeight = height(font, size);

        for (var line : lines) {
            if (shadow) {
                f.render(gfx, line, x + shadowOffset, y + shadowOffset, size, shadowColour, pose, true);
            }
            f.render(gfx, line, x, y, size, colour, pose, false);
            y += lineHeight;
        }
        return y;
    }

    public static float centeredWrappedText(GuiGraphicsExtractor gfx, String font, String content, float x, float y, float size, float maxWidth, Color colour = Color.WHITE, boolean shadow = false, Color shadowColour = SHADOW_COLOUR, float shadowOffset = SHADOW_OFFSET) {
        var f = getFont(font);
        var pose = RenderState.snapshotPose(gfx.pose());
        var lines = split(font, content, size, maxWidth);
        var lineHeight = height(font, size);

        for (var line : lines) {
            float rx = Math.max(0F, x - f.width(line, size) / 2F);
            if (shadow) {
                f.render(gfx, line, rx + shadowOffset, y + shadowOffset, size, shadowColour, pose, true);
            }
            f.render(gfx, line, rx, y, size, colour, pose, false);
            y += lineHeight;
        }
        return y;
    }

    public static Vector2f centeredText(GuiGraphicsExtractor gfx, String font, String content, float x, float y, float size, Color colour = Color.WHITE, boolean shadow = false, Color shadowColour = SHADOW_COLOUR, float shadowOffset = SHADOW_OFFSET) {
        var f = getFont(font);
        var pose = RenderState.snapshotPose(gfx.pose());
        float rx = Math.max(0F, x - (f.width(content, size) / 2F));

        if (shadow) {
            f.render(gfx, content, rx + shadowOffset, y + shadowOffset, size, shadowColour, pose, true);
        }
        return f.render(gfx, content, rx, y, size, colour, pose, false);
    }

    public static void close() {
        fonts.values().forEach(Font::close);
        fonts.clear();
    }
}
