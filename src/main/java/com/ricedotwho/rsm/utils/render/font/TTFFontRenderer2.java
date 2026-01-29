package com.ricedotwho.rsm.utils.render.font;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.render.NVGUtils;
import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Random;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVG.nvgStroke;
import static org.lwjgl.nanovg.NanoVG.nvgStrokeWidth;

@SuppressWarnings("unused")
public class TTFFontRenderer2 {

    private static final Random RANDOM = new Random();

    private static final char FORMATTER = '§';
    private final int[] colorCodes = new int[32];

    @Getter
    private final Font font;
    private final CharacterData[] charData = new CharacterData[256];

    private final int margin;
    private final boolean antiAlias;
    private final boolean fractionalMetrics;

    public TTFFontRenderer2(Font font, boolean antiAlias, boolean fractionalMetrics) {
        generateColors();
        this.font = font;
        this.margin = 6;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        generateTextures();
    }

    public void drawString(String text, float x, float y, Colour colour) {
        renderString(text, x, y, colour, false);
    }
    public void drawString(String text) {
        renderString(text, 0, 0, Colour.WHITE, false);
    }

    public void drawWrappedString(String text, float x, float y, Colour colour, float maxWidth) {
        String[] words = text.split(" ");
        float lineHeight = getHeight(text);
        float currentX = x;
        float currentY = y;

        for (String word : words) {
            float wordWidth = getWidth(word + " ");

            if (currentX + wordWidth > x + maxWidth) {
                currentX = x;
                currentY += lineHeight;
            }

            renderString(word, currentX, currentY, colour, false);

            currentX += wordWidth;
        }
    }

    public void drawStringWithShadow(String text, float x, float y, Colour colour) {
        NVGUtils.translate(0.5f, 0.5f);
        renderString(text, x, y, colour, true);
        NVGUtils.translate(-0.5f, -0.5f);
        renderString(text, x, y, colour, false);
    }

    public void drawCenteredStringWithShadow(String text, float x, float y, Colour colour) {
        NVGUtils.translate(0.5f, 0.5f);
        renderString(text, x - this.getWidth(text) / 2, y, colour, true);
        NVGUtils.translate(-0.5f, -0.5f);
        renderString(text, x - this.getWidth(text) / 2, y, colour, false);
    }

    public void drawCenteredString(String text, float x, float y, Colour colour) {
        renderString(text, x - this.getWidth(text) / 2, y, colour, false);
    }

    public float getWidth(String text) {
        if (text == null || text.isEmpty())
            return 0;

        float width = 0;
        CharacterData[] characterData = charData;
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char character = text.charAt(i);

            if (character == FORMATTER || (i > 0 ? text.charAt(i - 1) : '.') == FORMATTER || !font.canDisplay(character))
                continue;

            CharacterData charData = characterData[character];

            width += (charData.width - (2 * margin)) / 2f;
        }

