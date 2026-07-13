package com.ricedotwho.rsm.module.impl.player;

import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.PacketEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.BooleanSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import lombok.Getter;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;

@Getter
@ModuleInfo(aliases = "Crouch Speed", id = "CrouchAnimation", category = Category.PLAYER)
public class CrouchAnimation extends Module {
    private static CrouchAnimation INSTANCE;
    private final BooleanSetting doubleSneak = new BooleanSetting("Double Sneak Fix", true);
    private static final NumberSetting speed = new NumberSetting("Speed", 0.01, 1, 0.75, 0.01);

    public CrouchAnimation() {
        INSTANCE = this;
        this.registerProperty(
                doubleSneak,
                speed
        );
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Receive event) {
        if (!(event.getPacket() instanceof ClientboundSetEntityDataPacket packet)) return;
        packet.packedItems().removeIf(value -> value.id() == 6);
    }

    public static Float getFactor() {
        if (!INSTANCE.isEnabled()) return null;
        return speed.getValue().floatValue();
    }
}
