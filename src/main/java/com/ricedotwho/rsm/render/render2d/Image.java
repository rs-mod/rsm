package com.ricedotwho.rsm.render.render2d;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Image {
    private static final List<Image> images = new ArrayList<>();
    private static final Supplier<String> LABEL = () -> "RSM Dynamic Texture";
    protected String identifier;
    private final DynamicTexture texture;
    private TextureSetup textureSetup;

    public Image(String identifier) {
        this.identifier = identifier;
        try {
            String trimmed = identifier.trim();
            File file = new File(trimmed);
            InputStream stream;
            if (file.exists() && file.isFile()) {
                stream = Files.newInputStream(file.toPath());
            } else {
                stream = getClass().getResourceAsStream(trimmed);
                if (stream == null) {
                    throw new FileNotFoundException("Resource not found: " + trimmed);
                }
            }
            var image = NativeImage.read(stream);
            texture = new DynamicTexture(LABEL, image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        images.add(this);
    }

    public Image(String identifier, InputStream stream) {
        this.identifier = identifier;
        var image = NativeImage.read(stream);
        texture = new DynamicTexture(LABEL, image);
        images.add(this);
    }

    public Image(String identifier, NativeImage nativeImage) {
        this.identifier = identifier;
        texture = new DynamicTexture(LABEL, nativeImage);
        images.add(this);
    }

    public TextureSetup textureSetup() {
        if (textureSetup == null) {
            textureSetup = TextureSetup.singleTexture(
                    this.texture.getTextureView(),
                    RenderSystem.getSamplerCache()
                            .getClampToEdge(FilterMode.LINEAR)
            );
        }
        return textureSetup;
    }

    public DynamicTexture texture() {
        return texture;
    }

    public int height() {
        return texture.getPixels().getHeight();
    }

    public int width() {
        return texture.getPixels().getWidth();
    }

    public void close() {
        this.texture.close();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Image image)) return false;
        return this.identifier.equals(image.identifier);
    }

    @Override
    public int hashCode() {
        return this.identifier.hashCode();
    }

    public static void closeAll() {
        images.forEach(Image::close);
    }
}
