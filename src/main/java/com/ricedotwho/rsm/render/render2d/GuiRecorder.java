package com.ricedotwho.rsm.render.render2d;

import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.TextAlignment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.ColoredRectangleRenderState;
import org.joml.Matrix3x2f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * Records immediate-mode style GUI drawing operations and emits them as vanilla GUI render states.
 *
 * <p>All vertices are transformed into GUI-scaled screen coordinates at record time. This keeps the
 * emitted states independent of the current transform and allows accurate bounds/ordering.</p>
 */
final class GuiRecorder {
    static final Matrix3x2f IDENTITY = new Matrix3x2f();

    private final GuiGraphicsExtractor gfx;
    private final Deque<State> states = new ArrayDeque<>();

    private Matrix3x2f pose;
    private float alpha = 1f;
    private ScreenRectangle scissor;
    private TextAlignment textAlignment = TextAlignment.TopLeft;

    private final Batch batch = new Batch();
    private boolean batchTextured;
    private TextureSetup batchTexture;
    private boolean finished;

    GuiRecorder(GuiGraphicsExtractor gfx) {
        this.gfx = gfx;

        float windowWidth = com.ricedotwho.rsm.type.Accessor.mc.getWindow().getWidth();
        float scale = windowWidth <= 0f ? 1f : (float) gfx.guiWidth() / windowWidth;

        this.pose = new Matrix3x2f(gfx.pose()).scale(scale);

        ScreenRectangle initialScissor = gfx.scissorStack.peek();
        this.scissor = initialScissor != null
                ? initialScissor
                : new ScreenRectangle(0, 0, gfx.guiWidth(), gfx.guiHeight());
    }

    TextAlignment textAlignment() {
        return textAlignment;
    }

    void setTextAlignment(TextAlignment alignment) {
        this.textAlignment = alignment == null ? TextAlignment.TopLeft : alignment;
    }

    void push() {
        states.push(new State(new Matrix3x2f(pose), alpha, scissor, textAlignment));
    }

    void pop() {
        if (states.isEmpty()) return;
        State state = states.pop();
        this.pose = state.pose;
        this.alpha = state.alpha;
        this.scissor = state.scissor;
        this.textAlignment = state.alignment;
    }

    void translate(float x, float y) {
        pose.translate(x, y);
    }

    void scale(float x, float y) {
        pose.scale(x, y);
    }

    void rotate(float angle) {
        pose.rotate(angle);
    }

