package com.ricedotwho.rsm.IMixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;

public interface IMultiPlayerGameMode {
    void sendPacketSequenced(ClientLevel world, PredictiveAction packetCreator);
    void syncSlot();
}
