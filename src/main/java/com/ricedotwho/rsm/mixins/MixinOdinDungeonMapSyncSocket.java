package com.ricedotwho.rsm.mixins;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ricedotwho.rsm.managers.dungeon.map.DungeonScanner;
import com.ricedotwho.rsm.module.impl.dungeon.waypoint.DungeonWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.Reader;

@Mixin(targets = "com.odtheking.odin.features.impl.dungeon.map.DungeonMap$syncSocket$1$1")
public class MixinOdinDungeonMapSyncSocket {

    @Inject(method = "invoke(Ljava/lang/String;)V", at = @At("HEAD"))
    private void onMessage(String message, CallbackInfo ci) {
        if (!DungeonWaypoint.getInstance().getOdinSocket().getValue()) return;
        JsonElement object = JsonParser.parseReader(Reader.of(message));
        if (object == null || !object.isJsonObject) return;
        DungeonScanner.parseSocketData(object.getAsJsonObject());
    }
}
