package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import org.joml.Vector2d;

import java.util.*;
import java.util.List;

@Getter
@ModuleInfo(aliases = "Module List", id = "ModuleList", category = Category.RENDER)
public class ModuleList extends Module {
    private final DragSetting position = new DragSetting("Module List", new Vector2d(50, 50), new Vector2d(187, 300));
    private final StringSetting titleValue = new StringSetting("Title Text", "Active Modules");
    private final ColourSetting menu1 = new ColourSetting("Menu Fill", new Colour(0,0,0, 165));
    private final ColourSetting menu2 = new ColourSetting("Menu 2", new Colour(40,40,40, 255));
    private final ColourSetting title = new ColourSetting("Title", Colour.WHITE.copy());
    private final ColourSetting movement = new ColourSetting("Movement", new Colour(85, 170, 255));
    private final ColourSetting dungeons = new ColourSetting("Dungeons", new Colour(255, 85, 85));
    private final ColourSetting player = new ColourSetting("Player", new Colour(170, 255, 85));
    private final ColourSetting render = new ColourSetting("Render", new Colour(255, 255, 0));
    private final ColourSetting other = new ColourSetting("Other", new Colour(221, 66, 245));

    private static final Map<Category, Colour> colourMap = new HashMap<>();
    private final int padding = 12;
    private final int spacing = 4;
    private final int visualPadding = 8;
    private final int headerHeight = 32;
    private final int gapAfterHeader = 8;
    private final int extraWidth = 80;
    private float maxWidth = 200;
    private Float textHeight = null;
    private Float textHeight2 = null;
    private boolean loaded = false;

    private final List<Module> modules = new ArrayList<>();
    private final List<DisplayModule> displayModules = new ArrayList<>();

    public ModuleList() {
        this.registerProperty(
                position,
                titleValue,
                menu1,
                menu2,
                title,
                movement,
                dungeons,
                player,
                render,
                other
        );

        colourMap.put(Category.MOVEMENT, movement.getValue());
        colourMap.put(Category.DUNGEONS, dungeons.getValue());
        colourMap.put(Category.PLAYER, player.getValue());
        colourMap.put(Category.RENDER, render.getValue());
        colourMap.put(Category.OTHER, other.getValue());
    }

    @Override
    public void onEnable() {
        if (mc.level == null || mc.player == null) return;
        loaded = true;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (loaded) return;
        TaskComponent.onTick(20, () -> loaded = true);
    }

    @SubscribeEvent
    public void onSecond(TimeEvent.Second event) {
        if (mc.level == null || mc.player == null || !loaded) return;
        if (textHeight == null) textHeight = NVGUtils.getTextHeight(17, NVGUtils.PRODUCT_SANS);
        if (textHeight2 == null) textHeight2 = NVGUtils.getTextHeight(20, NVGUtils.ROBOTO);
        modules.clear();
        modules.addAll(RSM.getInstance().getModuleManager().getMap().values().stream()
                .filter(m -> m.isEnabled() && !m.getInfo().alwaysDisabled())
                .filter(m -> m != this)
                .toList());

        displayModules.clear();
        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            String name = m.getName();
            displayModules.add(new DisplayModule(m, name, NVGUtils.getTextWidth(name, 17, NVGUtils.PRODUCT_SANS), i));
        }

        displayModules.sort(Comparator
                .comparingDouble((DisplayModule dm) -> -dm.width)
                .thenComparingInt(dm -> dm.index));

        maxWidth = (float) displayModules.stream()
                .mapToDouble(dm -> dm.width)
                .max()
                .orElse(200);
    }

    @SubscribeEvent
    public void onRender(Render2DEvent event) {
        if (mc.level == null || mc.player == null || textHeight == null) return;

        int lineHeight = textHeight.intValue() + spacing;
        int totalWidth = (int) maxWidth + padding * 2 + extraWidth;
        int listHeight = displayModules.size() * lineHeight + padding * 2;

        float totalHeight = listHeight - 12 + (visualPadding * 3);

        position.renderScaled(event.getGfx(), () -> {
            drawMenu(totalWidth + (visualPadding * 2), listHeight - 6);

            String titleString = titleValue.getValue();
            float titleX = visualPadding + (totalWidth / 2f);
            float titleY = visualPadding + (headerHeight / 2f) - (textHeight2 / 2f) - visualPadding;
            NVGUtils.drawCenteredText(titleString, titleX, titleY, 20, this.title.getValue(), NVGUtils.ROBOTO);

            float moduleStartY = visualPadding + headerHeight + gapAfterHeader - visualPadding;
            float centerX = visualPadding + (totalWidth / 2f);

            for (int i = 0; i < displayModules.size(); i++) {
                DisplayModule dm = displayModules.get(i);
                if (dm == null) continue;
                float textY = moduleStartY + (i * lineHeight);
                Colour categoryColor = colourMap.get(dm.module.getCategory());
                NVGUtils.drawCenteredText(dm.displayName, centerX, textY, 17, categoryColor, NVGUtils.ROBOTO);
            }
        }, totalWidth + 2, totalHeight);
    }


    private void drawMenu(int width, int height) {
        NVGUtils.drawRect(0, 0, width, height + 20, 4, this.menu1.getValue());
        NVGUtils.drawRect(0, 0, width, headerHeight, 4, this.menu2.getValue());
    }

    private record DisplayModule(Module module, String displayName, double width, int index) {

    }
}
