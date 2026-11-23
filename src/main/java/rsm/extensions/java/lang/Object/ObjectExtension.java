package rsm.extensions.java.lang.Object;

import com.ricedotwho.rsm.utils.CollectionUtils;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

@Extension
public abstract class ObjectExtension {
    public static boolean equalsOneOf(@This Object thiz, Object ... others) {
        return CollectionUtils.equalsOneOf(thiz, others);
    }
}
