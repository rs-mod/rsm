package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import com.ricedotwho.rsm.module.impl.render.FullBright;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GlDevice.class)
public class GlDeviceMixin {

    /**
     * @author DocilElm
     * <a href="https://github.com/Synnerz/devonian/blob/1.21.11/src/main/java/com/github/synnerz/devonian/mixin/GlDeviceMixin.java">From Devonian</a>
     */

    @WrapOperation(
        method = "compileShader",
        at = @At(
                value = "INVOKE",
                target = "Lcom/mojang/blaze3d/shaders/ShaderSource;get(Lnet/minecraft/resources/Identifier;Lcom/mojang/blaze3d/shaders/ShaderType;)Ljava/lang/String;"
        )
    )
    private String compileShader(ShaderSource instance, Identifier identifier, ShaderType shaderType, Operation<String> original) {
        if (!FullBright.instance.isEnabled()) return original.call(instance, identifier, shaderType);

        if (shaderType != ShaderType.FRAGMENT || !identifier.equals(RenderPipelines.LIGHTMAP.getFragmentShader()))
            return original.call(instance, identifier, shaderType);

        return """
            #version 150
            
            in vec2 texCoord;
            out vec4 fragColor;
            
            void main() {
                fragColor = vec4(1.0);
            }
            """;
    }
}