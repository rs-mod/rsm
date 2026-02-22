package com.ricedotwho.rsm.addon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.FileUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.util.version.VersionParser;
import net.minecraft.ChatFormatting;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AddonLoader {
    private final Set<AddonContainer> addons = new HashSet<>();
    private final Set<AddonContainer> mixinAddons = new HashSet<>();
    private final Set<String> takenIds = new HashSet<>();

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Version.class, (JsonDeserializer<Version>) (json, typeOfT, context) ->
                    {
                        try {
                            return VersionParser.parse(json.getAsString(), false);
                        } catch (VersionParsingException e) {
                            throw new RuntimeException(e);
                        }
                    }
            )
            .create();

    public Set<AddonContainer> getAddons() {
        return Collections.unmodifiableSet(addons);
    }

    public AddonContainer getAddonById(String name) {
        for (AddonContainer addon : addons) {
            if (Objects.equals(addon.getMeta().getId(), name)) return addon;
        }
        return null;
    }

    public void loadMixinUser() {
        for (EntrypointContainer<Addon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("rsm", Addon.class)) {
            loadMixinAddon(entrypoint);
        }
    }

    public void load(boolean reload) {
        if (!addons.isEmpty()) unload();

        File addonDir = FileUtils.getCategoryFolder("addons");

        File[] jars = addonDir.listFiles(file ->
                file.isFile() && file.getName().endsWith(".jar")
        );
        if (jars == null) return;

        for (File jar : jars) {
            loadFile(jar, reload);
        }
    }

    public void load(String name, boolean reload) {
        AddonContainer existing = getAddonById(name);
        if (existing != null) {
            unload(existing);
        }

        File addonDir = FileUtils.getCategoryFolder("addons");

        File[] jars = addonDir.listFiles(file ->
                file.isFile() && file.getName().startsWith(name) && file.getName().endsWith(".jar")
        );
        if (jars == null || jars.length == 0) {
            ChatUtils.chat("No addon found with the name: %s", name);
            return;
        }

        loadFile(jars[0], reload);
    }

    private void loadFile(File jar, boolean reload) {
        AddonMeta meta;
        try {
            meta = readAddonJson(jar);
            if (meta == null) return;

            if (takenIds.contains(meta.getId())) {
                RSM.getLogger().error("An addon with the ID {} already exists! skipping...", meta.getId());
                ChatUtils.chat(ChatFormatting.RED + "An addon with the ID %s already exists! skipping...", meta.getId());
                return;
            }

            AddonClassLoader cl = new AddonClassLoader(jar.toURI().toURL(), AddonLoader.class.getClassLoader());
            Class<?> main = cl.loadClass(meta.getMain());

            if (!Addon.class.isAssignableFrom(main)) {
                RSM.getLogger().error("{} of {} does not extend Addon", meta.getMain(), jar.getName());
                ChatUtils.chat(ChatFormatting.RED + "%s of %s does not extend Addon", meta.getMain(), jar.getName());
                return;
            }

            Addon addon = (Addon) main.getDeclaredConstructor().newInstance();
            AddonContainer container = new AddonContainer(addon, cl, meta, false);
            addons.add(container);
            takenIds.add(meta.getId());
            container.load(reload);
            ChatUtils.chat("%s loaded", meta.getName());
        } catch (IOException e) {
            RSM.getLogger().error("Addon {} caused IOException! {}", jar.getName(), e);
        } catch (InstantiationException | IllegalAccessException e) {
            RSM.getLogger().error("Failed to load addon {}", jar.getName(), e);
            ChatUtils.chat(ChatFormatting.RED + "Failed to load addon %s", jar.getName());
        } catch (ClassNotFoundException e) {
            RSM.getLogger().error("Addon {}'s main class was not found!", jar.getName(), e);
            ChatUtils.chat(ChatFormatting.RED + "Addon %s's main class was not found!", jar.getName());
        } catch (InvocationTargetException | NoSuchMethodException e) {
            RSM.getLogger().error("Failed to create a new instance of {}", jar.getName(), e);
            ChatUtils.chat(ChatFormatting.RED + "Failed to create a new instance of %s", jar.getName());
        }
    }

    private void loadMixinAddon(EntrypointContainer<Addon> entrypoint) {
        ModMetadata metadata = entrypoint.getProvider().getMetadata();
        Addon addon;
        try {
            addon = entrypoint.getEntrypoint();
        } catch (Throwable throwable) {
            throw new RuntimeException("Exception during addon init \"%s\".".formatted(metadata.getName()), throwable);
        }

        AddonMeta meta = new AddonMeta(metadata.getId(), null, metadata.getName(), metadata.getVersion(), metadata.getAuthors());

        // Fabric loaded = cannot be unloaded idt
        AddonContainer container = new AddonContainer(
                addon,
                null,
                meta,
                true
        );

        mixinAddons.add(container);
        takenIds.add(meta.getId());
        container.load(false);
    }


    public void unload() {
        for (AddonContainer addon : addons) {
            addon.unLoad();
        }
        addons.clear();
        takenIds.clear();
    }

    public void unload(AddonContainer addon) {
        addon.unLoad();
        addons.remove(addon);
        takenIds.remove(addon.getMeta().getId());
    }

    public void unload(String id) {
        AddonContainer addon = getAddonById(id);
        if (addon == null) {
            ChatUtils.chat("No addon found with the id %s", id);
            return;
        }
        addon.unLoad();
        addons.remove(addon);
        takenIds.remove(addon.getMeta().getId());
    }

    public void reload() {
        unload();
        load(true);
    }

    public void reload(String id) {
        unload(id);
        load(id, true);
    }

    public AddonMeta readAddonJson(File jarFile) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry entry = jar.getJarEntry("addon.json");

            if (entry == null) {
                RSM.getLogger().error("Missing addons.json in {}", jarFile.getName());
                ChatUtils.chat(ChatFormatting.RED + "Missing addons.json in %s", jarFile.getName());
                return null;
            }

            try (InputStream in = jar.getInputStream(entry);
                 Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {

                return gson.fromJson(reader, AddonMeta.class);
            }
        }
    }

    public static <T> List<T> instantiate(List<Class<? extends T>> types) {
        List<T> ret = new ArrayList<>();
        for (Class<? extends T> type : types) {
            try {
                ret.add(type.getDeclaredConstructor().newInstance());
            } catch (ReflectiveOperationException e) {
                RSM.getLogger().error("Failed to instantiate {}", type.getSimpleName(), e);
                ChatUtils.chat(ChatFormatting.RED + "Failed to instantiate %s", type.getSimpleName());
            }
        }
        return ret;
    }
}
