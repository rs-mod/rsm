package com.ricedotwho.rsm.addon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.Person;

import java.util.Collection;

@Getter
@AllArgsConstructor
public class AddonMeta {
    private String id;
    private String name;
    String main;
    private Version version;
    private Collection<Person> authors;

    public String getId() {
        return this.id.toLowerCase();
    }
}
