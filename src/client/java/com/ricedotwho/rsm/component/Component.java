package com.ricedotwho.rsm.component;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;

@Getter
public class Component implements Accessor {
    private boolean enabled = false;
    private final String name;

    public Component(String name){
        this.name = name;
    }

    public void setEnabled(boolean enabled){
        if (this.enabled != enabled){
            if (enabled) {
                onEnable();
                RSM.getInstance().getEventBus().register(this);
            } else {
                RSM.getInstance().getEventBus().unregister(this);
                onDisable();
            }
            this.enabled = enabled;
        }
    }

    public void onEnable(){

    }

    public void onDisable(){

    }
}
