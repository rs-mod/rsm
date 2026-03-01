package com.ricedotwho.rsm.module.impl.render.hud;

import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.group.DefaultGroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ColourSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.Getter;
import org.joml.Vector2d;

import java.text.SimpleDateFormat;

@Getter
@SubModuleInfo(name = "Time", alwaysDisabled = false, isEnabled = false)
public class TimeHud extends SubModule<Hud> {

    private final BooleanSetting shadow = new BooleanSetting("Shadow", false);
    private final BooleanSetting timeHud24h = new BooleanSetting("24 Hour", false);
    private final ColourSetting timeColour = new ColourSetting("Time Colour", Colour.WHITE);
    private final DragSetting timeHudPos = new DragSetting("Time", new Vector2d(50, 50), new Vector2d(75, 15));

    private static final SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm:ss a");

    private String content = "[00:00:00]";

    public TimeHud(Hud hud) {
        super(hud);
        this.registerProperty(
                shadow,
                timeHud24h,
                timeColour,
                timeHudPos
        );
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        content = "[" + (timeHud24h.getValue() ? sdf24.format(System.currentTimeMillis()) : sdf12.format(System.currentTimeMillis())) + "]";
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        timeHudPos.renderScaled(event.getGfx(), () -> {
            if (shadow.getValue()) {
                NVGUtils.drawTextShadow(content, 0, 0, 12, timeColour.getValue(), NVGUtils.JOSEFIN);
            } else {
                NVGUtils.drawText(content, 0, 0, 12, timeColour.getValue(), NVGUtils.JOSEFIN);
            }
        }, 150, 30);
    }
}
