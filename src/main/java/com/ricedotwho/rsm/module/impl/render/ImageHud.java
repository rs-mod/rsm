package com.ricedotwho.rsm.module.impl.render;

import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.RSM;
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
import com.ricedotwho.rsm.utils.ConfigUtils;
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

@Getter
@ModuleInfo(aliases = "Image Hud", id = "ImageHud", category = Category.RENDER)
public class ImageHud extends Module {
    private final ButtonSetting reload = new ButtonSetting("Reload", "Reload", this::reload);
    private static final Pattern DISCORD_REGEX = Pattern.compile("https://cdn.discordapp.com/attachments/\\d*/\\d*/(.*)\\.(.*)\\?.*");

    private static final Set<String> ALLOWED = Set.of("png", "jpeg", "gif");
    private static final File file = FileUtils.getSaveFileInCategory("render", "image_urls.json");
    private boolean started = false;
    private boolean imageLoaded = false;
    private static List<String> urls = new ArrayList<>();
    @Getter
    private static final Map<DragSetting, FetchedImage> images = new HashMap<>();

    public ImageHud() {
        this.registerProperty(reload);
        loadUrls();
    }

    public static boolean add(String url) {
        Matcher matcher;
        if ((matcher = DISCORD_REGEX.matcher(url)).find()) {
            urls.add(url);
            RSM.getModule(ImageHud.class).registerProperty(new DragSetting(matcher.group(1), new Vector2d(50, 50), new Vector2d(128, 128)));
            saveUrls();
            return true;
        }
        return false;
    }

    public static boolean remove(String name) {
         Optional<Map.Entry<DragSetting, FetchedImage>> opt = images.entrySet().stream().filter(e -> e.getValue().name.equals(name)).findFirst();
         if (opt.isPresent()) {
             images.remove(opt.get().getKey());
             urls.remove(opt.get().getValue().url);
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
                this.registerProperty(new DragSetting(name, new Vector2d(50, 50), new Vector2d(128, 128)));
            }
        }
    }

    private static void saveUrls() {
        FileUtils.writeJson(urls, file);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!started) {
            started = true;
            reload();
        }
    }

    private void removeDragSetting(DragSetting ds) {
        for (GroupSetting<?> gs : this.getSettings()) {
            gs.getValue().getSettings().removeIf(ds::equals);
        }
    }

    public void reload() {
        imageLoaded = false;
        ConfigUtils.saveConfig(this);
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
            ConfigUtils.loadConfig(this);
            this.imageLoaded = true;
        }).start();
    }

    public void fetchImage(String url, boolean gif, String name) throws IOException {
        URL imgUrl = URI.create(url).toURL();
        HttpURLConnection connection = (HttpURLConnection) imgUrl.openConnection();
        connection.setRequestProperty("User-Agent", "RSM Image Hud");
        connection.connect();
        BufferedInputStream input = new BufferedInputStream(connection.getInputStream());

        Animated temp;
        boolean set;
        if (gif) {
            temp = new Animated(name, url, new GIF(name, input));
            set = true;
        } else {
            temp = null;
            set = false;
        }

        Optional<DragSetting> opt = this.getDragSettings().stream().filter(ds -> ds.getName().equals(name)).findFirst();
        DragSetting dragSetting = opt.orElse(new DragSetting(name, new Vector2d(50, 50), new Vector2d(128, 128)));

        this.registerProperty(dragSetting);

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
    public void onRender2D(Render2DEvent event) {
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
