package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.ModComponent;
import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.event.impl.game.ServerTickEvent;
import lombok.Getter;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.profiling.jfr.event.PacketEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerComponent extends ModComponent {
    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean init = false;
    @Getter
    private static long serverTime;

    public TimerComponent() {
        super("TimerComponent");
        if (init) return;
        init = true;

        ScheduledExecutorService initScheduler = Executors.newSingleThreadScheduledExecutor();

        initScheduler.scheduleAtFixedRate(() -> {
            if (mc.player != null && mc.level != null) {

//                scheduler.scheduleAtFixedRate(milli, 0, 1, TimeUnit.MILLISECONDS);
                scheduler.scheduleAtFixedRate(second, 0, 1, TimeUnit.SECONDS);
//                scheduler.scheduleAtFixedRate(minute, 0, 1, TimeUnit.MINUTES);

                initScheduler.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

//    private static final Runnable milli = () -> RSM.post(new TimeEvent.Millisecond());
    private static final Runnable second = () -> new TimeEvent.Second().post();
//    private static final Runnable minute = () -> RSM.post(new TimeEvent.Minute());


//    // Server tick
//    @SubscribeEvent
//    public void onServerTick(PacketEvent event) {
//        if (event.type != PacketType.RECEIVE || !(event.packet instanceof S32PacketConfirmTransaction)) return;
//        S32PacketConfirmTransaction packet = (S32PacketConfirmTransaction) event.packet;
//        if (packet.func_148888_e() || packet.getActionNumber() >= 0) return;
//        serverTime++;
//        RSM.post(new ServerTickEvent((S32PacketConfirmTransaction) event.packet, serverTime));
//    }
//
//    @SubscribeEvent
//    public void onTimeUpdate(PacketEvent event) {
//        if (event.type != PacketType.RECEIVE || !(event.packet instanceof S03PacketTimeUpdate)) return;
//        serverTime = ((S03PacketTimeUpdate) event.packet).getTotalWorldTime();
//    }
}
