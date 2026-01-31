package com.ricedotwho.rsm.module.impl.misc;

import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.List;

@Getter // please don't use spaces in the id
@ModuleInfo(aliases = "AJ", id = "AutoJax", category = Category.OTHER)
public class AutoJax extends Module {
    private boolean atstart = false;
    private final List<Vector2f> positions = List.of(
            //16 of these fuckers.
            new Vector2f(-90.1F, 7.0F), //1
            new Vector2f(-60.2F, -2.0F), //2
            new Vector2f(-4.5F,-1.9F),//3
            new Vector2f(4.9F,2.5F), //4
            new Vector2f(45.0F,-25.8F),//5
            new Vector2f(60.4F,-7.0F),//6
            new Vector2f(66.7F,2.2F),//7
            new Vector2f(90,-2.3F),//8
            new Vector2f(99.6F,3.1F),//9
            new Vector2f(116.2F,-6.3F),//10
            new Vector2f(123.1F,11),//11
            new Vector2f(154.6F,-2.3F),//12
            new Vector2f(174.2F,8.4F),//13
            new Vector2f(180,-2),//14
            new Vector2f(-150,-2),//15
            new Vector2f(-120,10.6F)//16
    );
    private int currentIndex = 0;
    private boolean isRunning = false;
    private int tickDelay = 5;
    private static final int TICK_DELAY = 60;
    private final Vec3 startPos = new Vec3(.5,62,-144.5);

    private static final int ROTATE_TO_CLICK_DELAY = 2; //how many ticks to wait after rotating
    private int rotateToClickTicks = 0;
    private boolean pendingClick = false;

    public AutoJax() {
        this.registerProperty(
                // todo: register settings
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

        if (unformatted.contains("Goal:")) {
//        if(atstart){
            ChatUtils.chat("Shooting All Targets in 3s.");
            isRunning = true;
            currentIndex = 0;
            tickDelay = TICK_DELAY;
            pendingClick = false;
            rotateToClickTicks = 0;
        }

        if (unformatted.contains("Sending packets too fast!")) {
            ChatUtils.chat("OH NOOOOOO! THIS IS A SIGN OF TIMER BALANCE BE SCARED!!!!");
            isRunning = false;
            currentIndex = 0;
            tickDelay = TICK_DELAY;
            pendingClick = false;
            rotateToClickTicks = 0;
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!isRunning) return;

        // If we've rotated and are waiting to click, handle that first.
        if (pendingClick) {
            if (rotateToClickTicks > 0) {
                rotateToClickTicks--;
                return;
            }

            rightClick();
            pendingClick = false;
            currentIndex++;

            // set delay before next target
            tickDelay = TICK_DELAY;
            return;
        }

        if (tickDelay > 10) {
            tickDelay--;
            return;
        }

        if (currentIndex >= positions.size()) {
            ChatUtils.chat("Finished.");
            isRunning = false;
            return;
        }

        Vector2f targetPos = positions.get(currentIndex);


        // Rotate to yaw and pitch
        player.setXRot(targetPos.y);
        player.setYRot(targetPos.x);

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
