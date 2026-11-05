package com.ricedotwho.rsm.render.render2d;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.ricedotwho.rsm.type.Pair;
import lombok.Getter;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.jspecify.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Objects;

import static com.ricedotwho.rsm.type.Accessor.mc;

/**
 * An image resource loaded lazily onto the GPU.
 *
 * <p>Pixel data is decoded from the backing stream/buffer on demand. A GPU texture is only created
 * the first time the image is actually rendered, so image creation can happen off the render
 * thread.</p>
 */
@Getter
public class Image {
    public static final int FLAG_NEAREST = 1;

    protected String identifier;
    private final int flags;
    private final boolean isSVG;
    private final InputStream inputStream;
    private ByteBuffer buffer;

    private NativeImage nativeImage;
    private DynamicTexture texture;
    private Pair<Integer, Integer> dims = new Pair<>(0, 0);

    public Image(String identifier) {
        this(identifier, 0);
    }

    public Image(String identifier, int flags) {
        this.identifier = identifier;
        this.flags = flags;
        this.isSVG = identifier.endsWith(".svg");
        try {
            String trimmed = identifier.trim();
            java.io.File file = new java.io.File(trimmed);
            if (file.exists() && file.isFile()) {
                inputStream = Files.newInputStream(file.toPath());
            } else {
                inputStream = getClass().getResourceAsStream(trimmed);
                if (inputStream == null) {
                    throw new java.io.FileNotFoundException("Resource not found: " + trimmed);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Image(String identifier, InputStream stream) {
        this.identifier = identifier;
        this.flags = 0;
        this.isSVG = identifier.endsWith(".svg");
        this.inputStream = stream;
    }

    public Image() {
        this.flags = 0;
        this.isSVG = false;
        this.inputStream = null;
    }

    public ByteBuffer buffer() {
        if (buffer == null) {
            try (InputStream stream = inputStream) {
                if (stream == null) throw new IOException("No input stream for image: " + identifier);
                byte[] bytes = stream.readAllBytes();
                buffer = ByteBuffer.allocateDirect(bytes.length);
                buffer.put(bytes);
                buffer.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return buffer;
    }

    public void loadDims() {
        NativeImage image = nativeImage();
        dims = new Pair<>(image.getWidth(), image.getHeight());
    }

    public void prepareTexture() {
        if (texture != null) return;
        NativeImage image = nativeImage();
        texture = new DynamicTexture(
                () -> "rsm-image/" + identifier,
                image
        );
    }

    @Nullable
    public TextureSetup textureSetup() {
        if (texture == null) {
            prepareTexture();
        }
        if (texture == null) return null;
        return TextureSetup.singleTexture(
                texture.getTextureView(),
                RenderSystem.getSamplerCache()
                        .getClampToEdge(
                                (flags & FLAG_NEAREST) != 0
                                        ? FilterMode.NEAREST
                                        : FilterMode.LINEAR
                        )
        );
    }

    public void deleteTexture() {
        if (texture != null) {
            texture.close();
            texture = null;
        }
        if (nativeImage != null) {
            nativeImage.close();
            nativeImage = null;
        }
    }

    public NativeImage nativeImage() {
        if (nativeImage == null) {
            try {
                nativeImage = NativeImage.read(buffer());
            } catch (IOException e) {
                throw new RuntimeException("Failed to load image: " + identifier, e);
            }
        }
        return nativeImage;
    }

    public void delete() {
        NVGUtils.deleteImageFully(this);
        deleteTexture();
    }

    public int width() {
        if (dims == null) return 0;
        return dims.getFirst();
    }

    public int height() {
        if (dims == null) return 0;
        return dims.getSecond();
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

    public static ByteBuffer bufferedImageToPNGByteBuffer(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return ByteBuffer.wrap(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
