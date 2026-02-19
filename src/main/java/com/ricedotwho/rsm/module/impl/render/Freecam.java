package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.component.impl.camera.CameraHandler;
import com.ricedotwho.rsm.component.impl.camera.CameraPositionProvider;
import com.ricedotwho.rsm.component.impl.camera.ClientRotationHandler;
import com.ricedotwho.rsm.component.impl.camera.ClientRotationProvider;
import com.ricedotwho.rsm.data.Keybind;
import com.ricedotwho.rsm.data.Pos;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render3DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.KeybindSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.NumberSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.RotationUtils;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

@Getter
@ModuleInfo(aliases = "Freecam", id = "Freecam", category = Category.RENDER)
public class Freecam extends Module implements ClientRotationProvider, CameraPositionProvider {

    private static final String ENABLE_MSG = "Freecam " + ChatFormatting.GREEN + "enabled!";
    private static final String DISABLE_MSG = "Freecam " + ChatFormatting.RED + "disabled!";

    private final KeybindSetting toggle = new KeybindSetting("Freecam Toggle", new Keybind(null, this::toggleState));
    private final NumberSetting horizontalSpeed = new NumberSetting("Horizontal Speed", 0d, 1d, 0.35d, 0.05d);
    private final NumberSetting verticalSpeed = new NumberSetting("Vertical Speed", 0d, 0.5d, 0.25d, 0.025d);

    private static Freecam INSTANCE;

    private Pos freecamPos = new Pos();

    private boolean state = false;

    public Freecam() {
        this.registerProperty(
                toggle,
                horizontalSpeed,
                verticalSpeed
        );
    }



    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        state = false;
    }

    @SubscribeEvent
    public void onRenderWorld(Render3DEvent.Start event) {
        // Delta time smth smth
        if (Minecraft.getInstance().getCameraEntity() == null) return;
        Options options = Minecraft.getInstance().options;
        boolean up = options.keyUp.isDown();
        boolean down = options.keyDown.isDown();
        boolean left = options.keyLeft.isDown();
        boolean right = options.keyRight.isDown();

        float x = RotationUtils.calculateImpulse(up, down);
        float y = RotationUtils.calculateImpulse(left, right);
        Vec2 hori = Vec2.ZERO;
        if (!(x == 0 && y == 0)) {
            // They have to be switched
            hori = RotationUtils.rotateVector(y, x, -ClientRotationHandler.getClientYaw()).normalized().scale(horizontalSpeed.getValue().floatValue());
        }

        float vertical = RotationUtils.calculateImpulse(options.keyJump.isDown(), options.keyShift.isDown()) * verticalSpeed.getValue().floatValue();

        freecamPos.selfAdd(hori.x, vertical, hori.y);
    }

    // Forces rendering of player entity
    // MixinGameRenderer

    // Stops game from culling chunks
    // MixinVisGraph
    public static boolean isDetached() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.state;
    }

    private void toggleState() {
        if (!state && Minecraft.getInstance().player == null) return;

        Minecraft.getInstance().execute(Minecraft.getInstance().levelRenderer::allChanged);
        this.state = !state;
        if (!state) {
            ChatUtils.chat(DISABLE_MSG);
            return;
        }

        ChatUtils.chat(ENABLE_MSG);
        if (INSTANCE == null) INSTANCE = RSM.getModule(Freecam.class);
        this.freecamPos = new Pos(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
        CameraHandler.registerProvider(this);
        ClientRotationHandler.registerProvider(this);
    }


    @Override
    public boolean shouldOverridePosition() {
        return this.isEnabled() && state;
    }

    @Override
    public boolean shouldOverrideHitPos() {
        return false;
    }

    @Override
    public boolean shouldOverrideHitRot() {
        return false;
    }

    @Override
    public boolean shouldBlockKeyboardMovement() {
        return true;
    }

    @Override
    public Vec3 getCameraPosition() {
        return freecamPos.asVec3();
    }

    @Override
    public Vec3 getPosForHit() {
        return null;
    }

    @Override
    public Vec3 getRotForHit() {
        return null;
    }

    @Override
    public boolean isClientRotationActive() {
        return this.isEnabled() && state;
    }

    @Override
    public boolean allowClientKeyInputs() {
        return false;
    }
}
