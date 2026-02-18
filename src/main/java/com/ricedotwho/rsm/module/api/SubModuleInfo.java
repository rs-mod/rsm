package com.ricedotwho.rsm.module.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SubModuleInfo {
    String name();
    int defaultKey() default -1;
    boolean alwaysDisabled() default true;
    boolean hasKeybind() default false;
    boolean isEnabled() default true;
    boolean isAllowGui() default false;
}
