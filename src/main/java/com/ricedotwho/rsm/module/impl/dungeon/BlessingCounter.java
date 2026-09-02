package com.ricedotwho.rsm.module.impl.dungeon;

import com.mojang.datafixers.util.Pair;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.SecretPickupEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.EventDispatcher;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.DragSetting;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.SecretType;
import com.ricedotwho.rsm.render.render2d.NVGUtils;
import com.ricedotwho.rsm.type.Color;
import com.ricedotwho.rsm.type.Pos;
import lombok.Getter;
import org.joml.Vector2d;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Getter
@ModuleInfo(aliases = "Blessing Counter", id = "blessing-counter", category = Category.DUNGEONS)
public class BlessingCounter extends Module {
    @SuppressWarnings("unused")
    private static final BlessingCounter instance = new BlessingCounter();
    private static final Pattern BLESSING = Pattern.compile("^DUNGEON BUFF! \\w{3,16} found a Blessing of (Life|) I{1,2}!$");
    private static final int MAX = 15;

    private final DragSetting pos = new DragSetting("Chest Gamble Chance", new Vector2d(50, 50), new Vector2d(60, 6));

    private final Set<Pos> locked = new HashSet<>();
    private Pair<Pos, Long> last = null;

    private int blessings = 0;

    @SubscribeEvent
    public void onSecret(SecretPickupEvent event) {
        if (!Location.getArea().is(Island.Dungeon) || event.getType() != SecretType.CHEST) return;
        last = new Pair<>(event.getPos(), EventDispatcher.getTotalWorldTime());
    }

    @SubscribeEvent
    public void onChat(ChatEvent.Chat event) {
        if (!Location.getArea().is(Island.Dungeon)) return;

        if ("That chest is locked!".equals(event.getString()) && last != null) {
            if (System.currentTimeMillis() - EventDispatcher.getTotalWorldTime() > 20) {
                last = null;
                return;
            }
            locked.add(last.getFirst());
        } else if (BLESSING.matcher(event.getString()).find()) {
            blessings++;
        }
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        if (mc.player == null || mc.level == null) return;
        pos.renderScaledGFX(event.getGfx(), () -> event.getGfx().text(mc.font, blessings + "/" + MAX, 0, 0, Color.WHITE.getARGB(), false), 65, 6.5f);
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        locked.clear();
        last = null;
        blessings = 0;
    }
}
