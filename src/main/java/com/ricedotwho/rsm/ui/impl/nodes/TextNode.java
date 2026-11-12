package com.ricedotwho.rsm.ui.impl.nodes;

import com.ricedotwho.rsm.render.render2d.font.Fonts;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.FontSizeSupplier;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2f;
import org.lwjgl.util.yoga.YGSize;
import org.lwjgl.util.yoga.Yoga;

import java.util.function.Supplier;

public class TextNode extends Node {
    public static final Color TEXT_SHADOW = Color.fromHex(0x000000);

    private final boolean shadow;
    @Getter
    private final FontSizeSupplier fontSize;
    private String text;
    @Getter
    private final Supplier<String> fontSupplier;
    private final TextAlignment align;
    private final boolean truncateToFit;
    private final boolean wrap;

    TextNode(long yogaNode, Color color, boolean shadow, FontSizeSupplier fontSize, String text, Supplier<String> fontSupplier, TextAlignment align, boolean truncateToFit, boolean wrap) {
        super(yogaNode, color);
        this.shadow = shadow;
        this.fontSize = fontSize;
        this.text = text;
        this.fontSupplier = fontSupplier;
        this.align = align;
        this.truncateToFit = truncateToFit;
        this.wrap = wrap;

        // TODO: Leak
        Yoga.YGNodeSetMeasureFunc(yogaNode, this::measure);
    }

    public void setText(String text) {
        this.text = text;
        this.cachedSourceText = null;
        Yoga.YGNodeMarkDirty(yogaNode);
    }

    private static final String ELLIPSIS = "...";

    private String cachedTruncatedText;
    private String cachedSourceText;
    private float cachedTruncateWidth = -1f;

    private String getText() {
        if (!truncateToFit) return text;

        float width = layoutWidth();
        if (width <= 0f) return text;

        if (text.equals(cachedSourceText) && width == cachedTruncateWidth) {
            return cachedTruncatedText;
        }

        float fullWidth = Fonts.width(fontSupplier.get(), text, fontSize.getFontSize());
        String result;

        if (fullWidth <= width) {
            result = text;
        } else {
            float ellipsisWidth = Fonts.width(fontSupplier.get(), ELLIPSIS, fontSize.getFontSize());
            float available = width - ellipsisWidth;

            if (available <= 0f) {
                result = ELLIPSIS;
            } else {
                int lo = 0;
                int hi = text.length();
                while (lo < hi) {
                    int mid = (lo + hi + 1) / 2;
                    String candidate = text.substring(0, mid);
                    if (Fonts.width(fontSupplier.get(), candidate, fontSize.getFontSize()) <= available) {
                        lo = mid;
                        continue;
                    }
                    hi = mid - 1;
                }
                result = lo == 0 ? ELLIPSIS : text.substring(0, lo) + ELLIPSIS;
            }
        }

        cachedSourceText = text;
        cachedTruncateWidth = width;
        cachedTruncatedText = result;
        return result;
    }

    private void measure(long node, float width, int widthMode, float height, int heightMode, YGSize result) {
        Vector2f measured = Fonts.measure(fontSupplier.get(), getText(), fontSize.getFontSize());

        float measuredWidth = measured.x;
        float measuredHeight = Fonts.height(fontSupplier.get(), fontSize.getFontSize());

        if (widthMode == Yoga.YGMeasureModeExactly) {
            measuredWidth = width;
        } else if (widthMode == Yoga.YGMeasureModeAtMost) {
            measuredWidth = Math.min(measuredWidth, width);
        }

        if (heightMode == Yoga.YGMeasureModeExactly) {
            measuredHeight = height;
        } else if (heightMode == Yoga.YGMeasureModeAtMost) {
            measuredHeight = Math.min(measuredHeight, height);
        }

        result.set(measuredWidth, measuredHeight);
    }

    @Override
    public void frame(GuiGraphicsExtractor gfx, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        float originX = originX(parentX);
        float originY = originY(parentY);

        if (this.color == null) {
            return;
        }

        float width = layoutWidth();
        float height = layoutHeight();
        float x = originX + align.calculateX(width);
        float y = originY + align.calculateY(height);
        gfx.push();
        align.setTextAlign();

        var yOffset = 0f;

        if (align == TextAlignment.CenterLeft || align == TextAlignment.CenterMiddle || align == TextAlignment.CenterRight) {
            yOffset = 1f;
        }

        var toDraw = getText();

        if (wrap) {
            Fonts.wrappedText(gfx, Fonts.JOSEFIN, toDraw, x, y + yOffset, fontSize.getFontSize(), width, colour:color, shadow:shadow, shadowOffset:1F);
        } else {
            Fonts.text(gfx, Fonts.JOSEFIN, toDraw, x, y + yOffset, fontSize.getFontSize(), colour:color);
        }

        gfx.pop();
    }

	public static final class Builder extends Node.Builder<Builder> {

        public Builder() {}
        private boolean shadow = false;
        private FontSizeSupplier fontSize;
        private String text;
        private Supplier<String> supplier;
        private TextAlignment align = TextAlignment.TopLeft;
        private boolean truncateTextToFit = false;
        private boolean wrap = false;

        public Builder wrap(boolean wrap) {
            this.wrap = wrap;
            return this;
        }

        public Builder truncateTextToFit(boolean shrink) {
            this.truncateTextToFit = shrink;
            return this;
        }
        public Builder shadow(boolean shadow) {
            this.shadow = shadow;
            return this;
        }

        public Builder fontSize(FontSizeSupplier fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public Builder text(String text) {
            this.text = text;
            return this;
        }

        public Builder font(Supplier<String> supplier) {
            this.supplier = supplier;
            return this;
        }

        public Builder align(TextAlignment align) {
            this.align = align;
            return this;
        }

        @Override
        public TextNode build() {
            if (truncateTextToFit && wrap) throw new IllegalArgumentException("Attempted to use truncateTextToFit argument and wrap argument at the same time!");
            return new TextNode(buildYogaNode(), color, shadow, fontSize, text, supplier, align, truncateTextToFit, wrap);
        }
    }
}
