package com.ricedotwho.rsm.module.impl;

import com.ricedotwho.rsm.module.api.Module;


/**
 * please don't use spaces in the id
 * also ModuleInfo is commented out here to prevent Template from being registered as an actual Module,
 * do not comment out the ModuleInfo thank you :)
 */

//@ModuleInfo(aliases = "Template", id = "template", category = Category.OTHER)
public class Template extends Module {

    /**
     * the instance field is required, it can be named anything though.
     */
    @SuppressWarnings("unused")
    private static final Template instance = new Template();

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void reset() {

    }
}
