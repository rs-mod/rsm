package rsm.extensions.java.lang.Object;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

import java.util.Objects;

@Extension
public abstract class ObjectExtension {
    public static boolean equalsOneOf(@This Object thiz, Object ... others) {
        for (Object obj : others) {
            if (Objects.equals(thiz, obj)) {
                return true;
            }
        }
        return false;
    }
}
