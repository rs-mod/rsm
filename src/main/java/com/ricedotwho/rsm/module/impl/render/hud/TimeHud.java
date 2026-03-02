package com.ricedotwho.rsm.module.impl.render.hud;

import com.ricedotwho.rsm.component.impl.task.TaskComponent;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
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
    private final BooleanSetting mcFont = new BooleanSetting("Minecraft Font", true);
    private final BooleanSetting shadow = new BooleanSetting("Shadow", false);
    private final BooleanSetting timeHud24h = new BooleanSetting("24 Hour", false);
    private final ColourSetting timeColour = new ColourSetting("Time Colour", Colour.WHITE);
    private final DragSetting timeHudPos = new DragSetting("Time", new Vector2d(50, 50), new Vector2d(60, 6));

    private static final SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm:ss a");

    private String content = "[00:00:00]";
    private boolean loaded = false;

    public TimeHud(Hud hud) {
        super(hud);
        this.registerProperty(
                mcFont,
                shadow,
                timeHud24h,
                timeColour,
                timeHudPos
        );
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
    public void onTick(ClientTickEvent event) {
        content = "[" + (timeHud24h.getValue() ? sdf24.format(System.currentTimeMillis()) : sdf12.format(System.currentTimeMillis())) + "]";
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        if (!loaded || mc.player == null || mc.level == null) return;
        if (mcFont.getValue()) {
            timeHudPos.renderScaledGFX(event.getGfx(), () -> event.getGfx().drawString(mc.font, content,0, 0, timeColour.getValue().getRGB(), shadow.getValue()), 65, 6.5f);
        } else {
            timeHudPos.renderScaled(event.getGfx(), () -> {
                if (shadow.getValue()) {
                    NVGUtils.drawTextShadow(content, 0, 0, 12, timeColour.getValue(), NVGUtils.JOSEFIN);
                } else {
                    NVGUtils.drawText(content, 0, 0, 12, timeColour.getValue(), NVGUtils.JOSEFIN);
                }
            }, 65, 7.5f);
        }
    }
}
