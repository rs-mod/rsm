package com.ricedotwho.rsm.managers;

public record StateRunnable(boolean canMultiRun, Runnable runnable) {
}
