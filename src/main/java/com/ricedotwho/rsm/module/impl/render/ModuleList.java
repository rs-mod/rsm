//package com.ricedotwho.rsm.module.impl.render;
//
//import com.ricedotwho.rsm.RSM;
//import com.ricedotwho.rsm.data.Colour;
//import com.ricedotwho.rsm.event.api.SubscribeEvent;
//import com.ricedotwho.rsm.event.impl.client.TimeEvent;
//import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
//import com.ricedotwho.rsm.module.Module;
//import com.ricedotwho.rsm.module.api.Category;
//import com.ricedotwho.rsm.module.api.ModuleInfo;
//import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
//import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
//import com.ricedotwho.rsm.ui.clickgui.settings.impl.StringSetting;
//import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
//import lombok.Getter;
//import org.joml.Vector2d;
//
//import java.awt.*;
//import java.util.*;
//import java.util.List;
//
//@Getter
//@ModuleInfo(aliases = "Module List", id = "ModuleList", category = Category.RENDER)
//public class ModuleList extends Module {
//    private final DragSetting position = new DragSetting("Module List", new Vector2d(50, 50), new Vector2d(187, 20));
//    private final StringSetting titleValue = new StringSetting("Title Text", "Active Modules");
//    private final ColourSetting menu1 = new ColourSetting("Menu Fill", new Colour(0,0,0, 165));
//    private final ColourSetting menu2 = new ColourSetting("Menu 2", new Colour(40,40,40, 255));
//    private final ColourSetting title = new ColourSetting("Title", Colour.WHITE.copy());
//    private final ColourSetting movement = new ColourSetting("Movement", new Colour(85, 170, 255));
//    private final ColourSetting dungeons = new ColourSetting("Dungeons", new Colour(255, 85, 85));
//    private final ColourSetting player = new ColourSetting("Player", new Colour(170, 255, 85));
//    private final ColourSetting render = new ColourSetting("Render", new Colour(255, 255, 0));
//    private final ColourSetting other = new ColourSetting("Other", new Colour(221, 66, 245));
//
//    private static final Map<Category, Colour> colourMap = new HashMap<>();
//    private final int padding = 6;
//    private final int spacing = 2;
//    private final int visualPadding = 4;
//    private final int headerHeight = 16;
//    private final int gapAfterHeader = 4;
//    private final int extraWidth = 40;
//
//    private final List<Module> modules = new ArrayList<>();
//
//    public ModuleList() {
//        this.registerProperty(
//                position,
//                titleValue,
//                menu1,
//                menu2,
//                title,
//                movement,
//                dungeons,
//                player,
//                render,
//                other
//        );
//
//        colourMap.put(Category.MOVEMENT, movement.getValue());
//        colourMap.put(Category.DUNGEONS, dungeons.getValue());
//        colourMap.put(Category.PLAYER, player.getValue());
//        colourMap.put(Category.RENDER, render.getValue());
//        colourMap.put(Category.OTHER, other.getValue());
//    }
//
//    @SubscribeEvent
//    public void onSecond(TimeEvent.Second event) {
//        modules.clear();
//        modules.addAll(RSM.getInstance().getModuleManager().getMap().values().stream()
//                .filter(m -> m.isEnabled() && !m.getInfo().alwaysDisabled())
//                .filter(m -> m != this)
//                .toList());
//    }
//
//    @SubscribeEvent
//    public void onRender(Render2DEvent event) {
//        List<DisplayModule> displayModules = new ArrayList<>();
//        for (int i = 0; i < modules.size(); i++) {
//            Module m = modules.get(i);
//            String name = m.getName();
//            displayModules.add(new DisplayModule(m, name, NVGUtils.getTextWidth(name, 17, NVGUtils.PRODUCT_SANS), i));
//        }
//
//        displayModules.sort(Comparator
//                .comparingDouble((DisplayModule dm) -> -dm.width)
//                .thenComparingInt(dm -> dm.index));
//
//        float maxWidth = (float) displayModules.stream()
//                .mapToDouble(dm -> dm.width)
//                .max()
//                .orElse(100);
//
//        int lineHeight = (int) NVGUtils.getTextHeight(17, NVGUtils.PRODUCT_SANS) + spacing;
//        int totalWidth = (int) maxWidth + padding * 2 + extraWidth;
//        int listHeight = displayModules.size() * lineHeight + padding * 2;
//        float totalHeight = lineHeight + headerHeight + gapAfterHeader + visualPadding;
//
//        int x = (int) position.getPosition().x;
//        int y = (int) position.getPosition().y;
//
//        position.renderScaled(event.getGfx(), () -> {
//            drawMenu(x - visualPadding, y - visualPadding, totalWidth + (visualPadding * 2), listHeight - 6);
//
//            String titleString = "Active Modules";
//            float titleX = x + (totalWidth / 2f);
//            float titleY = y + (headerHeight / 2f) - (NVGUtils.getTextWidth(titleString, 20, NVGUtils.ROBOTO) / 2f) - visualPadding;
//            NVGUtils.drawCenteredText(titleString, titleX, titleY, 20, this.title.getValue(), NVGUtils.ROBOTO);
//
//            float moduleStartY = y + headerHeight + gapAfterHeader - visualPadding;
//            float centerX = x + (totalWidth / 2f);
//
//            for (int i = 0; i < displayModules.size(); i++) {
//                DisplayModule dm = displayModules.get(i);
//                float textY = moduleStartY + (i * lineHeight);
//                Colour categoryColor = colourMap.get(dm.module.getCategory());
//                NVGUtils.drawCenteredText(dm.displayName, centerX, textY, 17, categoryColor, NVGUtils.ROBOTO);
//            }
//        }, totalWidth, totalHeight);
//    }
//
//
//    private void drawMenu(int x, int y, int width, int height) {
//        NVGUtils.drawRect(x, y, width, height + 20, 2, this.menu1.getValue());
//        NVGUtils.drawRect(x, y, width, 16, 2, this.menu2.getValue());
//        NVGUtils.drawRect(x, y + 13, width, 3, this.menu2.getValue());
//    }
//
//    private record DisplayModule(Module module, String displayName, double width, int index) {
//
//    }
//}
