package com.ricedotwho.rsm.module.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
    String[] aliases() default {};
    String id() default "";
    Category category() default Category.OTHER;
    int defaultKey() default 0;
    boolean alwaysDisabled() default false;
    boolean hasKeybind() default false;
    boolean isEnabled() default false;
    boolean isAllowGui() default false;
}
