package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.ColorSetting;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.module.api.settings.impl.StringSetting;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.render.render2d.font.Fonts;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ModuleInfo(aliases = "Module List", id = "module-list", category = Category.RENDER)
public class ModuleList extends Module {
    @SuppressWarnings("unused")
    private final static ModuleList instance = new ModuleList();
    private final StringSetting titleValue = new StringSetting("Title Text", "Active Modules");
    private final ColorSetting menu1 = new ColorSetting("Menu Fill", Color.fromRGB(0,0,0, 0.65f));
    private final ColorSetting menu2 = new ColorSetting("Menu 2", Color.fromRGB(40,40,40, 1f));
    private final ColorSetting title = new ColorSetting("Title", Color.WHITE.clone());
    private final ColorSetting movement = new ColorSetting("Movement", Color.fromRGB(85, 170, 255));
    private final ColorSetting dungeons = new ColorSetting("Dungeons", Color.fromRGB(255, 85, 85));
    private final ColorSetting player = new ColorSetting("Player", Color.fromRGB(170, 255, 85));
    private final ColorSetting render = new ColorSetting("Render", Color.fromRGB(255, 255, 0));
    private final ColorSetting other = new ColorSetting("Other", Color.fromRGB(221, 66, 245));

    private final Map<Category, Color> colorMap = new HashMap<>();
    private final int padding = 12;
    private final int spacing = 4;
    private final int visualPadding = 8;
    private final int headerHeight = 32;
    private final int gapAfterHeader = 8;
    private final int extraWidth = 80;
    private final float MAX_WIDTH = 200;
    private Float textHeight = null;

    public ModuleList() {
        colorMap.put(Category.MOVEMENT, movement.getValue());
        colorMap.put(Category.DUNGEONS, dungeons.getValue());
        colorMap.put(Category.PLAYER, player.getValue());
        colorMap.put(Category.RENDER, render.getValue());
        colorMap.put(Category.OTHER, other.getValue());
    }

    private final HudSetting position = new HudSetting("Module List", new Vector2f(50, 50), 1f) {
        @Override
        protected @NonNull Vector2f render(GuiGraphicsExtractor gfx, boolean example) {
            if (textHeight == null) textHeight = Fonts.height(Fonts.PRODUCT_SANS, 17);

            List<Module> modules = RSM.getInstance().getModuleManager().getModules().stream()
                    .filter(m -> m.isEnabled() && !m.getInfo().alwaysDisabled())
                    .filter(m -> m != ModuleList.this)
                    .sorted(Comparator.comparing(it -> Fonts.width(Fonts.PRODUCT_SANS, it.getName(), 17)))
                    .toList().reversed();

            int lineHeight = textHeight.intValue() + spacing;
            int totalWidth = (int) MAX_WIDTH + padding * 2 + extraWidth;
            int listHeight = modules.size() * lineHeight + padding * 2;

            float totalHeight = listHeight - 12 + (visualPadding * 3);

            drawMenu(gfx, totalWidth + (visualPadding * 2), listHeight - 6);

            String titleString = titleValue.getValue();
            float titleX = visualPadding + (totalWidth / 2f);
            float titleY = visualPadding + (headerHeight / 2f) - visualPadding;
            Fonts.centeredText(gfx, Fonts.ROBOTO, titleString, titleX, titleY, 20, title.getValue());

            float moduleStartY = visualPadding + headerHeight + gapAfterHeader - visualPadding;
            float centerX = visualPadding + (totalWidth / 2f);


            for (int i = 0; i < modules.size(); i++) {
                Module module = modules.get(i);
                if (module == null) continue;
                float textY = moduleStartY + (i * lineHeight);
                Color categoryColor = colorMap.get(module.getCategory());
                Fonts.centeredText(gfx, Fonts.PRODUCT_SANS, module.getName(), centerX, textY, 17, categoryColor);
            }

            return dims(totalWidth + (visualPadding * 2), totalHeight);
        }
    };

    private void drawMenu(GuiGraphicsExtractor gfx, int width, int height) {
        Render2D.rect(gfx, 0, 0, width, height + 20, this.menu1.getValue(), 4);
        Render2D.rect(gfx, 0, 0, width, headerHeight, this.menu2.getValue(), 4);
    }
}
