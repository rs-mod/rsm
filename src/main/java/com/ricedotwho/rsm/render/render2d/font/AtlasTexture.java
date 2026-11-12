package com.ricedotwho.rsm.render.render2d.font;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class AtlasTexture extends AbstractTexture {
    private static final GpuFormat format = GpuFormat.RGBA8_UNORM;
    private static final FilterMode min = FilterMode.LINEAR;
    private static final FilterMode mag = FilterMode.LINEAR;

    public AtlasTexture(NativeImage image) {
        texture = RenderSystem.getDevice().createTexture("", 15, format, image.getWidth(), image.getHeight(), 1, 1);
        sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT, min, mag, false);
        textureView = RenderSystem.getDevice().createTextureView(texture);
        RenderSystem.getDevice().createCommandEncoder().writeToTexture(texture, image);
        image.close();
    }

    public int width() {
        return texture == null ? 0 : texture.getWidth(0);
    }

    public int height() {
        return texture == null ? 0 : texture.getHeight(0);
    }
}
