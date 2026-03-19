package com.ricedotwho.rsm.ui.clickgui.settings.impl;

import com.google.gson.*;
import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.ui.clickgui.settings.Setting;
import com.ricedotwho.rsm.utils.Accessor;
import com.ricedotwho.rsm.utils.FileUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Getter
@Setter
public class SaveSetting<T> extends Setting<T> implements Accessor {
    private final Gson gson;
    private final String path;
    private final String defaultFile;
    private String fileName;
    private final String ext;
    private File file;
    private final Type type;
    private final Supplier<T> factory;
    private final boolean allowEdits;
    private final Runnable action;

    public SaveSetting(String name, String path, String defaultFile, Supplier<T> factory, Type type) {
        this(name, path, defaultFile, factory, type, true, false, null);
    }

    public SaveSetting(String name, String path, String defaultFile, Supplier<T> factory, Type type, Runnable action) {
        this(name, path, defaultFile, factory, type, true, false, action);
    }

    public SaveSetting(String name, String path, String defaultFile, Supplier<T> factory, Type type, boolean allowEdits, Runnable action) {
        this(name, path, defaultFile, factory, type, true, allowEdits, action);
    }

    public SaveSetting(String name, String path, String defaultFile, Supplier<T> factory, Type type, boolean pretty, boolean allowEdits, Runnable action) {
        this(name, path, defaultFile, factory, type, pretty, allowEdits, action, null);
    }

    public SaveSetting(String name, String path, String defaultFile, Supplier<T> factory, Type type, boolean pretty, boolean allowEdits, Runnable action, BooleanSupplier supplier) {
        this(name, path, defaultFile, factory, type, pretty ? FileUtils.getPgson() : FileUtils.getGson(), allowEdits, action, supplier);
    }

    public SaveSetting(String name, String path, String defaultFile, Supplier<T> factory, Type type, Gson gson, boolean allowEdits, Runnable action, BooleanSupplier supplier) {
        super(name, supplier);
        this.path = path;
        String[] f = defaultFile.split("\\.");
        this.defaultFile = f[0];
        this.fileName = this.defaultFile;
        this.ext = f[1];
        this.file = FileUtils.getSaveFileInCategory(path, defaultFile);
        this.gson = gson;
        this.type = type;
        this.factory = factory;
        this.allowEdits = allowEdits;
        this.value = factory.get();
        this.action = action;
    }

    public void updateFile() {
        file = FileUtils.getSaveFileInCategory(path, fileName + "." + ext);
    }

    @Override
    public void loadFromJson(JsonObject obj) {
        this.fileName = obj.get("file").getAsString();
        updateFile();
        load();
    }

    @Override
    public void saveToJson(JsonObject obj) {
        obj.addProperty("name", this.getName());
        obj.addProperty("type", this.getType());
        obj.addProperty("file", this.fileName);
        save();
    }

    public void save() {
        try {
            Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
            gson.toJson(getValue(), writer);
            writer.close();
        } catch (IOException e) {
            RSM.getLogger().error("Error saving SaveSetting {}", this.getName(), e);
        }
    }

    public void load() {
        T instance = factory.get();
        if (FileUtils.checkDir(file, instance)) {
            try {
                T temp;
                try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8)) {
                    temp = gson.fromJson(reader, type);
                    setValue(temp);
                }
            } catch (IOException | JsonSyntaxException | JsonIOException e) {
                RSM.getLogger().error("Error while loading SaveSetting {}", this.getName(), e);
            }
        } else {
            setValue(factory.get());
        }
        if (action != null) action.run();
    }

    @Override
    public String getType() {
        return "save";
    }
}
