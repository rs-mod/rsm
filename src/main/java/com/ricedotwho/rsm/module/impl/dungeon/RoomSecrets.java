package com.ricedotwho.rsm.module.impl.dungeon;

import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.type.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;

@ModuleInfo(aliases = "Room Secrets", id = "room-secrets", category = Category.DUNGEONS)
public class RoomSecrets extends Module {
    @SuppressWarnings("unused")
    private static final RoomSecrets instance = new RoomSecrets();
    public final HudSetting hud = new HudSetting("Room Secrets", new Vector2f(100, 100)) {
        @Override
        protected @NonNull Vector2f render(GuiGraphicsExtractor gfx, boolean example) {
            if (example) {
                hud.text(gfx, "1/5", 0, 0, getColour(1, 5));
                return dims("1/5");
            } else {
                var found = Dungeon.current().foundSecrets;
                var max = Dungeon.current().getInfo().secrets();
                String content = found + "/" + max;
                hud.text(gfx, content, 0, 0, getColour(found, max));
                return dims(content);
            }
        }
    }.isVisible(() -> !Dungeon.isInBoss() && Dungeon.isStarted() && Dungeon.current() != null && Dungeon.current().getInfo().secrets() != 0);

    private Color getColour(int found, int max) {
        float percent = (float) found / max;
        if (percent >= 1f) return Color.MINECRAFT_GREEN;
        if (percent > 0.5f) return Color.MINECRAFT_YELLOW;
        return Color.MINECRAFT_RED;
    }
}
