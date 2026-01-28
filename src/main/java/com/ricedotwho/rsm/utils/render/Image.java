package com.ricedotwho.rsm.utils.render;

/*
 * Original code Copyright (c) 2026, odtheking (https://github.com/odtheking/OdinFabric/blob/main/src/main/kotlin/com/odtheking/odin/utils/ui/rendering/Image.kt)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import lombok.Getter;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;

@Getter
public class Image {
    private final String identifier;
    private final boolean isSVG;
    private final InputStream inputStream;
    private ByteBuffer buffer = null;

    public Image(String identifier) {
        this.identifier = identifier;
        this.isSVG = identifier.endsWith(".svg");
        try {
            String trimmed = identifier.trim();
            File file = new File(trimmed);
            if (file.exists() && file.isFile()) {
                inputStream = Files.newInputStream(file.toPath());
            } else {
                inputStream = getClass().getResourceAsStream(trimmed);
                if (inputStream == null) {
                    throw new FileNotFoundException("Resource not found: " + trimmed);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ByteBuffer buffer() {
        if (buffer == null) {
            try (InputStream stream = inputStream) {
                byte[] bytes = stream.readAllBytes();
                buffer = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return buffer;
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
}
