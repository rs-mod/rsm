package com.ricedotwho.rsm.component.impl;

import net.minecraft.network.protocol.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class PacketOrderManager {
    private static final ConcurrentHashMap<STATE, List<StateRunnable>> packets = new ConcurrentHashMap<>();
    private static final List<Predicate<Packet<?>>> receiveListeners = new ArrayList<>(4);

    public enum STATE {
        START,
        ITEM_USE,
        ATTACK
    }

    public static void onPreTickStart() {
        execute(STATE.START);
    }

    public static void register(STATE state, Runnable runnable) {
        register(state, new StateRunnable(true, runnable));
    }

    public static void register(STATE state, StateRunnable runnable) {
        synchronized (packets) {
            if (!packets.containsKey(state)) packets.put(state, new ArrayList<>());
        }

        List<StateRunnable> list = packets.get(state);
        synchronized (list) {
            list.add(runnable);
        }
    }

    public static void registerReceiveListener(Predicate<Packet<?>> listener) {
        synchronized (receiveListeners) {
            receiveListeners.add(listener);
        }
    }

    public static void onPreReceivePacket(Packet<?> packet) {
        synchronized (receiveListeners) {
            if (receiveListeners.isEmpty()) return;
            receiveListeners.removeIf(predicate -> predicate.test(packet));
        }
    }

    public static void execute(STATE state) {
        if (!packets.containsKey(state)) return;

        List<StateRunnable> runnables = packets.get(state);
        synchronized (runnables) {
            if (runnables.isEmpty()) return;
            for (int i = 0 ; i < runnables.size(); i++) {
                StateRunnable r = runnables.get(i);
                boolean bl2 = i == 0 || r.canMultiRun();
                if (!bl2) continue;
                r.runnable().run();
                runnables.remove(i--);
            }
        }
    }

}
