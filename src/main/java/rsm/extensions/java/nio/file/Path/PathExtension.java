package rsm.extensions.java.nio.file.Path;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.nio.file.Path;

@Extension
public abstract class PathExtension {
    public static String getFileName0(@This Path thiz, boolean extension) {
        var name = thiz.getFileName().toString();
        if (!extension) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        return name;
    }

    public static String getFileName0(@This Path thiz) {
        return getFileName0(thiz, true);
    }
}
