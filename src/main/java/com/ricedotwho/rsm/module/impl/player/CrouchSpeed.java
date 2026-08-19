package com.ricedotwho.rsm.module.impl.player;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.module.api.settings.impl.NumberSetting;
import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;

@Getter
@ModuleInfo(aliases = "Crouch Speed", id = "CrouchAnimation", category = Category.PLAYER)
public class CrouchSpeed extends Module {
    @SuppressWarnings("unused")
    private static final CrouchSpeed instance = new CrouchSpeed();
    private final BooleanSetting doubleSneak = new BooleanSetting("Double Sneak Fix", true);
    private final NumberSetting<Float> speed = new NumberSetting<>("Speed", 0.01f, 1f, 0.75f, 0.01f);

    @SubscribeEvent
    public void onPacket(PacketEvent.MainReceivePre event, ClientboundSetEntityDataPacket packet) {

        packet.packedItems().removeIf(value -> value.id() == 6);
    }

    public static Float getFactor() {
        if (!instance.isEnabled()) return null;
        return instance.speed.getValue();
    }
}
