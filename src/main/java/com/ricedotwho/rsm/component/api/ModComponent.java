package com.ricedotwho.rsm.component.api;

import com.ricedotwho.rsm.RSM;
import com.ricedotwho.rsm.utils.Accessor;
import lombok.Getter;

@Getter
public class ModComponent implements Accessor {
    private final String name;

    public ModComponent(String name){
        this.name = name;
    }

    public void register(){
        RSM.getInstance().getEventBus().register(this);
    }

    public void unregister(){
        RSM.getInstance().getEventBus().unregister(this);
    }

}