    void globalAlpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0f, 1f);
    }

    void pushScissor(float x, float y, float w, float h) {
        flush();
        push();
        ScreenRectangle rectangle = screenRectangle(x, y, w, h);
        ScreenRectangle intersected = intersect(scissor, rectangle);
        // Never null out the scissor — if the new rect doesn't intersect, keep the current one.
        // A null scissor causes flush() to drop every subsequent batch.
        this.scissor = intersected != null ? intersected : scissor;
    }

    void popScissor() {
        flush();
        pop();
    }

    void finish() {
        if (finished) return;
        flush();
        finished = true;
    }

    // ------------------------------------------------------------------
    // Shapes
    // ------------------------------------------------------------------

    void drawRect(float x, float y, float w, float h, Color color) {
        drawRoundedRect(x, y, w, h, null, color);
    }

    void drawRect(float x, float y, float w, float h, int color) {
        drawRoundedRect(x, y, w, h, null, color);
    }

    void drawRect(float x, float y, float w, float h, float radius, Color color) {
        drawRoundedRect(x, y, w, h, uniform(radius), color);
    }

    void drawRect(float x, float y, float w, float h, float radius, int color) {
        drawRoundedRect(x, y, w, h, uniform(radius), color);
    }

    void drawRoundedRect(float x, float y, float w, float h, float[] radii, Color color) {
        if (color == null) return;
        drawRoundedRect(x, y, w, h, radii, color.getARGB());
    }

    void drawRoundedRect(float x, float y, float w, float h, float[] radii, int color) {
        if (scissor == null || alpha <= 0f) return;

        if (w < 0f) {
            x += w;
            w = -w;
        }
        if (h < 0f) {
            y += h;
            h = -h;
        }
        if (w <= 0f || h <= 0f) return;

        int argb = multiplyAlpha(color);
        // Use vanilla's fill() directly for all solid rectangles.
        // This bypasses the custom colored render-state batch entirely.
        fillRect(x, y, w, h, argb);
    }

    /**
     * Renders a solid rectangle directly via {@link GuiGraphicsExtractor#fill},
     * bypassing the custom GUI render-state batch. Coordinates are in the
    /**
     * Renders a solid rectangle directly into {@link GuiGraphicsExtractor#guiRenderState},
     * bypassing both the custom batch and {@link GuiGraphicsExtractor#fill}. Coordinates are in
     * the recorder's local space and are baked by the current pose.
     */
    void fillRect(float x, float y, float w, float h, int argb) {
        flush();

        // Bake local coordinates into GUI space using the current pose.
        float x0 = GuiGeometry.transformX(pose, x, y);
        float y0 = GuiGeometry.transformY(pose, x, y);
        float x1 = GuiGeometry.transformX(pose, x + w, y + h);
        float y1 = GuiGeometry.transformY(pose, x + w, y + h);

        int left = (int) Math.floor(Math.min(x0, x1));
        int top = (int) Math.floor(Math.min(y0, y1));
        int right = (int) Math.ceil(Math.max(x0, x1));
        int bottom = (int) Math.ceil(Math.max(y0, y1));

        ScreenRectangle scissorArea = scissor;
        ScreenRectangle bounds = new ScreenRectangle(left, top, right - left, bottom - top);
        if (scissorArea != null) {
            bounds = bounds.intersection(scissorArea);
        }
        if (bounds == null) {
            bounds = ScreenRectangle.empty();
        }

        // Pass color directly — ColoredRectangleRenderState.setColor(int) unpacks
        // the color int the same way vanilla's fill() does.
        gfx.guiRenderState.addGuiElement(new ColoredRectangleRenderState(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                GuiRecorder.IDENTITY,
                left, top, right, bottom,
                argb, argb,
                scissorArea,
                bounds
        ));
    }

    void drawOutlineRect(float x, float y, float w, float h, float radius, float thickness, Color color) {
        drawRoundedOutlineRect(x, y, w, h, uniform(radius), thickness, color);
    }

    void drawOutlineRect(float x, float y, float w, float h, float thickness, Color color) {
        drawRoundedOutlineRect(x, y, w, h, null, thickness, color);
    }

    void drawRoundedOutlineRect(float x, float y, float w, float h, float[] radii, float thickness, Color color) {
        if (color == null || thickness <= 0f || scissor == null || alpha <= 0f) return;

        if (w < 0f) {
            x += w;
            w = -w;
        }
        if (h < 0f) {
            y += h;
            h = -h;
        }
        if (w <= 0f || h <= 0f) return;

        int argb = multiplyAlpha(color.getARGB());
        float inset = thickness;
        if (w - inset * 2f <= 0f || h - inset * 2f <= 0f) {
            drawRoundedRect(x, y, w, h, radii, argb);
            return;
        }

        float[] outerRadii = radii == null ? new float[]{0f, 0f, 0f, 0f} : radii;
        float[] innerRadii = new float[4];
        for (int i = 0; i < 4; i++) {
            innerRadii[i] = Math.max(0f, outerRadii[i] - inset);
        }

        int segments = segmentsFor(outerRadii);
        List<GuiGeometry.Point> outer = new ArrayList<>();
        List<GuiGeometry.Point> inner = new ArrayList<>();
        GuiGeometry.roundedRect(outer, x, y, w, h, outerRadii, segments);
        GuiGeometry.roundedRect(inner, x + inset, y + inset, w - inset * 2f, h - inset * 2f, innerRadii, segments);

        int count = Math.min(outer.size(), inner.size());
        for (int i = 0; i < count; i++) {
            int next = (i + 1) % count;
            addQuad(outer.get(i), inner.get(i), inner.get(next), outer.get(next), argb);
        }
    }

    void drawHalfRoundedRect(float x, float y, float w, float h, float radius, boolean top, Color color) {
        float[] radii = top
                ? new float[]{radius, radius, 0f, 0f}
                : new float[]{0f, 0f, radius, radius};
        drawRoundedRect(x, y, w, h, radii, color);
    }

    void drawCircle(float x, float y, float radius, Color color) {
        if (color == null || radius <= 0f || scissor == null || alpha <= 0f) return;
        List<GuiGeometry.Point> points = new ArrayList<>();
        GuiGeometry.circle(points, x, y, radius, GuiGeometry.circleSegments(radius));
        fillPolygon(points, multiplyAlpha(color.getARGB()));
    }

    void drawCircleOutline(float x, float y, float radius, float thickness, Color color) {
        if (color == null || radius <= 0f || thickness <= 0f || scissor == null || alpha <= 0f) return;

        float innerRadius = radius - thickness;
        if (innerRadius <= 0f) {
            drawCircle(x, y, radius, color);
            return;
        }

        int segments = GuiGeometry.circleSegments(radius);
        List<GuiGeometry.Point> outer = new ArrayList<>();
        List<GuiGeometry.Point> inner = new ArrayList<>();
        GuiGeometry.circle(outer, x, y, radius, segments);
        GuiGeometry.circle(inner, x, y, innerRadius, segments);

        int argb = multiplyAlpha(color.getARGB());
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            addQuad(outer.get(i), inner.get(i), inner.get(next), outer.get(next), argb);
        }
    }

    void drawLine(float x, float y, float x1, float y1, float thickness, Color color) {
        if (color == null || thickness <= 0f || scissor == null || alpha <= 0f) return;

        float dx = x1 - x;
        float dy = y1 - y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length <= 0.0001f) return;

        float nx = -dy / length * (thickness * 0.5f);
        float ny = dx / length * (thickness * 0.5f);

        int argb = multiplyAlpha(color.getARGB());
        addQuad(
                new GuiGeometry.Point(x + nx, y + ny),
                new GuiGeometry.Point(x1 + nx, y1 + ny),
                new GuiGeometry.Point(x1 - nx, y1 - ny),
                new GuiGeometry.Point(x - nx, y - ny),
                argb
        );
    }

    void drawGradientRect(float x, float y, float w, float h, float radius, Color from, Color to, Gradient direction) {
        if (from == null || to == null || scissor == null || alpha <= 0f) return;

        if (w < 0f) {
            float nx = x + w;
            w = -w;
            x = nx;
        }
        if (h < 0f) {
            float ny = y + h;
            h = -h;
            y = ny;
        }
        if (w <= 0f || h <= 0f) return;

        float[] radii = radius <= 0f ? null : uniform(radius);
        if (!hasRounding(radii)) {
            int c0 = multiplyAlpha(from.getARGB());
            int c1 = multiplyAlpha(to.getARGB());
            float x1 = x + w;
            float y1 = y + h;
            if (direction == Gradient.LeftToRight) {
                addQuad(x, y, x1, y1, c0, c0, c1, c1);
            } else {
                addQuad(x, y, x1, y1, c0, c1, c1, c0);
            }
            return;
        }

        List<GuiGeometry.Point> points = new ArrayList<>();
        GuiGeometry.roundedRect(points, x, y, w, h, radii, segmentsFor(radii));
        int c0 = multiplyAlpha(from.getARGB());
        int c1 = multiplyAlpha(to.getARGB());

        GuiGeometry.Point center = centroid(points);
        for (int i = 0; i < points.size(); i++) {
            GuiGeometry.Point a = points.get(i);
            GuiGeometry.Point b = points.get((i + 1) % points.size());
            addGradientTriangle(center, a, b, x, y, w, h, c0, c1, direction);
        }
    }

    private void addGradientTriangle(GuiGeometry.Point a, GuiGeometry.Point b, GuiGeometry.Point c,
                                     float x, float y, float w, float h, int c0, int c1, Gradient direction) {
        int ca = gradientColor(a, x, y, w, h, c0, c1, direction);
        int cb = gradientColor(b, x, y, w, h, c0, c1, direction);
        int cc = gradientColor(c, x, y, w, h, c0, c1, direction);
        addTriangle(a, b, c, ca, cb, cc);
    }

    // ------------------------------------------------------------------
    // Text
    // ------------------------------------------------------------------

    void drawText(FontAtlas atlas, String text, float x, float y, Color color, TextAlignment alignment) {
        if (color == null || text == null || text.isEmpty() || scissor == null || alpha <= 0f) return;

        atlas.prepare(text);
        FontAtlas.TextMetrics metrics = atlas.measure(text);
        // Convert alignment anchor to text top-left (drawText draws text starting at drawX).
        // x/y are alignment anchors like NanoVG: *Right means right edge at x, Center* means center at x.
        float drawX = x - horizontalOffset(alignment, metrics.width());
        // y is treated as text top (like NanoVG NVG_ALIGN_TOP). For Center*/Bottom*,
        // adjust so the alignment anchor maps correctly using actual text height.
        float baseline = y + atlas.ascent() - verticalOffset(alignment, metrics.height());
        addString(atlas, text, drawX, baseline, multiplyAlpha(color.getARGB()));
    }

    void drawTextShadow(FontAtlas atlas, String text, float x, float y, Color color, Color shadowColor, TextAlignment alignment) {
        if (color == null || text == null || text.isEmpty() || scissor == null || alpha <= 0f) return;

        atlas.prepare(text);
        FontAtlas.TextMetrics metrics = atlas.measure(text);
        float drawX = x - horizontalOffset(alignment, metrics.width());
        float baseline = y + atlas.ascent() - verticalOffset(alignment, metrics.height());
        addString(atlas, text, drawX + 1f, baseline + 1f, multiplyAlpha(shadowColor.getARGB()));
        addString(atlas, text, drawX, baseline, multiplyAlpha(color.getARGB()));
    }

    void drawTextBox(FontAtlas atlas, String text, float x, float y, float width, Color color) {
        if (color == null || text == null || text.isEmpty() || scissor == null || alpha <= 0f) return;

        atlas.prepare(text);
        List<String> lines = atlas.wrapLines(text, width);
        int argb = multiplyAlpha(color.getARGB());
        float baseline = y + atlas.ascent();
        for (String line : lines) {
            if (!line.isEmpty()) {
                addString(atlas, line, x, baseline, argb);
            }
            baseline += atlas.lineHeight();
        }
    }

    private void addString(FontAtlas atlas, String text, float x, float baselineY, int argb) {
        if (text.isEmpty()) return;

        TextureSetup setup = atlas.textureSetup();
        beginTextured(setup);

        float penX = x;
        float baseline = baselineY;
        int previous = -1;

        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);

            if (codePoint == '\n' || codePoint == '\r') {
                if (codePoint == '\n') {
                    penX = x;
                    baseline += atlas.lineHeight();
                }
                previous = -1;
                continue;
            }

            FontAtlas.Glyph glyph = atlas.glyph(codePoint);

            // If atlas growth happened during glyph() (new texture created), flush the current
            // batch and start a new one with the updated texture setup. Otherwise glyphs added
            // after growth would use UVs from the larger atlas but reference the old closed
            // texture, rendering invisible (missing letters).
            TextureSetup newSetup = atlas.textureSetup();
            if (!Objects.equals(newSetup, setup)) {
                flush();
                setup = newSetup;
                beginTextured(setup);
            }

            if (previous != -1) {
                penX += atlas.kerning(previous, codePoint);
            }

            if (glyph.width() > 0 && glyph.height() > 0) {
                float x0 = penX + glyph.xOffset();
                float y0 = baseline + glyph.yOffset();
                float x1 = x0 + glyph.width();
                float y1 = y0 + glyph.height();
                addTexturedQuad(
                        x0, y0, x1, y1,
                        atlas.u0(glyph), atlas.v0(glyph),
                        atlas.u1(glyph), atlas.v1(glyph),
                        argb
                );
            }

            penX += glyph.advance();
            previous = codePoint;
        }
    }

    // ------------------------------------------------------------------
    // Images
    // ------------------------------------------------------------------

    void renderImage(Image image, float x, float y, float w, float h, float[] radii, float imageAlpha) {
        if (image == null || scissor == null || alpha <= 0f) return;

        TextureSetup setup = image.textureSetup();
        if (setup == null) return;

        beginTextured(setup);
        addTexturedRoundedRect(x, y, w, h, radii, 0f, 0f, 1f, 1f, multiplyAlpha(white(imageAlpha)));
    }

    void renderImage(Image image, int texWidth, int texHeight, float subX, float subY, float subW, float subH,
                     float x, float y, float w, float h, float radius) {
        if (image == null || texWidth <= 0 || texHeight <= 0 || scissor == null || alpha <= 0f) return;

        TextureSetup setup = image.textureSetup();
        if (setup == null) return;

        beginTextured(setup);

        float u0 = subX / texWidth;
        float v0 = subY / texHeight;
        float u1 = (subX + subW) / texWidth;
        float v1 = (subY + subH) / texHeight;

        addTexturedRoundedRect(x, y, w, h, radius <= 0f ? null : uniform(radius), u0, v0, u1, v1, multiplyAlpha(0xFFFFFFFF));
    }

    private void addTexturedRoundedRect(float x, float y, float w, float h, float[] radii,
                                        float u0, float v0, float u1, float v1, int argb) {
        if (w < 0f) {
            x += w;
            w = -w;
            float tmp = u0;
            u0 = u1;
            u1 = tmp;
        }
        if (h < 0f) {
            y += h;
            h = -h;
            float tmp = v0;
            v0 = v1;
            v1 = tmp;
        }
        if (w <= 0f || h <= 0f) return;

        if (!hasRounding(radii)) {
            addTexturedQuad(x, y, x + w, y + h, u0, v0, u1, v1, argb);
            return;
        }

        List<GuiGeometry.Point> points = new ArrayList<>();
        GuiGeometry.roundedRect(points, x, y, w, h, radii, segmentsFor(radii));
        GuiGeometry.Point center = centroid(points);

        float cu = (u0 + u1) * 0.5f;
        float cv = (v0 + v1) * 0.5f;
        for (int i = 0; i < points.size(); i++) {
            GuiGeometry.Point a = points.get(i);
            GuiGeometry.Point b = points.get((i + 1) % points.size());

            addTexturedTriangle(center, a, b,
                    cu, cv,
                    textureU(a, x, w, u0, u1), textureV(a, y, h, v0, v1),
                    textureU(b, x, w, u0, u1), textureV(b, y, h, v0, v1),
                    argb
            );
        }
    }

    // ------------------------------------------------------------------
    // Vertex emission
    // ------------------------------------------------------------------

    private void fillPolygon(List<GuiGeometry.Point> points, int argb) {
        if (points.size() < 3) return;
        GuiGeometry.Point center = centroid(points);
        for (int i = 0; i < points.size(); i++) {
            GuiGeometry.Point a = points.get(i);
            GuiGeometry.Point b = points.get((i + 1) % points.size());
            addTriangle(center, a, b, argb, argb, argb);
        }
    }

    private static GuiGeometry.Point centroid(List<GuiGeometry.Point> points) {
        float x = 0f;
        float y = 0f;
        for (GuiGeometry.Point point : points) {
            x += point.x();
            y += point.y();
        }
        return new GuiGeometry.Point(x / points.size(), y / points.size());
    }

    private void addQuad(float x0, float y0, float x1, float y1, int argb) {
        beginColored();
        addVertex(x0, y0, 0f, 0f, argb);
        addVertex(x0, y1, 0f, 0f, argb);
        addVertex(x1, y1, 0f, 0f, argb);
        addVertex(x1, y0, 0f, 0f, argb);
    }

    private void addQuad(float x0, float y0, float x1, float y1, int c0, int c1, int c2, int c3) {
        beginColored();
        addVertex(x0, y0, 0f, 0f, c0);
        addVertex(x0, y1, 0f, 0f, c1);
        addVertex(x1, y1, 0f, 0f, c2);
        addVertex(x1, y0, 0f, 0f, c3);
    }

    private void addQuad(GuiGeometry.Point a, GuiGeometry.Point b, GuiGeometry.Point c, GuiGeometry.Point d, int argb) {
        beginColored();
        addVertex(a.x(), a.y(), 0f, 0f, argb);
        addVertex(b.x(), b.y(), 0f, 0f, argb);
        addVertex(c.x(), c.y(), 0f, 0f, argb);
        addVertex(d.x(), d.y(), 0f, 0f, argb);
    }

    private void addTriangle(GuiGeometry.Point a, GuiGeometry.Point b, GuiGeometry.Point c, int ca, int cb, int cc) {
        beginColored();
        addVertex(a.x(), a.y(), 0f, 0f, ca);
        addVertex(b.x(), b.y(), 0f, 0f, cb);
        addVertex(c.x(), c.y(), 0f, 0f, cc);
        addVertex(c.x(), c.y(), 0f, 0f, cc);
    }

    private void addTriangle(GuiGeometry.Point a, GuiGeometry.Point b, GuiGeometry.Point c, int argb) {
        addTriangle(a, b, c, argb, argb, argb);
    }

    private void addTexturedQuad(float x0, float y0, float x1, float y1,
                                 float u0, float v0, float u1, float v1, int argb) {
        addVertex(x0, y0, u0, v0, argb);
        addVertex(x0, y1, u0, v1, argb);
        addVertex(x1, y1, u1, v1, argb);
        addVertex(x1, y0, u1, v0, argb);
    }

    private void addTexturedTriangle(GuiGeometry.Point a, GuiGeometry.Point b, GuiGeometry.Point c,
                                     float ua, float va, float ub, float vb, float uc, float vc, int argb) {
        addVertex(a.x(), a.y(), ua, va, argb);
        addVertex(b.x(), b.y(), ub, vb, argb);
        addVertex(c.x(), c.y(), uc, vc, argb);
        addVertex(c.x(), c.y(), uc, vc, argb);
    }

    private void addVertex(float x, float y, float u, float v, int argb) {
        batch.add(
                GuiGeometry.transformX(pose, x, y),
                GuiGeometry.transformY(pose, x, y),
                u,
                v,
                argb
        );
    }

    private void beginColored() {
        if (batch.count > 0 && batchTextured) {
            flush();
        }
        batchTextured = false;
        batchTexture = null;
    }

    private void beginTextured(TextureSetup setup) {
        if (batch.count > 0 && (!batchTextured || !Objects.equals(batchTexture, setup))) {
            flush();
        }
        batchTextured = true;
        batchTexture = setup;
    }

    private void flush() {
        if (batch.count == 0) return;

        ScreenRectangle bounds = boundsOf(batch);
        if (bounds == null) {
            // Bounds computation failed (e.g. empty intersection). Use an empty bounds so the
            // element is still tracked for layering but won't produce visible output outside
            // the scissor area. Do NOT silently drop the batch.
            bounds = ScreenRectangle.empty();
        }
        if (scissor == null) {
            batch.reset();
            return;
        }

        float[] positions = Arrays.copyOf(batch.positions, batch.count * 2);
        float[] uvs = batchTextured ? Arrays.copyOf(batch.uvs, batch.count * 2) : null;
        int[] colors = Arrays.copyOf(batch.colors, batch.count);

        GuiElementState state = new GuiElementState(
                batchTextured ? RenderPipelines.GUI_TEXTURED : RenderPipelines.GUI,
                batchTextured ? batchTexture : TextureSetup.noTexture(),
                positions,
                uvs,
                colors,
                batch.count,
                scissor,
                bounds
        );
        gfx.guiRenderState.addGuiElement(state);

        batch.reset();
    }

    private ScreenRectangle boundsOf(Batch batch) {
        if (scissor == null || batch.count == 0) return null;

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (int i = 0; i < batch.count; i++) {
            float x = batch.positions[i * 2];
            float y = batch.positions[i * 2 + 1];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        int x0 = (int) Math.floor(minX);
        int y0 = (int) Math.floor(minY);
        int x1 = (int) Math.ceil(maxX);
        int y1 = (int) Math.ceil(maxY);
        if (x0 >= x1 || y0 >= y1) return null;

        ScreenRectangle rectangle = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0);
        return rectangle.intersection(scissor);
    }

    private ScreenRectangle screenRectangle(float x, float y, float w, float h) {
        float x0 = GuiGeometry.transformX(pose, x, y);
        float y0 = GuiGeometry.transformY(pose, x, y);
        float x1 = GuiGeometry.transformX(pose, x + w, y + h);
        float y1 = GuiGeometry.transformY(pose, x + w, y + h);

        int left = (int) Math.floor(Math.min(x0, x1));
        int top = (int) Math.floor(Math.min(y0, y1));
        int right = (int) Math.ceil(Math.max(x0, x1));
        int bottom = (int) Math.ceil(Math.max(y0, y1));

        return new ScreenRectangle(left, top, Math.max(0, right - left), Math.max(0, bottom - top));
    }

    private static ScreenRectangle intersect(ScreenRectangle a, ScreenRectangle b) {
        if (a == null || b == null) return null;
        return a.intersection(b);
    }

    private int multiplyAlpha(int argb) {
        int a = (int) Math.clamp(((argb >>> 24) & 0xFF) * alpha, 0f, 255f);
        return (argb & 0x00FFFFFF) | (a << 24);
    }

    private static int white(float imageAlpha) {
        int a = (int) Math.clamp(imageAlpha * 255f, 0f, 255f);
        return (a << 24) | 0x00FFFFFF;
    }

    private static int gradientColor(GuiGeometry.Point point, float x, float y, float w, float h,
                                     int from, int to, Gradient direction) {
        float t;
        if (direction == Gradient.LeftToRight) {
            t = w <= 0f ? 0f : (point.x() - x) / w;
        } else {
            t = h <= 0f ? 0f : (point.y() - y) / h;
        }
        return lerpColor(from, to, Math.clamp(t, 0f, 1f));
    }

    private static int lerpColor(int from, int to, float t) {
        int a = (int) (((from >>> 24) & 0xFF) * (1f - t) + ((to >>> 24) & 0xFF) * t);
        int r = (int) (((from >>> 16) & 0xFF) * (1f - t) + ((to >>> 16) & 0xFF) * t);
        int g = (int) (((from >>> 8) & 0xFF) * (1f - t) + ((to >>> 8) & 0xFF) * t);
        int b = (int) ((from & 0xFF) * (1f - t) + (to & 0xFF) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static float textureU(GuiGeometry.Point point, float x, float w, float u0, float u1) {
        return w <= 0f ? u0 : u0 + (point.x() - x) / w * (u1 - u0);
    }

    private static float textureV(GuiGeometry.Point point, float y, float h, float v0, float v1) {
        return h <= 0f ? v0 : v0 + (point.y() - y) / h * (v1 - v0);
    }

    private static float horizontalOffset(TextAlignment alignment, float width) {
        return switch (alignment) {
            case TopLeft, CenterLeft, BottomLeft -> 0f;
            case TopMiddle, CenterMiddle, BottomMiddle -> width * 0.5f;
            case TopRight, CenterRight, BottomRight -> width;
        };
    }

    private static float verticalOffset(TextAlignment alignment, float height) {
        return switch (alignment) {
            case TopLeft, TopMiddle, TopRight -> 0f;
            case CenterLeft, CenterMiddle, CenterRight -> height * 0.5f;
            case BottomLeft, BottomMiddle, BottomRight -> height;
        };
    }

    private static boolean hasRounding(float[] radii) {
        if (radii == null) return false;
        for (float radius : radii) {
            if (radius > 0.0001f) return true;
        }
        return false;
    }

    private static int segmentsFor(float[] radii) {
        float max = 0f;
        if (radii != null) {
            for (float radius : radii) {
                max = Math.max(max, radius);
            }
        }
        return GuiGeometry.cornerSegments(max);
    }

    private static float[] uniform(float radius) {
        return new float[]{radius, radius, radius, radius};
    }

    private record State(Matrix3x2f pose, float alpha, ScreenRectangle scissor, TextAlignment alignment) {
    }

    private static final class Batch {
        private float[] positions = new float[16];
        private float[] uvs = new float[16];
        private int[] colors = new int[8];
        private int count;

        void add(float x, float y, float u, float v, int color) {
            if (count >= colors.length) {
                grow();
            }
            int offset = count * 2;
            positions[offset] = x;
            positions[offset + 1] = y;
            uvs[offset] = u;
            uvs[offset + 1] = v;
            colors[count] = color;
            count++;
        }

        void reset() {
            count = 0;
        }

        private void grow() {
            positions = Arrays.copyOf(positions, positions.length * 2);
            uvs = Arrays.copyOf(uvs, uvs.length * 2);
            colors = Arrays.copyOf(colors, colors.length * 2);
        }
    }
}
