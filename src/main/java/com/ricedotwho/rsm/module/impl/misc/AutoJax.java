package com.ricedotwho.rsm.module.impl.misc;

import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ServerTickEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.List;

@Getter // please don't use spaces in the id
@ModuleInfo(aliases = "AJ", id = "AutoJax", category = Category.OTHER)
public class AutoJax extends Module {
    private final NumberSetting tickdelay = new NumberSetting("Tick Delay", 1, 60, 10, 2);
    private final NumberSetting startDelay = new NumberSetting("Start Delay", 1, 260, 60, 2);
    private final NumberSetting shootAfterDelay = new NumberSetting("Rotate -> Shoot delay", 1, 60, 10, 2);
    private boolean atstart = false;
    private final List<Vector2f> positions = List.of(
            //16 of these fuckers. <- was. Now its 14 in the new hub. (weird change btw wtv).
            new Vector2f(0, -1.8F), //1
            new Vector2f(30.3F, -1.8F), //2
            new Vector2f(59.4F,11.4F),//3
            new Vector2f(90F,5.1F), //4
            new Vector2f(120.2F,-2.9F),//5
            new Vector2f(-135F,-26F),//6
            new Vector2f(-120.3F,-6.2F),//7
            new Vector2f(-113.2F,2.1F),//8
            new Vector2f(-90F,-2F),//9
            new Vector2f(-80.5F,3F),//10
            new Vector2f(-63.4F,-5.9F),//11
            new Vector2f(-56.9F,11.3F),//12
            new Vector2f(-25.4F,-1.7F),//13
            new Vector2f(-6.1F,8.5F)//14
    );
    private int currentIndex = 0;
    private boolean isRunning = false;
    private int tickDelay;
    private final Vec3 startPos = new Vec3(-55.5,62,-81.5);

    private static final int ROTATE_TO_CLICK_DELAY = 2; //how many ticks to wait after rotating
    private int rotateToClickTicks = 0;
    private boolean pendingClick = false;

    public AutoJax() {
        this.registerProperty(
                tickdelay,
                startDelay,
                shootAfterDelay
        );
    }

    @Override
    public void onEnable() {
        currentIndex = 0;
        isRunning = false;
        tickDelay = 0;
        pendingClick = false;
        rotateToClickTicks = 0;
    }

    @Override
    public void onDisable() {
        isRunning = false;
        pendingClick = false;
        rotateToClickTicks = 0;
    }

    @Override
    public void reset() {
        currentIndex = 0;
        isRunning = false;
        tickDelay = 0;
        pendingClick = false;
        rotateToClickTicks = 0;
    }

    @SubscribeEvent
    public void onChat(ChatEvent event) {
        if (mc.player == null) return;
        Vec3 pos = mc.player.position();

        //plain txt
        String unformatted = StringUtil.stripColor(event.getMessage().getString());

        if (pos.distanceTo(startPos) < .3) {
            ChatUtils.chat("at start area.");
            atstart = true;
        }

        if (unformatted.contains("Goal:") && atstart) {
            ChatUtils.chat("Shooting All Targets in 3s.");
            isRunning = true;
            currentIndex = 0;
            tickDelay = startDelay.getValue().intValue();
            pendingClick = false;
            rotateToClickTicks = 0;
        }

        if (unformatted.contains("Sending packets too fast!") || unformatted.contains("Cancelled!")) {
            ChatUtils.chat("GET BACK ON DA PAD! AutoJax Canceled.");
            isRunning = false;
            currentIndex = 0;
            tickDelay = startDelay.getValue().intValue();
            pendingClick = false;
            rotateToClickTicks = 0;
        }
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!isRunning) return;

        if (pendingClick) {
            if (rotateToClickTicks > 0) {
                rotateToClickTicks--;
                return;
            }

            rightClick();
            pendingClick = false;
            currentIndex++;

            // set delay before next target
            tickDelay = tickdelay.getValue().intValue();
            return;
        }

        if (currentIndex >= positions.size()) {
            ChatUtils.chat("Finished.");
            isRunning = false;
            return;
        }

        Vector2f targetPos = positions.get(currentIndex);
        if(tickDelay > 0){
            tickDelay--;
            return;
        }

        player.setXRot(targetPos.y);
        player.setYRot(targetPos.x);

        tickDelay = shootAfterDelay.getValue().intValue();
        // click :)
        pendingClick = true;
        rotateToClickTicks = ROTATE_TO_CLICK_DELAY;
    }

    private void rightClick() {
        LocalPlayer player = mc.player;
        if (player == null) return;

        player.connection.send(
                new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, player.getYRot(), player.getXRot())
        );
        player.swing(InteractionHand.MAIN_HAND);
    }
}
