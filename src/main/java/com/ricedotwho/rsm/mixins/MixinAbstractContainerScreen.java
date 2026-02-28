package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// from odin https://github.com/odtheking/OdinFabric/blob/main/src/main/java/com/odtheking/mixin/mixins/AbstractContainerScreenMixin.java

@Mixin(value = AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    protected void onInit(CallbackInfo ci) {
        if (new GuiEvent.Open((Screen) (Object) this).post()) ci.cancel();
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    protected void onClose(CallbackInfo ci) {
        if (new GuiEvent.Close((Screen) (Object) this).post()) ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    protected void onRender(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (new GuiEvent.Draw((Screen) (Object) this, context, mouseX, mouseY).post()) ci.cancel();
    }

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    protected void onRenderBackground(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (new GuiEvent.DrawBackground((Screen) (Object) this, context, mouseX, mouseY).post()) ci.cancel();
    }

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (new GuiEvent.DrawSlot((Screen) (Object) this, guiGraphics, slot).post()) ci.cancel();
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClickedSlot(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
        if (new GuiEvent.SlotClick((Screen) (Object) this, slotId, button).post()) ci.cancel();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClicked(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (new GuiEvent.Click((Screen) (Object) this, click, doubled).post()) cir.cancel();
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void onMouseReleased(MouseButtonEvent mouseButtonEvent, CallbackInfoReturnable<Boolean> cir) {
        if (new GuiEvent.Release((Screen) (Object) this, mouseButtonEvent).post()) cir.cancel();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void onKeyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
        if (new GuiEvent.Key((Screen) (Object) this, input).post()) cir.cancel();
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"), cancellable = true)
    public void onDrawMouseoverTooltip(GuiGraphics context, int mouseX, int mouseY, CallbackInfo ci) {
        if (new GuiEvent.DrawTooltip((Screen) (Object) this, context, mouseX, mouseY).post()) ci.cancel();
    }
}
