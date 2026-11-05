package com.ricedotwho.rsm.ui.impl.nodes;

import com.ricedotwho.rsm.render.render2d.Font;
import com.ricedotwho.rsm.render.render2d.GuiRenderUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.FontSizeSupplier;
import com.ricedotwho.rsm.ui.api.FontSupplier;
import com.ricedotwho.rsm.ui.api.Node;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import lombok.Getter;
import lombok.val;
import org.lwjgl.util.yoga.YGSize;
import org.lwjgl.util.yoga.Yoga;


public class TextNode extends Node {

    public static final Color TEXT_SHADOW = Color.fromHex(0x000000);


    private final boolean shadow;
    @Getter
    private final FontSizeSupplier fontSize;
    private String text;
    @Getter
    private final FontSupplier fontSupplier;
    private final TextAlignment align;
    private final boolean truncateToFit;
    private final boolean wrap;

    TextNode(long yogaNode, Color color, boolean shadow, FontSizeSupplier fontSize, String text, FontSupplier fontSupplier, TextAlignment align, boolean truncateToFit, boolean wrap) {
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

        float size = fontSize.getFontSize();
        Font font = fontSupplier.getFont();
        float fullWidth = GuiRenderUtils.getTextWidth(text, size, font);
        String result;

        if (fullWidth <= width) {
            result = text;
        } else {
            float ellipsisWidth = GuiRenderUtils.getTextWidth(ELLIPSIS, size, font);
            float available = width - ellipsisWidth;

            if (available <= 0f) {
                result = ELLIPSIS;
            } else {
                int lo = 0;
                int hi = text.length();
                while (lo < hi) {
                    int mid = (lo + hi + 1) / 2;
                    String candidate = text.substring(0, mid);
                    if (GuiRenderUtils.getTextWidth(candidate, size, font) <= available) {
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
        float size = fontSize.getFontSize();
        Font font = fontSupplier.getFont();

        float measuredWidth;
        float measuredHeight;

        boolean canWrap = wrap && widthMode != Yoga.YGMeasureModeUndefined && width > 0f;

        if (canWrap) {
            measuredWidth = GuiRenderUtils.getWrappedTextWidth(getText(), width, size, font);
            measuredHeight = GuiRenderUtils.getWrappedTextHeight(getText(), width, size, font);
        } else {
            measuredWidth = GuiRenderUtils.getTextWidth(getText(), size, font);
            measuredHeight = GuiRenderUtils.getLineHeight(size, font);
        }

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
    public void frame(float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
        float originX = originX(parentX);
        float originY = originY(parentY);

        if (this.color == null) {
            return;
        }

        float width = layoutWidth();
        float height = layoutHeight();
        float size = fontSize.getFontSize();
        Font font = fontSupplier.getFont();

        float x = wrap ? originX : originX + align.calculateX(width);
        float y = originY + align.calculateY(height);

        float textHeight = wrap
                ? GuiRenderUtils.getWrappedTextHeight(getText(), width, size, font)
                : GuiRenderUtils.getLineHeight(size, font);

        float drawX = x;
        float drawY = y - align.calculateY(textHeight);

        if (!wrap) {
            drawX = x - align.calculateX(GuiRenderUtils.getTextWidth(getText(), size, font));
        }

        val toDraw = getText();

        if (shadow) {
            GuiRenderUtils.drawTextShadow(toDraw, drawX + 1.0F, drawY + 1.0F, size, TEXT_SHADOW, font);
        }

        GuiRenderUtils.drawText(toDraw, drawX, drawY, size, color, font);
    }

	public static final class Builder extends Node.Builder<Builder> {

        public Builder() {}
        private boolean shadow = false;
        private FontSizeSupplier fontSize;
        private String text;
        private FontSupplier supplier;
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

        public Builder font(FontSupplier supplier) {
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
