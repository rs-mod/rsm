package com.ricedotwho.rsm.utils.render.render2d;

import lombok.Getter;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

@Getter
public class Font {
    private final String name;
    private final byte[] bytes;
    private ByteBuffer buffer = null;

    public Font(String name, InputStream inputStream) {
        this.name = name;

        try (InputStream stream = inputStream) {
            bytes = stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteBuffer buffer() {
        if (bytes == null) {
            throw new IllegalStateException("Font bytes not cached for font: " + this.name);
        }
        if (buffer == null) {
            buffer = BufferUtils.createByteBuffer(bytes.length);
            buffer.put(bytes);
            buffer.flip();
        }
        return buffer;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Font font && font.getName().equals(this.name);
    }
}
