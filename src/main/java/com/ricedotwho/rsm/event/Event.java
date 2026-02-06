package com.ricedotwho.rsm.event;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.event.api.Cancellable;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;

public class Event {
    @Getter
    private boolean cancelled;

    public final boolean isCancellable() {
        return this.getClass().isAnnotationPresent(Cancellable.class);
    }

    public void setCancelled(boolean value) {
        if (isCancellable()) {
            cancelled = value;
        } else {
            // should this throw an error or keep silent?
            RSM.getLogger().error("Attempted to cancel uncancellable event! {}", this.getClass().getSimpleName());
        }
    }

    public boolean post() {
        return RSM.getInstance().getEventBus().post(this);
    }
}
