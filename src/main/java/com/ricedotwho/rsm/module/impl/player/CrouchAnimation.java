package com.ricedotwho.rsm.module.impl.player;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.old.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.old.clickgui.settings.impl.NumberSetting;
import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;

@Getter
@ModuleInfo(aliases = "Crouch Speed", id = "CrouchAnimation", category = Category.PLAYER)
public class CrouchAnimation extends Module {
    @SuppressWarnings("unused")
    private static final CrouchAnimation instance = new CrouchAnimation();
    private final BooleanSetting doubleSneak = new BooleanSetting("Double Sneak Fix", true);
    private final NumberSetting speed = new NumberSetting("Speed", 0.01, 1, 0.75, 0.01);

    @SubscribeEvent
    public void onPacket(PacketEvent.MainReceivePre event) {
        if (!(event.getPacket() instanceof ClientboundSetEntityDataPacket packet)) return;
        packet.packedItems().removeIf(value -> value.id() == 6);
    }

    public static Float getFactor() {
        if (!instance.isEnabled()) return null;
        return instance.speed.getValue().floatValue();
    }
}
