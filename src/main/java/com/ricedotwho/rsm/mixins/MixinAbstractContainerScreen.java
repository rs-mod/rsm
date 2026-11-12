package com.ricedotwho.rsm.mixins;

import com.ricedotwho.rsm.event.impl.game.GuiEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * Modified, original code Copyright (c) 2026, odtheking (https://github.com/odtheking/OdinFabric/blob/main/src/main/java/com/odtheking/mixin/mixins/AbstractContainerScreenMixin.java)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

@Mixin(value = AbstractContainerScreen.class)
public class MixinAbstractContainerScreen {

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    protected void rsm$onInit(CallbackInfo ci) {
        if (new GuiEvent.Open((Screen) (Object) this).post()) ci.cancel();
    }

    @Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
    protected void rsm$onClose(CallbackInfo ci) {
        if (new GuiEvent.Close((Screen) (Object) this).post()) ci.cancel();
    }

    @Inject(method = "extractSlot", at = @At("HEAD"), cancellable = true)
    private void rsm$onDrawSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        if (new GuiEvent.DrawSlot((Screen) (Object) this, graphics, slot).post()) ci.cancel();
    }

    @Inject(method = "extractSlots", at = @At("TAIL"), cancellable = true)
    private void rsm$onDrawSlots(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (new GuiEvent.PostDrawSlots((Screen) (Object) this, graphics, mouseX, mouseY).post()) ci.cancel();
    }

    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("HEAD"), cancellable = true)
    public void rsm$onMouseClickedSlot(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        if (new GuiEvent.SlotClick((Screen) (Object) this, slotId, buttonNum, containerInput).post()) ci.cancel();
    }

    @Inject(method = "slotClicked(Lnet/minecraft/world/inventory/Slot;IILnet/minecraft/world/inventory/ContainerInput;)V", at = @At("TAIL"))
    public void rsm$onMouseClickedSlotPost(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        new GuiEvent.PostSlotClick((Screen) (Object) this, slotId, buttonNum, containerInput).post();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void rsm$onMouseClicked(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (new GuiEvent.Click((Screen) (Object) this, event, doubleClick).post()) cir.cancel();
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    public void rsm$onMouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (new GuiEvent.Release((Screen) (Object) this, event).post()) cir.cancel();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void rsm$onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (new GuiEvent.Key((Screen) (Object) this, event).post()) cir.cancel();
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    public void rsm$onDrawMouseoverTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (new GuiEvent.DrawTooltip((Screen) (Object) this, graphics, mouseX, mouseY).post()) ci.cancel();
    }
}
