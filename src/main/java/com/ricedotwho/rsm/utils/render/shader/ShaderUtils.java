package com.ricedotwho.rsm.utils.render.shader;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.render.RenderUtils;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;

public class ShaderUtils implements Accessor {

    private static final HashMap<Integer, FloatBuffer> kernelCache = new HashMap<>();

    public static FloatBuffer getKernel(int radius) {
        FloatBuffer buffer = kernelCache.get(radius);
        if (buffer == null) {
            buffer = BufferUtils.createFloatBuffer(radius);
            float[] kernel = new float[radius];
            float sigma = radius / 2.0F;
            float total = 0.0F;
            for (int i = 0; i < radius; i++) {
                float multiplier = i / sigma;
                kernel[i] = 1.0F / (Math.abs(sigma) * 2.50662827463F) * (float) Math.exp(-0.5 * multiplier * multiplier);
                total += i > 0 ? kernel[i] * 2 : kernel[0];
            }
            for (int i = 0; i < radius; i++) {
                kernel[i] /= total;
            }
            buffer.put(kernel);
            buffer.flip();
            kernelCache.put(radius, buffer);
        }
        return buffer;
    }

    public static int loadTexture(BufferedImage image) {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        ByteBuffer buffer = BufferUtils.createByteBuffer(pixels.length * 4);

        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();

        int textureID = GL11.glGenTextures();
        RenderUtils.bindTexture(textureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        RenderUtils.bindTexture(0);
        return textureID;
    }

//    public static int getTextureId(ResourceLocation identifier) {
//        AbstractTexture abstractTexture = mc.getTextureManager().getTexture(identifier);
//        return ((NativeImageBackedTexture) abstractTexture).getGlId();
//    }

    public static void initStencilReplace() {

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glColorMask(false, false, false, false);
    }

    public static void uninitStencilReplace() {
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);
    }

}
	