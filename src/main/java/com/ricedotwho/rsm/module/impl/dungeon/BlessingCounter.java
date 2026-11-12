package com.ricedotwho.rsm.module.impl.dungeon;

import com.mojang.datafixers.util.Pair;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.SecretPickupEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.location.Island;
import com.ricedotwho.rsm.location.Location;
import com.ricedotwho.rsm.managers.EventDispatcher;
import com.ricedotwho.rsm.managers.dungeon.Dungeon;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.SecretType;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

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

    private final HudSetting pos = new HudSetting("Chest Gamble Chance", new Vector2f(50, 50)) {

        @Override
        protected @NotNull Vector2f render(GuiGraphicsExtractor gfx, boolean example) {
            return dims(text(gfx, blessings + "/" + MAX, 0, 0));
        }
    }.isVisible(() -> Location.getArea().is(Island.Dungeon) && !Dungeon.isInBoss());

    private final Set<Vec3> locked = new HashSet<>();
    private Pair<Vec3, Long> last = null;

    private int blessings = 0;

    @SubscribeEvent
    public void onSecret(SecretPickupEvent event) {
        if (!Location.getArea().is(Island.Dungeon) || event.getType() != SecretType.CHEST) return;
        last = new Pair<>(event.getVec3(), EventDispatcher.getTotalWorldTime());
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
    public void onLoad(WorldEvent.Load event) {
        locked.clear();
        last = null;
        blessings = 0;
    }
}
