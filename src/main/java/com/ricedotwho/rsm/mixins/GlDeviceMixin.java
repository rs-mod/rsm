package com.ricedotwho.rsm.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.frontend.shaders.PipelineBuilder;
import com.ricedotwho.rsm.module.impl.render.FullBright;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PipelineBuilder.class)
public class GlDeviceMixin {

    /**
     * @author DocilElm
     * <a href="https://github.com/Synnerz/devonian/blob/1.21.11/src/main/java/com/github/synnerz/devonian/mixin/GlDeviceMixin.java">From Devonian</a>
     */

    @WrapOperation(
        method = "loadShaderSource",
        at = @At(
                value = "INVOKE",
                target = "Lcom/mojang/renderpearl/api/pipeline/ShaderSource;getShader(Lnet/minecraft/resources/Identifier;Lcom/mojang/renderpearl/api/pipeline/ShaderType;)Ljava/lang/String;"
        )
    )
    private static String compileShader(ShaderSource instance, Identifier identifier, ShaderType shaderType, Operation<String> original) {
        if (!FullBright.instance.isEnabled()) return original.call(instance, identifier, shaderType);

        if (shaderType != ShaderType.FRAGMENT || !identifier.equals(RenderPipelines.LIGHTMAP.getShaders().get(ShaderType.FRAGMENT)))
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