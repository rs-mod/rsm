package com.ricedotwho.rsm.ui.impl.elements;

import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.ui.api.Palette;
import com.ricedotwho.rsm.ui.impl.nodes.RectangleNode;
import com.ricedotwho.rsm.ui.impl.popups.impl.colorSelector.ColorPopup;
import lombok.val;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.Nullable;

public class DualColorBoxElement extends ClickHandler {
    public DualColorBoxElement(Color leftColor, Color rightColor, @Nullable Runnable onEdit, boolean leftAlpha = true, boolean rightAlpha = true) {
        var container = new RectangleNode.Builder()
                .display(Display.FLEX)
                .flexDirection(FlexDirection.ROW)
                .justifyContent(JustifyContent.SPACE_EVENLY)
                .alignItems(Align.STRETCH)
                .build();

        container.addChild(new ColorContainer(leftColor, leftAlpha, onEdit));
        container.addChild(new ColorContainer(rightColor, rightAlpha, onEdit));

        super(container, true, false);
    }

    private static class ColorContainer extends ClickHandler {
        private final Color color;
        private final boolean alpha;
        private final Runnable onEdit;

        public ColorContainer(Color color, boolean alpha, Runnable onEdit) {
            val base = new RectangleNode.Builder()
                    .padding(Palette.elementInteriorPadding)
                    .height(Palette.largeElementHeight)
                    .aspectRatio(1.5f)
                    .color(Palette.elementBackgroundDark)
                    .outline(Palette.strokeThickness, Palette.stroke)
                    .display(Display.FLEX)
                    .flexDirection(FlexDirection.ROW)
                    .justifyContent(JustifyContent.CENTER)
                    .alignItems(Align.STRETCH)
                    .build();

            val filling = new RectangleNode.Builder()
                    .flexGrow(1f)
                    .color(color)
                    .build();

            base.addChild(filling);

            super(base, true, false);
            this.color = color;
            this.alpha = alpha;
            this.onEdit = onEdit;
        }

        private boolean requestOpenPopup = false;

        @Override
        public void frame(GuiGraphicsExtractor gfx, float parentX, float parentY, float mouseX, float mouseY, float scrollY) {
            super.frame(gfx, parentX, parentY, mouseX, mouseY, scrollY);

            if (requestOpenPopup) {
                //the 4f is just to make it not blend into the setting.
                ColorPopup.openColorPopup(
                        color,
                        parentX + this.layoutLeft() - 4f - ColorPopup.precomputedWidth,
                        parentY + this.layoutTop(), scrollY,
                        onEdit,
                        alpha

                );

                requestOpenPopup = false;
            }
        }

        @Override
        protected void onLeftTriggered() {
            requestOpenPopup = true;
        }
    }
}
