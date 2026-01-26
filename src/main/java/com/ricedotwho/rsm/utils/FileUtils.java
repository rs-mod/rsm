package com.ricedotwho.rsm.utils;

import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
public class FileUtils {

    public File file_path = new File("config");

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
}
