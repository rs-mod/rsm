package com.ricedotwho.rsm.managers;

import com.ricedotwho.rsm.core.GraphicsInit;
import com.ricedotwho.rsm.core.Init;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.event.impl.client.TimeEvent;
import com.ricedotwho.rsm.type.Accessor;
import lombok.experimental.UtilityClass;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class Timer implements Accessor {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @GraphicsInit
    private void init() {
        scheduler.scheduleAtFixedRate(milli, 0, 1, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    private static final Runnable milli = () -> new TimeEvent.Millisecond(System.currentTimeMillis()).post();
}
