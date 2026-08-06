package com.ricedotwho.rsm.module.impl.render;

import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.event.impl.world.WorldEvent;
import com.ricedotwho.rsm.module.Module;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.group.GroupSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.DragSetting;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import com.ricedotwho.rsm.utils.render.render2d.GIF;
import com.ricedotwho.rsm.utils.render.render2d.Image;
import com.ricedotwho.rsm.utils.render.render2d.NVGUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Vector2d;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Use dynamic texture instead of NanoVG Image, support other hosts
@Getter
@ModuleInfo(aliases = "Image Hud", id = "ImageHud", category = Category.RENDER)
public class ImageHud extends Module {
    @Getter
    private static final ImageHud instance = new ImageHud();

    private final ButtonSetting reload = new ButtonSetting("Reload", "Reload", this::reload);
    private final Pattern DISCORD_REGEX = Pattern.compile("https://cdn.discordapp.com/attachments/\\d*/\\d*/(.*)\\.(.*)\\?.*");

    private final Set<String> ALLOWED = Set.of("png", "jpeg", "gif");
    private final File file = FileUtils.getSaveFileInCategory("render", "image_urls.json");
    private boolean started = false;
    private boolean imageLoaded = false;
    private List<String> urls = new ArrayList<>();
    @Getter
    private final Map<DragSetting, FetchedImage> images = new HashMap<>();

    public ImageHud() {
        loadUrls();
    }

    public static boolean add(String url) {
        Matcher matcher;
        if ((matcher = instance.DISCORD_REGEX.matcher(url)).find()) {
            instance.urls.add(url);
            instance.getGeneralGroup().add(new DragSetting(matcher.group(1), new Vector2d(50, 50), new Vector2d(128, 128)));
            instance.syncGeneralGroup();
            saveUrls();
            return true;
        }
        return false;
    }

    public static boolean remove(String name) {
         Optional<Map.Entry<DragSetting, FetchedImage>> opt = instance.images.entrySet().stream().filter(e -> e.getValue().name.equals(name)).findFirst();
         if (opt.isPresent()) {
             instance.images.remove(opt.get().getKey());
             instance.urls.remove(opt.get().getValue().url);
             saveUrls();
             return true;
         }
         return false;
    }

    private void loadUrls() {
        if (FileUtils.checkDir(file, List.of())) {
            try {
                List<String> temp;
                try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
                    temp = FileUtils.getGson().fromJson(reader, new TypeToken<List<String>>(){}.getType());
                    urls = temp;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        for (String url : urls) {
            Matcher matcher;
            if ((matcher = DISCORD_REGEX.matcher(url)).find()) {
                String name = matcher.group(1);
                instance.getGeneralGroup().add(new DragSetting(name, new Vector2d(50, 50), new Vector2d(128, 128)));
                instance.syncGeneralGroup();
            }
        }
    }

    private static void saveUrls() {
        FileUtils.writeJson(instance.urls, instance.file);
    }

    @SubscribeEvent
    private void onWorldLoad(WorldEvent.Load event) {
        if (!started) {
            started = true;
            reload();
        }
    }

    private void removeDragSetting(DragSetting ds) {
        for (GroupSetting<?> gs : this.getGroupSettings()) {
            gs.getValue().getSettings().removeIf(ds::equals);
        }
    }

    public void reload() {
        imageLoaded = false;
        this.saveConfig();
        for (Map.Entry<DragSetting, FetchedImage> e : images.entrySet()) {
            if (e.getValue() instanceof Animated a) a.image.delete();
            else if (e.getValue() instanceof Static a) a.image.delete();
            removeDragSetting(e.getKey());
        }
        images.clear();

        List<LoadingData> data = new ArrayList<>();

        for (String url : urls) {
            boolean isGif;
            if (url.isBlank()) {
                ChatUtils.chat("Link is blank!");
                return;
            }
            String name;
            Matcher matcher;
            if ((matcher = DISCORD_REGEX.matcher(url)).find()) {
                name = matcher.group(1);
                String extension = matcher.group(2);
                if (!ALLOWED.contains(extension)) {
                    ChatUtils.chat("Unsupported file type! (%s)", extension);
                    return;
                }
                isGif = matcher.group(2).contains("gif");
            } else {
                ChatUtils.chat("Invalid link (%s)", url);
                return;
            }
            data.add(new LoadingData(url, isGif, name));
        }

        new Thread(() -> {
            try {
                for (LoadingData d : data) {
                    fetchImage(d.url, d.gif, d.name);
                    Thread.sleep(250);
                }
                Thread.sleep(500);
            } catch (IOException e) {
                ChatUtils.chat("Exception while loading image!, %s", e.getMessage());
                RSM.getLogger().error("Exception while loading image!", e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.loadConfig();
            this.imageLoaded = true;
        }).start();
    }

    public void fetchImage(String url, boolean gif, String name) throws IOException {
        URL imgUrl = URI.create(url).toURL();
        HttpURLConnection connection = (HttpURLConnection) imgUrl.openConnection();
        connection.setRequestProperty("User-Agent", "RSM Image Hud");
        connection.connect();
        BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
        String contentType = connection.getContentType();

        Animated temp;
        boolean set;
        if (contentType.equals("image/gif") && gif) {
            temp = new Animated(name, url, new GIF(name, input));
            set = true;
        } else {
            temp = null;
            set = false;
        }

        Optional<DragSetting> opt = this.getDragSettings().stream().filter(ds -> ds.getName().equals(name)).findFirst();
        DragSetting dragSetting = opt.orElse(new DragSetting(name, new Vector2d(50, 50), new Vector2d(128, 128)));

        instance.getGeneralGroup().add(dragSetting);
        instance.syncGeneralGroup();

        mc.execute(() -> {
            if (set) {
                temp.image.create();
                images.put(dragSetting, temp);
            } else {
                images.put(dragSetting, new Static(name, url, NVGUtils.createImage(name, input)));
            }
        });
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        if (images.isEmpty() || !imageLoaded) return;

        // cursed af
        for (Map.Entry<DragSetting, FetchedImage> e : images.entrySet()) {
            Image im;
            if (e.getValue() instanceof Animated a) {
                im = a.image.getCurrent();
            } else if (e.getValue() instanceof Static s) {
                im = s.image;
            } else {
                im = null;
            }
            if (im == null) continue;

            e.getKey().renderScaled(event.getGfx(), () -> {
                if (NVGUtils.hasImage(im)) NVGUtils.renderImage(im, 0, 0, im.getDims().getFirst(), im.getDims().getSecond());
            }, im.getDims().getFirst(), im.getDims().getSecond());
        }
    }

    @AllArgsConstructor
    public static class FetchedImage {
        public final String name;
        public final String url;
    }

    private static class Animated extends FetchedImage {
        public final GIF image;
        public Animated(String name, String url, GIF image) {
            super(name, url);
            this.image = image;
        }
    }

    private static class Static extends FetchedImage {
        public final Image image;
        public Static(String name, String url, Image image) {
            super(name, url);
            this.image = image;
        }
    }

    private record LoadingData(String url, boolean gif, String name) { }
}
