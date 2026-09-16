package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.DragSetting;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2d;

@ModuleInfo(aliases = "Room Secrets", id = "room-secrets", category = Category.DUNGEONS)
public class RoomSecrets extends Module {
    @SuppressWarnings("unused")
    private static final RoomSecrets instance = new RoomSecrets();
    public final HudSetting hud = new HudSetting("Room Secrets", new Vector2d(50, 50), new Vector2d(50, 10)) {
        @Override
        protected void draw(GuiGraphicsExtractor gfx) {
            var found = Dungeon.current().foundSecrets;
            var max = Dungeon.current().getInfo().secrets();
            String content = found + "/" + max;
            hud.renderScaledGFX(gfx, () -> hud.text(gfx, content, DragSetting.Align.LEFT, 0, 0, getColour(found, max), false));
        }
    }.shouldRender(() -> !Dungeon.isInBoss() && Dungeon.isStarted() && Dungeon.current() != null && Dungeon.current().getInfo().secrets() != 0);

    private Color getColour(int found, int max) {
        float percent = (float) found / max;
        if (percent >= 1f) return Color.MINECRAFT_GREEN;
        if (percent > 0.5f) return Color.MINECRAFT_YELLOW;
        return Color.MINECRAFT_RED;
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        this.hud.render(event.getGfx());
    }
}
