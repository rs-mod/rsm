package com.ricedotwho.rsm.module.impl.player;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.MouseInputEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.managers.EventDispatcher;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;

@ModuleInfo(aliases = "No Cursor Reset", id = "no-cursor-reset", category = Category.PLAYER)
public class NoCursorReset extends Module {
    @SuppressWarnings("unused")
    private static final NoCursorReset instance = new NoCursorReset();

    private static long millis = -1;
    private static long ticks = -1;

    public static long ignore = 0;

    @SubscribeEvent
    public void onMouseMove(MouseInputEvent.Move event) {
        if (ignore == EventDispatcher.getClientLifeTime()) event.setCancelled(true);
    }

    @SubscribeEvent
    public void onContainerClose(PacketEvent.MainReceivePre event, ClientboundContainerClosePacket packet) {
        millis = System.currentTimeMillis();
        ticks = EventDispatcher.getServerTickTime();
        ignore = 0;
    }

    public static boolean shouldNotReset() {
        if (!instance.isEnabled()) return false;
        return ticks != -1 && EventDispatcher.getServerTickTime() - ticks < 3 && System.currentTimeMillis() - millis < 250;
    }

    public static boolean resetMove() {
        boolean bl = instance.isEnabled() && ignore == EventDispatcher.getClientLifeTime();
        ignore = 0;
        return bl;
    }
}
