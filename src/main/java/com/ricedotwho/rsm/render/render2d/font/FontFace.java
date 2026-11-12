package com.ricedotwho.rsm.render.render2d.font;

import com.google.gson.JsonParser;
import com.ricedotwho.rsm.render.render2d.font.msdf.MSDFInfo;
import com.ricedotwho.rsm.type.Accessor;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;
import java.util.function.IntFunction;

class FontFace implements Accessor {
    private final String location;
    private ByteBuffer buffer = null;
    private MSDFInfo info = null;

    FontFace(String path) {
        this.location = path;
    }

    public ByteBuffer buffer() {
        if (buffer == null) {
            buffer = readToByteBuffer();
        }
        return buffer;
    }

    public MSDFInfo info() {
        if (info == null) {
            info = readInfo();
        }
        return info;
    }

    private MSDFInfo readInfo() {
        var obj = JsonParser.parseReader(
                new InputStreamReader(stream("${this.location}.json"))
        ).getAsJsonObject();
        return new MSDFInfo(obj);
    }

    private ReadableByteChannel byteChannel() {
        var inputStream = stream("${this.location}.png");
        return Channels.newChannel(inputStream);
    }

    private static InputStream stream(String location) {
        return FontFace.class.getResourceAsStream(location);
    }

    private ByteBuffer readToByteBuffer() {
        try (ReadableByteChannel channel = byteChannel()) {
            return readFully(channel, BufferUtils::createByteBuffer);
        }
    }

    private static ByteBuffer readFully(ReadableByteChannel channel, IntFunction<ByteBuffer> allocator) throws IOException {
        if (channel instanceof SeekableByteChannel seekable) {
            long size = seekable.size();

            if (size > Integer.MAX_VALUE) {
                throw new IOException("Channel content too large to read into ByteBuffer");
            }

            return readFully(seekable, (int) size, allocator);
        }

        ByteBuffer buffer = requireCapacity(allocator.apply(8192), 8192);

        while (true) {
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) break;

            if (bytesRead == 0) {
                if (!buffer.hasRemaining()) {
                    buffer = grow(buffer, allocator);
                    continue;
                }
                break;
            }

            if (!buffer.hasRemaining()) {
                buffer = grow(buffer, allocator);
            }
        }

        buffer.flip();
        return buffer;
    }

    private static ByteBuffer readFully(SeekableByteChannel channel, int size, IntFunction<ByteBuffer> allocator) throws IOException {
        ByteBuffer buffer = requireCapacity(allocator.apply(size), size);
        while (buffer.hasRemaining()) {
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) break; // EOF
        }

        buffer.flip();
        return buffer;
    }

    private static ByteBuffer grow(ByteBuffer buffer, IntFunction<ByteBuffer> allocator) {
        int oldCap = buffer.capacity();
        int newCap = oldCap << 1;

        if (newCap <= 0) {
            throw new OutOfMemoryError("Buffer too large (overflow): " + oldCap);
        }

        ByteBuffer newBuffer = requireCapacity(allocator.apply(newCap), newCap);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    private static ByteBuffer requireCapacity(ByteBuffer buf, int minCap) {
        Objects.requireNonNull(buf, "Allocator returned null");

        if (buf.capacity() < minCap) {
            throw new IllegalArgumentException("Allocator returned capacity " + buf.capacity() + " < " + minCap);
        }

        return buf;
    }
}
