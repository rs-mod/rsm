package com.ricedotwho.rsm.module.impl.render.hud;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.impl.dungeon.puzzle.TicTacToe;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Vector2d;

@Getter
@ModuleInfo(aliases = "Hud", id = "Hud", category = Category.RENDER)
public class Hud extends Module {
    private final DragSetting title = new DragSetting("Title", new Vector2d(50, 50), new Vector2d(150, 30));
    private final DragSetting subTitle = new DragSetting("Subtitle", new Vector2d(50, 50), new Vector2d(100, 20));
    private final GroupSetting<TimeHud> timeHud = new GroupSetting<>("Time", new TimeHud(this));

    private static TitleInfo titleInfo = null;
    private static TitleInfo subTitleInfo = null;

    public Hud() {
        this.registerProperty(
                title,
                subTitle,
                timeHud
        );
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        if (titleInfo != null) {
            title.renderScaled(event.getGfx(), () -> {
                NVGUtils.drawCenteredText(titleInfo.content, 75, 0, 24, titleInfo.colour, NVGUtils.JOSEFIN);
                if (titleInfo.isExpired()) {
                    titleInfo = null;
                }
            }, 150, 30);
        }
        if (subTitleInfo != null) {
            subTitle.renderScaled(event.getGfx(), () -> {
                NVGUtils.drawCenteredText(subTitleInfo.content, 75, 0, 16, subTitleInfo.colour, NVGUtils.JOSEFIN);
                if (subTitleInfo.isExpired()) {
                    subTitleInfo = null;
                }
            }, 150, 30);
        }
    }

    public static void showTitle(String content, Colour colour, long duration) {
        showTitle(content, colour, duration, false);
    }

    public static void showTitle(String content, Colour colour, long duration, boolean override) {
        if (titleInfo == null || override) {
            titleInfo = new TitleInfo(content, colour, duration);
        }
    }

    public static void showSubTitle(String content, Colour colour, long duration) {
        showSubTitle(content, colour, duration, false);
    }

    public static void showSubTitle(String content, Colour colour, long duration, boolean override) {
        if (subTitleInfo == null || override) {
            subTitleInfo = new TitleInfo(content, colour, duration);
        }
    }

    @AllArgsConstructor
    private static class TitleInfo {
        public final String content;
        public final Colour colour;
        public final long duration;
        private final long startedAt = System.currentTimeMillis();

        public boolean isExpired() {
            return System.currentTimeMillis() - startedAt > duration;
        }
    }
}
