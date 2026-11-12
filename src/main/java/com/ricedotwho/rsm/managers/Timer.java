package com.ricedotwho.rsm.managers;

import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.type.Accessor;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@UtilityClass
public class Timer implements Accessor {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Init
    private void init() {
        ScheduledExecutorService initScheduler = Executors.newSingleThreadScheduledExecutor();

        initScheduler.scheduleAtFixedRate(() -> {
            if (mc.player != null && mc.level != null) {
                scheduler.scheduleAtFixedRate(milli, 0, 1, TimeUnit.MILLISECONDS);
                initScheduler.shutdown();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }


    private static final Runnable milli = () -> new TimeEvent.Millisecond(System.currentTimeMillis()).post();
}
