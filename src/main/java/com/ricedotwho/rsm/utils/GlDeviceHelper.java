package com.ricedotwho.rsm.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.opengl.DirectStateAccess;
import com.mojang.renderpearl.backend.opengl.FrameBufferAttachment;
import com.mojang.renderpearl.backend.opengl.GlTexture;

import java.util.List;

public class GlDeviceHelper {
    private static java.lang.reflect.Field backendField;
    private static java.lang.reflect.Method directStateAccessMethod;
    private static java.lang.reflect.Method frameBufferCacheMethod;
    private static java.lang.reflect.Method getFboMethod;

    public static int getFbo(GpuTexture colorTex, GlTexture depthTex) {
        try {
            var gpuDevice = RenderSystem.getDevice();

            if (backendField == null) {
                backendField = gpuDevice.getClass().getDeclaredField("backend");
                backendField.setAccessible(true);
            }
            var device = backendField.get(gpuDevice);

            if (directStateAccessMethod == null) {
                directStateAccessMethod = device.getClass().getMethod("directStateAccess");
                directStateAccessMethod.setAccessible(true);
            }
            DirectStateAccess dsa = (DirectStateAccess) directStateAccessMethod.invoke(device);

            if (frameBufferCacheMethod == null) {
                frameBufferCacheMethod = device.getClass().getMethod("frameBufferCache");
                frameBufferCacheMethod.setAccessible(true);
            }
            var frameBufferCache = frameBufferCacheMethod.invoke(device);

            if (getFboMethod == null) {
                getFboMethod = frameBufferCache.getClass().getMethod("getFbo", DirectStateAccess.class, List.class, FrameBufferAttachment.class);
                getFboMethod.setAccessible(true);
            }
            return (int) getFboMethod.invoke(frameBufferCache, dsa, List.of(colorTex), (FrameBufferAttachment) depthTex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}