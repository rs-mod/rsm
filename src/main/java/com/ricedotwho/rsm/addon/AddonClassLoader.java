package com.ricedotwho.rsm.addon;

import java.net.URL;
import java.net.URLClassLoader;

public class AddonClassLoader extends URLClassLoader {
    public AddonClassLoader(URL jar, ClassLoader parent) {
        super(new URL[]{jar}, parent);
    }
}
