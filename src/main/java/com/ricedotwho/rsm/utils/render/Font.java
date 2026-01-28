package com.ricedotwho.rsm.utils.render;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Font {
    @Getter
    private final String name;
    private final byte[] cachedBytes;

    public Font(String name, InputStream inputStream) {
        this.name = name;

        try (InputStream stream = inputStream) {
            cachedBytes = stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteBuffer buffer() {
        return ByteBuffer.allocateDirect(cachedBytes.length)
                .order(ByteOrder.nativeOrder())
                .put(cachedBytes)
                .flip();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Font font)) return false;
        return this.name.equals(font.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
