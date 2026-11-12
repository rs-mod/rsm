package com.ricedotwho.rsm.module.impl.render;

import com.google.gson.reflect.TypeToken;
import com.ricedotwho.rsm.core.GraphicsInit;
import com.ricedotwho.rsm.core.RSM;
import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.module.api.settings.impl.ButtonSetting;
import com.ricedotwho.rsm.module.api.settings.impl.HudSetting;
import com.ricedotwho.rsm.render.render2d.GIF;
import com.ricedotwho.rsm.render.render2d.Image;
import com.ricedotwho.rsm.render.render2d.Render2D;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Getter
@ModuleInfo(aliases = "Image Hud", id = "ImageHud", category = Category.RENDER)
public class ImageHud extends Module {
    @Getter
    private static final ImageHud instance = new ImageHud();

    private final ButtonSetting reload = new ButtonSetting("Reload", "Reload", this::reload);
    private static final Pattern DISCORD_REGEX = Pattern.compile("https://cdn.discordapp.com/attachments/\\d*/\\d*/(.*)\\.(.*)\\?.*");

    private final Set<String> ALLOWED = Set.of("png", "jpeg", "gif");
    private final File file = FileUtils.getSaveFileInCategory("render", "image_urls.json");
    private final Path folder = FileUtils.getCategoryFolder("render/imagehud").toPath();
    private List<String> urls = new ArrayList<>();
    @Getter
    private final Map<HudSetting, FetchedImage> images = new HashMap<>();

    public static boolean add(String url) {
        Matcher matcher;
        if ((matcher = DISCORD_REGEX.matcher(url)).find()) {
            instance.urls.add(url);
            instance.addSetting(new ImageHudSetting(matcher.group(1)));
            instance.syncGeneralGroup();
            saveUrls();
            return true;
        }
        return false;
    }

    public static boolean remove(String name) {
         Optional<Map.Entry<HudSetting, FetchedImage>> opt = instance.images.entrySet().stream().filter(e -> e.getValue().name.equals(name)).findFirst();
         if (opt.isPresent()) {
             if (Objects.equals(opt.get().getValue().url, "LOCAL")) {
                 ChatUtils.chat("Cannot remove local image! Delete the image and reload instead.");
                 return false;
             }

             instance.removeSetting(opt.get().getKey());
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
    }

    private static void saveUrls() {
        FileUtils.writeJson(instance.urls, instance.file);
    }

    @GraphicsInit
    public void init() {
        loadUrls();
        reload(false);
    }

    // how does module manager know? I don't
    private void addSetting(HudSetting setting) {
        mc.execute(() -> getGeneralGroup().add(setting));
    }

    private void removeSetting(HudSetting setting) {
        this.getGeneralGroup().getValue().getSettings().removeIf(it -> Objects.equals(it.getName(), setting.getName()));
        RSM.getInstance().getModuleManager().onRemove(setting);
    }

    public void reload(boolean save = true) {
        if (instance == null) return;
        if (save) this.saveConfig();
        for (Map.Entry<HudSetting, FetchedImage> e : images.entrySet()) {
            if (e.getValue() instanceof Animated a) a.image.close();
            else if (e.getValue() instanceof Static a) a.image.close();
        }
        images.clear();

        this.getGeneralGroup().getValue().getSettings().filter(HudSetting.class::isInstance).map(HudSetting.class::cast).toList().forEach(this::removeSetting);
        this.getGeneralGroup().getValue().getHudSettings().clear();
        this.getHudSettings().clear();

        // load files from config
        try (Stream<Path> files = Files.list(folder)) {
            var valid = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(ALLOWED))
                    .toList();

            for (var path : valid) {
                boolean gif = path.toString().endsWith(".gif");
                var name = path.getFileName0(false);

                HudSetting setting = new ImageHudSetting(name);
                try (InputStream stream = Files.newInputStream(path)) {
                    images.put(setting, gif ? new Animated(name, "LOCAL", new GIF(name, stream)) : new Static(name, "LOCAL", new Image(name, stream)));
                    addSetting(setting);
                }
            }
        }

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
                    ChatUtils.chat("Unsupported file type! ({})", extension);
                    return;
                }
                isGif = matcher.group(2).contains("gif");
            } else {
                ChatUtils.chat("Invalid link ({})", url);
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
                this.syncGeneralGroup();
                Thread.sleep(500);
            } catch (IOException e) {
                ChatUtils.chat("Exception while loading image!, {}", e.getMessage());
                RSM.getLogger().error("Exception while loading image!", e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mc.execute(this::loadConfig);
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

        HudSetting dragSetting = new ImageHudSetting(name);
        instance.addSetting(dragSetting);

        mc.execute(() -> {
            if (set) {
                temp.image.create();
                images.put(dragSetting, temp);
            } else {
                images.put(dragSetting, new Static(name, url, new Image(name, input)));
            }
        });
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

    private static class ImageHudSetting extends HudSetting {
        public ImageHudSetting(String name) {
            super(name, new Vector2f(50, 50), 1f);
        }

        @Override
        protected @NotNull Vector2f render(GuiGraphicsExtractor gfx, boolean example) {
            Image im;
            var entry = ImageHud.getInstance().images.get(this);
            if (entry instanceof Animated a) {
                im = a.image.getCurrent();
            } else if (entry instanceof Static s) {
                im = s.image;
            } else {
                return new Vector2f();
            }

            Render2D.image(gfx, 0f, 0f, im);
            return dims(im.width(), im.height());
        }
    }
}
