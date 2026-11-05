package com.ricedotwho.rsm.render.render2d;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.ricedotwho.rsm.type.Accessor;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Pair;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import lombok.experimental.UtilityClass;
import net.minecraft.resources.Identifier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@UtilityClass
@SuppressWarnings("unused")
public class NVGUtils implements Accessor {
    public final String ROBOTO = "Roboto Medium";
    public final String NUNITO = "Nunito";
    public final String SF_PRO = "SF Pro Rounded";
    public final String PRODUCT_SANS = "Product Sans";
    public final String JOSEFIN_BOLD = "JoseFin Bold";
    public final String JOSEFIN = "JoseFin";

    private final Map<String, Identifier> fontPaths = new HashMap<>();
    private final Map<String, Font> registeredFonts = new HashMap<>();

    private final Map<String, Image> images = new HashMap<>();
    private final Map<Font, Map<Integer, FontAtlas>> fontAtlases = new HashMap<>();

    private final Color TEXT_SHADOW = Color.fromHex(-16777216);

    private GuiRecorder recorder;
    private int frameDepth;

    static {
        fontPaths.put(ROBOTO, Identifier.fromNamespaceAndPath("rsm", "font/roboto-medium.ttf"));
        fontPaths.put(NUNITO, Identifier.fromNamespaceAndPath("rsm", "font/nunito.ttf"));
        fontPaths.put(SF_PRO, Identifier.fromNamespaceAndPath("rsm", "font/sf-pro-rounded.ttf"));
        fontPaths.put(PRODUCT_SANS, Identifier.fromNamespaceAndPath("rsm", "font/product-sans.ttf"));
        fontPaths.put(JOSEFIN_BOLD, Identifier.fromNamespaceAndPath("rsm", "font/josefin-bold.ttf"));
        fontPaths.put(JOSEFIN, Identifier.fromNamespaceAndPath("rsm", "font/josefin.ttf"));
    }

    public void registerFont(String name, Identifier path) {
        fontPaths.put(name, path);
    }