        return width;
    }

    public float getHeight(String text) {
        float height = 0;

        if (text == null || text.isEmpty())
            return 0;

        CharacterData[] characterData = charData;

        int length = text.length();

        for (int i = 0; i < length; i++) {
            char character = text.charAt(i);
            if ((i > 0 ? text.charAt(i - 1) : '.') == FORMATTER || character == FORMATTER
                    || !font.canDisplay(character))
                continue;

            CharacterData charData = characterData[character];
            height = Math.max(height, charData.height);
        }

        return (height - margin) / 2;
    }

    public float getHeight() {
        return this.getHeight("G");
    }

    public void generateTextures() {
        for (int codepoint = 32; codepoint <= 0xFFFF; codepoint++) {
            if (font.canDisplay((char) codepoint)) {
                setup((char) codepoint);
            }
        }
    }

    private void setup(char character) {
        BufferedImage utilityImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D utilGraphics = utilityImage.createGraphics();
        utilGraphics.setFont(font);
        FontMetrics fontMetrics = utilGraphics.getFontMetrics();
        Rectangle2D bounds = fontMetrics.getStringBounds(String.valueOf(character), utilGraphics);
        utilGraphics.dispose();

        int width = (int) Math.ceil(bounds.getWidth() + 2 * margin);
        int height = (int) Math.ceil(bounds.getHeight());

        BufferedImage charImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = charImage.createGraphics();
        graphics.setFont(font);

        if (antiAlias) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }

        graphics.setColor(new Color(0,0,0,0));
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(Color.WHITE);
        graphics.drawString(String.valueOf(character), margin, fontMetrics.getAscent());
        graphics.dispose();

        ByteBuffer rgba = toRGBA(charImage);

        int nvgImage = NanoVG.nvgCreateImageRGBA(
                NVGUtils.getVg(),
                width,
                height,
                NanoVG.NVG_IMAGE_PREMULTIPLIED,
                rgba
        );

        if (nvgImage == 0) {
            throw new RuntimeException("Failed to create NanoVG image for char: " + character);
        }

        CharacterData data = new CharacterData(
                width,
                height,
                nvgImage,
                (float) bounds.getWidth()
        );

        charData[character] = data;

        ChatUtils.chat("Created data for %s: %s", character, data);
    }

    private static ByteBuffer toRGBA(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();

        int[] pixels = new int[w * h];
        image.getRGB(0, 0, w, h, pixels, 0, w);

        ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = pixels[y * w + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
                buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
                buffer.put((byte) (pixel & 0xFF));         // B
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
            }
        }

        buffer.flip();
        return buffer;
    }

    private void renderString(String text, float x, float y, Colour colour, boolean shadow) {
        if (text == null || text.isEmpty())
            return;


        NVGUtils.push();
        NVGUtils.scale(0.5f, 0.5f);

        x -= margin / 2f;
        y -= 2;

        x *= 2;
        y *= 2;

        CharacterData[] characterData = charData;

        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        int length = text.length();

        float multiplier = (shadow ? 4 : 1);

        float a = (float) colour.getAlpha() / 255.0F;
        float r = (float) colour.getRed() / 255.0F;
        float g = (float) colour.getGreen() / 255.0F;
        float b = (float) colour.getBlue() / 255.0F;

        nvgRGBAf(r, g, b, a, NVGUtils.getNvgColor());

        for (int i = 0; i < length; i++) {
            char character = text.charAt(i);
            char previous = i > 0 ? text.charAt(i - 1) : '.';
            if (previous == FORMATTER)
                continue;

            if (character == FORMATTER) {
                int index = "0123456789abcdefklmnor".indexOf(text.charAt(i + 1));
                if (index < 16) {
                    obfuscated = false;
                    strikethrough = false;
                    underlined = false;
                    if (index < 0)
                        index = 15;
                    if (shadow)
                        index += 16;
                    int textColor = this.colorCodes[index];
                    nvgRGBAf((textColor >> 16) / 255.0F, (textColor >> 8 & 255) / 255.0F, (textColor & 255) / 255.0F, a, NVGUtils.getNvgColor());
                } else if (index == 16)
                    obfuscated = true;
                else if (index == 18)
                    strikethrough = true;
                else if (index == 19)
                    underlined = true;
                else {
                    obfuscated = false;
                    strikethrough = false;
                    underlined = false;

                    float v = 1f / multiplier;
                    nvgRGBAf(v, v, v, a, NVGUtils.getNvgColor());
                }
            } else {
                if (!font.canDisplay(character))
                    continue;

                final CharacterData charData = characterData[character];



                drawChar(charData, x, y);

                if (strikethrough)
                    drawLine(0, charData.height / 2f, charData.width, charData.height / 2f, 3);
                if (underlined)
                    drawLine(0, charData.height - 15, charData.width, charData.height - 15, 3);
                x += charData.width - (2 * margin);
            }
        }

        NVGUtils.pop();
        nvgRGBAf(1f, 1f, 1f, 1f, NVGUtils.getNvgColor());
    }


    public void drawChar(CharacterData cd, float x, float y) {
        if (cd == null) return;

        long vg = NVGUtils.getVg();
        NVGPaint paint = NVGUtils.getNvgPaint();

        NanoVG.nvgImagePattern(
                vg,
                0, 0,
                cd.width, cd.height,
                0f,
                cd.image(),
                1f,
                paint
        );

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgRect(vg, x, y, cd.width, cd.height);
        NanoVG.nvgFillPaint(vg, paint);
        NanoVG.nvgFill(vg);
    }

    private void drawLine(float x, float y, float x2, float y2, float width) {
        long vg = NVGUtils.getVg();
        nvgBeginPath(vg);
        nvgMoveTo(vg, x, y);
        nvgLineTo(vg, x2, y2);
        nvgStrokeWidth(vg, width);
        nvgStroke(vg);
    }

    private void generateColors() {
        for (int index = 0; index < 32; index++) {
            int noClue = (index >> 3 & 0x1) * 85;
            int red = (index >> 2 & 0x1) * 170 + noClue;
            int green = (index >> 1 & 0x1) * 170 + noClue;
            int blue = (index & 0x1) * 170 + noClue;

            if (index == 6) {
                red += 85;
            }

            if (index >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            this.colorCodes[index] = ((red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF);
        }
    }

    public String truncate(String text, float width) {
        if (getWidth(text) <= width) return text;

        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (getWidth(truncated + "...") < width) {
                truncated.append(text.charAt(i));
            } else {
                truncated.append("...");
                break;
            }
        }

        return truncated.toString();
    }

    public record CharacterData(int width, int height, int image, float advance) {
    }
}
