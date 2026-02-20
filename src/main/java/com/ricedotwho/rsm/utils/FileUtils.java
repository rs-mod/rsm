package com.ricedotwho.rsm.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ricedotwho.rsm.RSM;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@UtilityClass
public class FileUtils {
    private final  Gson gson = new Gson();
    private final  Gson pgson = new GsonBuilder().setPrettyPrinting().create();
    public final File file_path = new File("config");

    public File getSaveFileInCategory(String category, String fileName){
        File mainPath = new File(file_path, "rsm");
        mainPath.mkdir();

        File rsPath = new File(mainPath, category);
        rsPath.mkdir();

        return new File(rsPath, fileName);

    }

    public File getCategoryFolder(String category){
        File mainPath = new File(file_path, "rsm");
        mainPath.mkdir();

        File rsPath = new File(mainPath, category);
        rsPath.mkdir();

        return rsPath;
    }

    public void writeJson(Object obj, File file) {
        writeJson(obj, file, true);
    }

    public void writeJson(Object obj, File file, boolean pretty) {
        try {
            Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);

            if (pretty) {
                pgson.toJson(obj, writer);
            } else {
                gson.toJson(obj, writer);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toJson(Object object, boolean pretty) {
        if(pretty) {
            return pgson.toJson(object);
        } else {
            return gson.toJson(object);
        }
    }

    public static boolean checkDir(File file, Object def) {
        try {
            if(file.exists()) return true;
            File parentDir = file.getParentFile();
            if(parentDir != null && !parentDir.exists()) {
                if(!parentDir.mkdirs()) {
                    RSM.getLogger().error("Failed to create file: {}", parentDir.getName());
                    return false;
                }
            }
            try {
                if (file.createNewFile()) {
                    FileUtils.writeJson(def, file);
                    return false;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
