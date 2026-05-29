package com.ricedotwho.rsm.utils;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;

public class GlDeviceHelper {
    private static java.lang.reflect.Field backendField;
    private static java.lang.reflect.Method directStateAccessMethod;
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
                directStateAccessMethod = device.getClass().getDeclaredMethod("directStateAccess");
                directStateAccessMethod.setAccessible(true);
            }
            DirectStateAccess dsa = (DirectStateAccess) directStateAccessMethod.invoke(device);
            return depthTex.getFbo(dsa, colorTex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}