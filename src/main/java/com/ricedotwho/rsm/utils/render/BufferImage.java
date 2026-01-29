package com.ricedotwho.rsm.utils.render;

import java.nio.ByteBuffer;

public class BufferImage extends Image {
    private final ByteBuffer buffer;

    public BufferImage(String identifier, ByteBuffer buffer) {
        this.identifier = identifier;
        this.buffer = buffer;
    }

    @Override
    public ByteBuffer buffer() {
        return this.buffer;
    }
}
