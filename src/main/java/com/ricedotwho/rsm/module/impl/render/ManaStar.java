package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.SbStatTracker;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.module.api.settings.impl.NumberSetting;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;

@Getter
@ModuleInfo(aliases = "Mana Star", id = "mana-star", category = Category.RENDER)
public class ManaStar extends Module {
    @SuppressWarnings("unused")
    private static final ManaStar instance = new ManaStar();
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("rsmpack", "image/mana_icons.png");

    private final BooleanSetting hideFood = new BooleanSetting("Hide Food Bar", true);
    private final NumberSetting<Integer> gap = new NumberSetting<>("Gap", 0, 50, 10, 1);
    private final NumberSetting<Integer> amount = new NumberSetting<>("Amount", 0, 20, 8, 1);
    private final NumberSetting<Integer> fullX = new NumberSetting<>("Full X", 0, 50, 0, 1);
    private final NumberSetting<Integer> fullY = new NumberSetting<>("Full Y", 0, 50, 0, 1);
    private final NumberSetting<Integer> halfX = new NumberSetting<>("Half X", 0, 50, 1, 1);
    private final NumberSetting<Integer> halfY = new NumberSetting<>("Half Y", 0, 50, 0, 1);
    private final NumberSetting<Integer> emptyX = new NumberSetting<>("Empty X", 0, 50, 5, 1);
    private final NumberSetting<Integer> emptyY = new NumberSetting<>("Empty Y", 0, 50, 0, 1);

    private final HudSetting manaStarPos = new HudSetting("Mana Star Hud", new Vector2f(50, 50), 3f) {
        @Override
        protected @NonNull Vector2f render(GuiGraphicsExtractor gfx, boolean example) {
            int a = amount.getValue();
            float ratio = SbStatTracker.getStats().getMana().percent();
            float iconsToFill = ratio * a;
            float g = (gap.getValue().floatValue() - 9);
            for (int i = 0; i < a; i++) {
                float posX = (9 + g) * (a - 1 - i);
                if (i < (int) iconsToFill) {  // render full star
                    drawStar(gfx, posX, fullX.getValue().floatValue(), fullY.getValue().floatValue());
                } else if (i < iconsToFill) { // render half star
                    drawStar(gfx, posX, halfX.getValue().floatValue(), halfY.getValue().floatValue());
                } else {                      // render empty star
                    drawStar(gfx, posX, emptyX.getValue().floatValue(), emptyY.getValue().floatValue());
                }
            }
            return dims(a * (9 + g), 9);
        }
    }.isVisible(Location::isInSkyblock);

    public static boolean shouldHideFood() {
        return Location.isInSkyblock() && instance.isEnabled() && instance.hideFood.getValue();
    }

    private void drawStar(GuiGraphicsExtractor gfx, float x, float texX, float texY) {
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, (int) x, 0, texX * 9, texY * 9, 9, 9, 54, 9);
    }
}