package com.ricedotwho.rsm.component.impl;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.api.ModComponent;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import lombok.Getter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timer extends ModComponent {
    static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean init = false;

    public Timer() {
        super("TimerComponent");
        if (init) return;
        init = true;

        ScheduledExecutorService initScheduler = Executors.newSingleThreadScheduledExecutor();

        initScheduler.scheduleAtFixedRate(() -> {
            if (mc.player != null && mc.level != null) {

                scheduler.scheduleAtFixedRate(milli, 0, 1, TimeUnit.MILLISECONDS);
                scheduler.scheduleAtFixedRate(second, 0, 1, TimeUnit.SECONDS);
//                scheduler.scheduleAtFixedRate(minute, 0, 1, TimeUnit.MINUTES);

                initScheduler.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static final Runnable milli = () -> new TimeEvent.Millisecond(System.currentTimeMillis()).post();
    private static final Runnable second = () -> new TimeEvent.Second().post();
//    private static final Runnable minute = () -> RSM.post(new TimeEvent.Minute());
}
