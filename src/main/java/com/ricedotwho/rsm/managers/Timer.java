package com.ricedotwho.rsm.managers;

import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.ricedotwho.rsm.type.Accessor.mc;


@UtilityClass
public class Timer {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Init
    private void init() {
        ScheduledExecutorService initScheduler = Executors.newSingleThreadScheduledExecutor();

        initScheduler.scheduleAtFixedRate(() -> {
            if (mc.player != null && mc.level != null) {

                scheduler.scheduleAtFixedRate(milli, 0, 1, TimeUnit.MILLISECONDS);
                scheduler.scheduleAtFixedRate(second, 0, 1, TimeUnit.SECONDS);
//                scheduler.scheduleAtFixedRate(minute, 0, 1, TimeUnit.MINUTES);

                initScheduler.shutdown();
                initScheduler.close();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }


    private static final Runnable milli = () -> new TimeEvent.Millisecond(System.currentTimeMillis()).post();
    private static final Runnable second = () -> new TimeEvent.Second().post();
//    private static final Runnable minute = () -> RSM.post(new TimeEvent.Minute());
}
