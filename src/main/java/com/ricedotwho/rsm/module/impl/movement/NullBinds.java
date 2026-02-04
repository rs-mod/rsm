package com.ricedotwho.rsm.module.impl.movement;

import com.ricedotwho.rsm.data.Pair;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.KeyInputEvent;
import com.ricedotwho.rsm.mixins.accessor.AccessorKeyMapping;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import lombok.Getter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.KeyMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@ModuleInfo(aliases = "Null Binds", id = "NullBinds", category = Category.MOVEMENT)
public class NullBinds extends Module {

    private List<Pair<KeyMapping, KeyMapping>> opposites;

    private final Map<KeyMapping, Boolean> realState = new HashMap<>();
    private final Map<KeyMapping, Boolean> gameState = new HashMap<>();

    private boolean setup = false;

    public NullBinds() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (setup) return;
            setup = true;
            realState.put(mc.options.keyUp, false);
            realState.put(mc.options.keyDown, false);
            realState.put(mc.options.keyLeft, false);
            realState.put(mc.options.keyRight, false);

            gameState.put(mc.options.keyUp, false);
            gameState.put(mc.options.keyDown, false);
            gameState.put(mc.options.keyLeft, false);
            gameState.put(mc.options.keyRight, false);

            opposites = List.of(
                    new Pair<>(mc.options.keyUp, mc.options.keyDown),
                    new Pair<>(mc.options.keyLeft,mc.options.keyRight)
            );
        });
    }

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        if (mc.screen != null || event.getState() == KeyInputEvent.State.REPEAT) return;

        for (KeyMapping key : realState.keySet()) {
            if (((AccessorKeyMapping) key).getKey().equals(event.getKey())) {
                realState.put(key, event.isDown());
                handleStateChange(key, event.isDown());
                break;
            }
        }
    }

    private void handleStateChange(KeyMapping changedKey, boolean down) {
        for (Pair<KeyMapping, KeyMapping> p : opposites) {
            doNullBind(p.getFirst(), p.getSecond(), changedKey, down);
            doNullBind(p.getSecond(), p.getFirst(), changedKey, down);
        }

    }
    private void doNullBind(KeyMapping primary, KeyMapping opposite, KeyMapping changedKey, boolean down) {
        if (changedKey != primary) return;

        boolean oppHeld = realState.get(opposite);

        if (down) {
            if (oppHeld) {
                setDown(opposite, false);
            }
            setDown(primary, true);
        } else {
            setDown(primary, false);
            if (oppHeld) {
                setDown(opposite, true);
            }
        }
    }

    private void setDown(KeyMapping key, boolean state) {
        if (gameState.get(key) == state) return;
        key.setDown(state);
        gameState.put(key, state);
    }
}
