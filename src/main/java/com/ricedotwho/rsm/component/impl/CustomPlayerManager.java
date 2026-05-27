package com.ricedotwho.rsm.component.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.module.impl.render.ClickGUI;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/// [a bit modified from](https://github.com/squarcles/ofCapes/blob/architectury/common/src/main/kotlin/me/cael/capes/handler/PlayerHandler.kt)

public class CustomPlayerManager implements Accessor {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Map<UUID, PlayerData> PLAYERS = new HashMap<>();

    public static PlayerData get(GameProfile profile) {
        return PLAYERS.computeIfAbsent(profile.id(), PlayerData::new);
    }

    public static void onLoadTexture(GameProfile profile) {
        if (!ClickGUI.getCapes().getValue()) return;
        PlayerData data = get(profile);
        EXECUTOR.submit(() -> {
            for (CapeSource source : CapeSource.values()) {
                if (data.setCape(profile, source)) break;
            }
        });
    }

    @Getter
    @RequiredArgsConstructor
    public static class PlayerData {
        private final UUID id;
        private boolean hasCape;
        private boolean hasElytra;

        private boolean setCape(GameProfile profile, CapeSource source) {
            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create(source.getUrl().formatted(source == CapeSource.OPTIFINE ? profile.name() : profile.id().toString())).toURL().openConnection(mc.getProxy());
                connection.addRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setInstanceFollowRedirects(true);
                connection.setDoInput(true);
                connection.connect();
                if (connection.getResponseCode() == 200) {
                    return this.setCapeTexture(connection.getInputStream());
                }
            } catch (IOException e) {
                RSM.getLogger().error("Error while fetching cape for {}", profile.id(), e);
            }
            return false;
        }

        private boolean setCapeTexture(InputStream image) {
            try {
                NativeImage cape = NativeImage.read(image);
                mc.schedule(() -> {
                    this.hasElytra = Mth.floorDiv(cape.getWidth(), cape.getHeight()) == 2;
                    mc.getTextureManager().register(ResourceLocation.fromNamespaceAndPath("rsm", this.id.toString()), new DynamicTexture(() -> "rsm:" + this.id, parseCape(cape)));
                    this.hasCape = true;
                });
                return true;
            } catch (IOException ignored) {
                return false;
            }
        }

        private NativeImage parseCape(NativeImage img) {
            int imageWidth = 64;
            int imageHeight = 32;
            int srcWidth = img.getWidth();
            int srcHeight= img.getHeight();
            while (imageWidth < srcWidth || imageHeight < srcHeight) {
                imageWidth *= 2;
                imageHeight *= 2;
            }
            NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
            for (int x = 0; x < srcWidth; x++) {
                for (int y = 0; y < srcHeight; y++) {
                    imgNew.setPixel(x, y, img.getPixel(x, y));
                }
            }
            img.close();
            return imgNew;
        }

        public ClientAsset.ResourceTexture getCape() {
            ResourceLocation a = ResourceLocation.fromNamespaceAndPath("rsm", this.id.toString());
            return new ClientAsset.ResourceTexture(a, a);
        }
    }

    @Getter
    private enum CapeSource {
        RSM("https://player.rsamod.net/cape/%s.png"),
        OPTIFINE("http://s.optifine.net/capes/%s.png");

        private final String url;
        CapeSource(String url) {
            this.url = url;
        }
    }
}
