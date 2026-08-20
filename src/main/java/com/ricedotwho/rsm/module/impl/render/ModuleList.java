package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.ColorSetting;
import com.ricedotwho.rsm.module.api.settings.impl.DragSetting;
import com.ricedotwho.rsm.module.api.settings.impl.StringSetting;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import lombok.Getter;
import org.joml.Vector2d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ModuleInfo(aliases = "Module List", id = "ModuleList", category = Category.RENDER)
public class ModuleList extends Module {
    @SuppressWarnings("unused")
    private final static ModuleList instance = new ModuleList();
    private final DragSetting position = new DragSetting("Module List", new Vector2d(50, 50), new Vector2d(187, 300));
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
    private Float textHeight2 = null;

    public ModuleList() {
        colorMap.put(Category.MOVEMENT, movement.getValue());
        colorMap.put(Category.DUNGEONS, dungeons.getValue());
        colorMap.put(Category.PLAYER, player.getValue());
        colorMap.put(Category.RENDER, render.getValue());
        colorMap.put(Category.OTHER, other.getValue());
    }

    @SubscribeEvent
    private void onRender(Render2DEvent event) {
        if (mc.level == null || mc.player == null) return;

        if (textHeight == null) textHeight = NVGUtils.getTextHeight(17, NVGUtils.getFont(NVGUtils.PRODUCT_SANS));
        if (textHeight2 == null) textHeight2 = NVGUtils.getTextHeight(20, NVGUtils.getFont(NVGUtils.ROBOTO));

        List<Module> modules = RSM.getInstance().getModuleManager().getModules().stream()
                .filter(m -> m.isEnabled() && !m.getInfo().alwaysDisabled())
                .filter(m -> m != this)
                .toList();

        int lineHeight = textHeight.intValue() + spacing;
        int totalWidth = (int) MAX_WIDTH + padding * 2 + extraWidth;
        int listHeight = modules.size() * lineHeight + padding * 2;

        float totalHeight = listHeight - 12 + (visualPadding * 3);

        position.renderScaled(event.getGfx(), () -> {
            drawMenu(totalWidth + (visualPadding * 2), listHeight - 6);

            String titleString = titleValue.getValue();
            float titleX = visualPadding + (totalWidth / 2f);
            float titleY = visualPadding + (headerHeight / 2f) - (textHeight2 / 2f) - visualPadding;
            NVGUtils.drawCenteredText(titleString, titleX, titleY, 20, this.title.getValue(), NVGUtils.getFont(NVGUtils.ROBOTO));

            float moduleStartY = visualPadding + headerHeight + gapAfterHeader - visualPadding;
            float centerX = visualPadding + (totalWidth / 2f);


            for (int i = 0; i < modules.size(); i++) {
                Module module = modules.get(i);
                if (module == null) continue;
                float textY = moduleStartY + (i * lineHeight);
                Color categoryColor = colorMap.get(module.getCategory());
                NVGUtils.drawCenteredText(module.getName(), centerX, textY, 17, categoryColor, NVGUtils.getFont(NVGUtils.ROBOTO));
            }
        }, totalWidth + 2, totalHeight);
    }

    private void drawMenu(int width, int height) {
        NVGUtils.drawRect(0, 0, width, height + 20, 4, this.menu1.getValue());
        NVGUtils.drawRect(0, 0, width, headerHeight, 4, this.menu2.getValue());
    }
}
