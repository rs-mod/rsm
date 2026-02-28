package com.ricedotwho.rsm.module.impl.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.ricedotwho.rsm.component.impl.SbStatTracker;
import com.ricedotwho.rsm.component.impl.location.Location;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.render.render2d.Image;
import com.ricedotwho.rsm.utils.render.render2d.NVGSpecialRenderer;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import org.joml.Vector2d;
import org.lwjgl.nanovg.NanoVG;

@Getter
@ModuleInfo(aliases = "Mana Star", id = "ManaStar", category = Category.RENDER)
public class ManaStar extends Module {

    private final BooleanSetting hideFood = new BooleanSetting("Hide Food Bar", true);
    private final NumberSetting gap = new NumberSetting("Gap", 0, 50, 10, 1);
    private final NumberSetting amount = new NumberSetting("Amount", 0, 20, 8, 1);
    private final NumberSetting fullX = new NumberSetting("Full X", 0, 50, 0, 1);
    private final NumberSetting fullY = new NumberSetting("Full Y", 0, 50, 0, 1);
    private final NumberSetting halfX = new NumberSetting("Half X", 0, 50, 1, 1);
    private final NumberSetting halfY = new NumberSetting("Half Y", 0, 50, 0, 1);
    private final NumberSetting emptyX = new NumberSetting("Empty X", 0, 50, 5, 1);
    private final NumberSetting emptyY = new NumberSetting("Empty Y", 0, 50, 0, 1);
    private final DragSetting manaStarPos = new DragSetting("Mana Star Hud", new Vector2d(50, 50), new Vector2d(155.5, 19.5));


    private Image icons = null;

    public ManaStar() {
        this.registerProperty(
                hideFood,
                gap,
                amount,
                fullX,
                fullY,
                halfX,
                halfY,
                emptyX,
                emptyY,
                manaStarPos
        );
    }

    public boolean shouldHideFood() {
        return Location.isInSkyblock() && this.isEnabled() && this.hideFood.getValue();
    }

    private Image getIcons() {
        if (icons == null) {
            icons = NVGUtils.createImage("/assets/rsmpack/image/mana_icons.png", NanoVG.NVG_IMAGE_NEAREST);
        }
        return icons;
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        if (!Location.isInSkyblock()) return;

        int amount = this.amount.getValue().intValue();
        float ratio = SbStatTracker.getStats().getMana().percent();
        float iconsToFill = ratio * amount;
        float gap = (this.gap.getValue().floatValue() - 9);

        manaStarPos.renderScaled(event.getGfx(), () -> {
            // what's up team

            for (int i = 0; i < amount; i++) {

                float posX = (9 + gap) * (amount - 1 - i);

                // freaky aah func
                if (i < (int) iconsToFill) {  // render full star
                    drawStar(posX, 0, fullX.getValue().floatValue(), fullY.getValue().floatValue());
                } else if (i < iconsToFill) { // render half star
                    drawStar(posX, 0, halfX.getValue().floatValue(), halfY.getValue().floatValue());
                } else {                      // render empty star
                    drawStar(posX, 0, emptyX.getValue().floatValue(), emptyY.getValue().floatValue());
                }
            }
        }, amount * 9, 9);
    }

    private void drawStar(float x, float y, float texX, float texY) {
        NVGUtils.renderImage(getIcons(), 54, 9, texX * 9f, texY * 9f, 9f, 9f, x, y, 9, 9, 0);
    }

}
