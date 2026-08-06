package com.ricedotwho.rsm.render.render2d;

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
