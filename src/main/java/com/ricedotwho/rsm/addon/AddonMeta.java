package com.ricedotwho.rsm.addon;

import lombok.Getter;

@Getter
public class AddonMeta {
    private String id;
    private String main;
    private String version;

    public String getId() {
        return this.id.toLowerCase();
    }
}
