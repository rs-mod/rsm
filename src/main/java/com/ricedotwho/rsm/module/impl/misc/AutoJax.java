package com.ricedotwho.rsm.module.impl.misc;

import com.ricedotwho.rsm.event.annotations.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import com.ricedotwho.rsm.event.impl.game.ClientTickEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.utils.ChatUtils;
import lombok.Getter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import java.util.List;

@Getter // please don't use spaces in the id
@ModuleInfo(aliases = "AJ", id = "AutoJax", category = Category.OTHER)
public class AutoJax extends Module {
    private boolean atstart = false;
    private final List<Vec3> positions = List.of(
            //15 of these fuckers. theres actually 16..
            new Vec3(7, 62,-145.500), //1
            new Vec3(7, 63,-141.500), //2
            new Vec3(1.5,63,-130.500),//3
            new Vec3(-1,62,-131.500), //4
            new Vec3(-9.5,65,-138.500),//5
            new Vec3(-11.5,62,-139.500),//6
            new Vec3(-11.5,63,-144.500),//7
            new Vec3(-9.5,62,-146.500),//8
            new Vec3(-8.5,64,-148.500),//9
            new Vec3(-9.5,60,-151.500),//10
            new Vec3(-4.5,63,-155.500),//11
            new Vec3(-0.5,61,-155.500),//12
            new Vec3(0.5,63,-155.500),//13
            new Vec3(5.5,63,-153.5),//14
            new Vec3(7.5,61,-148.5)//15
    );

    // TODO: FIX WHERE ITS LOOKING. IT LOOKS EITHER TOO FAR UPWARDS OR TOO FAR DOWNWARDS!
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
            ChatUtils.chat("Shooting All Targets in 3s.");
            isRunning = true;
            currentIndex = 0;
            tickDelay = TICK_DELAY;
            pendingClick = false;
            rotateToClickTicks = 0;
        }

        if (unformatted.contains("Sending packets too fast!")) {
            ChatUtils.chat("OH NOOOOOO");
            isRunning = false;
            currentIndex = 0;
            tickDelay = TICK_DELAY;
            pendingClick = false;
            rotateToClickTicks = 0;
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Start event) {
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

        Vec3 targetPos = positions.get(currentIndex);

        // Rotate now...
        lookAt(targetPos.x, targetPos.y, targetPos.z);

        // ...then click a few ticks later.
        pendingClick = true;
        rotateToClickTicks = ROTATE_TO_CLICK_DELAY;
    }

    private void lookAt(double targetX, double targetY, double targetZ) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        //calculate differences from users eye pos
        double deltaX = targetX - player.getX();
        double deltaY = targetY - player.getEyeY();
        double deltaZ = targetZ - player.getZ();

        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        float pitch = (float) Math.toDegrees(Math.atan2(deltaY, horizontalDistance));
        pitch = Math.max(-90.0f, Math.min(pitch, 90.0f));

        player.setYRot(yaw);
        player.setXRot(pitch);
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