    public Font getFont(String name) {
        Font font = registeredFonts.get(name);
        if (font == null) {
            Identifier path = fontPaths.get(name);
            if (path == null) {
                throw new IllegalArgumentException("Font \"" + name + "\" is not registered!");
            }
            try {
                font = new Font(name, mc.getResourceManager().getResource(path).get().open());
                registeredFonts.put(name, font);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return font;
    }

    public GuiRecorder beginFrame(GuiGraphicsExtractor gfx) {
        if (frameDepth == 0) {
            recorder = new GuiRecorder(gfx);
            frameDepth = 1;
        } else {
            frameDepth++;
        }
        return recorder;
    }

    public void endFrame() {
        if (frameDepth <= 0) {
            throw new IllegalStateException("[NVGUtils] endFrame called when not drawing");
        }
        frameDepth--;
        if (frameDepth == 0) {
            GuiRecorder current = recorder;
            recorder = null;
            if (current != null) current.finish();
        }
    }

    public boolean isDrawing() {
        return frameDepth > 0;
    }

    public GuiRecorder recorder() {
        if (recorder == null) {
            throw new IllegalStateException("[NVGUtils] Not inside a frame");
        }
        return recorder;
    }

    public void push() {
        recorder().push();
    }

    public void pop() {
        recorder().pop();
    }

    public void scale(float x, float y) {
        recorder().scale(x, y);
    }

    public void scale(float factor) {
        recorder().scale(factor, factor);
    }

    public void translate(float x, float y) {
        recorder().translate(x, y);
    }

    public void rotate(float angle) {
        recorder().rotate(angle);
    }

    public void globalAlpha(float alpha) {
        recorder().globalAlpha(alpha);
    }

    public void pushScissor(float x, float y, float w, float h) {
        recorder().pushScissor(x, y, w, h);
    }

    public void popScissor() {
        recorder().popScissor();
    }

    // ------------------------------------------------------------------
    // Shapes
    // ------------------------------------------------------------------

    public void drawRect(float x, float y, float w, float h, Color color) {
        recorder().drawRect(x, y, w, h, color);
    }

    public void drawRect(float x, float y, float w, float h, int color) {
        recorder().drawRect(x, y, w, h, color);
    }

    public void drawRect(double x, double y, double w, double h, double r, Color color) {
        drawRect((float) x, (float) y, (float) w, (float) h, (float) r, color);
    }

    public void drawRect(float x, float y, float w, float h, float r, Color color) {
        recorder().drawRoundedRect(x, y, w, h, uniform(r), color);
    }

    public void drawRect(float x, float y, float w, float h, float r, int color) {
        recorder().drawRoundedRect(x, y, w, h, uniform(r), color);
    }

    public void drawRect(float x, float y, float w, float h, double r, Color color) {
        drawRect(x, y, w, h, (float) r, color);
    }

    public void drawRect(float x, float y, float w, float h, double r, int color) {
        drawRect(x, y, w, h, (float) r, color);
    }

    public void drawRoundedRect(float x, float y, float w, float h, float[] radii, Color color) {
        recorder().drawRoundedRect(x, y, w, h, radii, color);
    }

    public void drawRoundedRect(float x, float y, float w, float h, float[] radii, int color) {
        recorder().drawRoundedRect(x, y, w, h, radii, Color.fromARGB(color));
    }

    public void drawRoundedOutlineRect(float x, float y, float w, float h, float[] radii, float thickness, Color color) {
        recorder().drawRoundedOutlineRect(x, y, w, h, radii, thickness, color);
    }

    public void renderImage(Image image, float x, float y, float w, float h, float[] radii, float imageAlpha) {
        recorder().renderImage(image, x, y, w, h, radii, imageAlpha);
    }

    public void drawHalfRoundedRect(float x, float y, float w, float h, float r, boolean top, Color color) {
        recorder().drawHalfRoundedRect(x, y, w, h, r, top, color);
    }

    public void drawOutlineRect(float x, float y, float w, float h, float r, float thickness, Color color) {
        recorder().drawRoundedOutlineRect(x, y, w, h, uniform(r), thickness, color);
    }

    public void drawOutlineRect(float x, float y, float w, float h, float thickness, Color color) {
        recorder().drawRoundedOutlineRect(x, y, w, h, null, thickness, color);
    }

    public void drawCircle(float x, float y, float r, Color color) {
        recorder().drawCircle(x, y, r, color);
    }

    public void drawGradientRect(float x, float y, float w, float h, float r, Color from, Color to, Gradient direction) {
        recorder().drawGradientRect(x, y, w, h, r, from, to, direction);
    }

    public void drawGradientRect(float x, float y, float w, float h, float r, int from, int to, Gradient direction) {
        recorder().drawGradientRect(x, y, w, h, r, Color.fromARGB(from), Color.fromARGB(to), direction);
    }

    public void drawCircleOutline(float x, float y, float r, float thickness, Color color) {
        recorder().drawCircleOutline(x, y, r, thickness, color);
    }

    public void drawLine(float x, float y, float x1, float y1, float thickness, Color color) {
        recorder().drawLine(x, y, x1, y1, thickness, color);
    }

    // ------------------------------------------------------------------
    // Images
    // ------------------------------------------------------------------

    public int createNVGImage(int texId, int width, int height) {
        // NanoVG image handles no longer exist; retained for source compatibility.
        return -1;
    }

    public void renderImage(int image, int texWidth, int texHeight, float subX, float subY, float subW, float subH,
                            float x, float y, float w, float h, float r) {
        // Retained for source compatibility; int image handles no longer exist.
    }

    public int getImage(Image image) {
        if (images.containsKey(image)) {
            return 0;
        }
        throw new IllegalStateException("Image (" + image.getIdentifier() + ") doesn't exist");
    }

    public boolean hasImage(Image image) {
        return images.containsKey(image);
    }

    public void renderImage(Image image, float x, float y, float w, float h, float r, float imageAlpha) {
        recorder().renderImage(image, x, y, w, h, uniform(r), imageAlpha);
    }

    public void renderImage(Image image, float x, float y, float w, float h, float r) {
        renderImage(image, x, y, w, h, r, 1f);
    }

    public void renderImage(Image image, float x, float y, float w, float h) {
        renderImage(image, x, y, w, h, 0f, 1f);
    }

    public void renderImage(Image image, int texWidth, int texHeight, float subX, float subY, float subW, float subH,
                            float x, float y, float w, float h, float r) {
        recorder().renderImage(image, texWidth, texHeight, subX, subY, subW, subH, x, y, w, h, r);
    }

    public Image createImage(String path) {
        return images.computeIfAbsent(path, key -> new Image(path));
    }

    public Image createImage(String path, int flags) {
        return images.computeIfAbsent(path, key -> new Image(path, flags));
    }

    public Image createImage(String path, InputStream stream) {
        Image image = new Image(path, stream);
        images.putIfAbsent(path, image);
        return images.get(path);
    }

    public Image createImage(String id, ByteBuffer buffer) {
        Image image = new BufferImage(id, buffer);
        images.putIfAbsent(id, image);
        return images.get(id);
    }

    public Image createImage(String id, BufferedImage bufferedImage) {
        return createImage(id, bufferedImageToPNGByteBuffer(bufferedImage));
    }

    public void deleteImage(Image image) {
        deleteImageFully(image);
    }

    public void deleteImageFully(Image image) {
        images.remove(image.getIdentifier());
        image.deleteTexture();
    }

    public static Pair<Integer, Integer> getImageSize(Image image) {
        if (image == null) return new Pair<>(0, 0);
        return new Pair<>(image.width(), image.height());
    }

    public static ByteBuffer bufferedImageToPNGByteBuffer(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return ByteBuffer.wrap(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ------------------------------------------------------------------
    // Text
    // ------------------------------------------------------------------

    public void setTextAlignment(TextAlignment alignment) {
        recorder().setTextAlignment(alignment);
    }

    public TextAlignment textAlignment() {
        return recorder().textAlignment();
    }

    public void drawCenteredText(String text, float x, float y, float size, Color color, Font font) {
        float halfWidth = getTextWidth(text, size, font) / 2f;
        drawText(text, x - halfWidth, y, size, color, font);
    }

    public void drawText(String text, float x, float y, float size, Color color, boolean shadow, Font font) {
        if (shadow) {
            drawTextShadow(text, x, y, size, color, font);
        } else {
            drawText(text, x, y, size, color, font);
        }
    }

    public void drawText(String text, float x, float y, float size, Color color, Font font) {
        drawText(text, x, y, size, color, font, TextAlignment.TopLeft);
    }

    public void drawText(String text, float x, float y, float size, Color color, Font font, TextAlignment alignment) {
        FontAtlas atlas = getFontAtlas(font, size);
        recorder().drawText(atlas, text, x, y, color, alignment);
    }

    public void drawTextShadow(String text, float x, float y, float size, Color color, Font font) {
        drawTextShadow(text, x, y, size, color, font, TextAlignment.TopLeft);
    }

    public void drawTextShadow(String text, float x, float y, float size, Color color, Font font, TextAlignment alignment) {
        FontAtlas atlas = getFontAtlas(font, size);
        recorder().drawTextShadow(atlas, text, x, y, color, TEXT_SHADOW, alignment);
    }

    public void drawTextBox(String text, float x, float y, float width, float size, Color color, Font font) {
        FontAtlas atlas = getFontAtlas(font, size);
        recorder().drawTextBox(atlas, text, x, y, width, color);
    }

    public float getTextWidth(String text, float size, Font font) {
        return getFontAtlas(font, size).measureWidth(text);
    }

    public float getTextHeight(float size, Font font) {
        return getFontAtlas(font, size).measureHeight("G");
    }

    public float getTextHeight(String content, float size, Font font) {
        return getFontAtlas(font, size).measureHeight(content);
    }

    public float getLineHeight(float size, Font font) {
        return getFontAtlas(font, size).lineHeight();
    }

    public float getWrappedTextWidth(String text, float width, float size, Font font) {
        return getFontAtlas(font, size).measureWrapped(text, width).width();
    }

    public float getWrappedTextHeight(String text, float width, float size, Font font) {
        return getFontAtlas(font, size).measureWrapped(text, width).height();
    }

    // ------------------------------------------------------------------
    // Misc
    // ------------------------------------------------------------------

    public boolean isHovering(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private FontAtlas getFontAtlas(Font font, float size) {
        int pixelSize = Math.max(1, Math.round(size));
        return fontAtlases.computeIfAbsent(font, f -> new HashMap<>())
                .computeIfAbsent(pixelSize, ps -> new FontAtlas(font, ps));
    }

    private static float[] uniform(float radius) {
        return new float[]{radius, radius, radius, radius};
    }

    public void clearFontAtlases() {
        fontAtlases.values().forEach(map -> map.values().forEach(FontAtlas::close));
        fontAtlases.clear();
    }
}
