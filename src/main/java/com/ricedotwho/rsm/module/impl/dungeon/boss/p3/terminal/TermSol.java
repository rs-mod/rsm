package com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class TermSol {
    private Integer slot;
    private Integer clicks;

    public TermSol(int slot) {
        this.slot = slot;
        this.clicks = 0;
    }

    public TermSol copy() {
        return new TermSol(this.slot, this.clicks);
    }

    @Override
    public String toString() {
        return "TermSol{slot=" + slot
                + ",clicks=" + clicks + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TermSol other)) return false;
        return Objects.equals(this.slot, other.slot) && Objects.equals(this.clicks, other.clicks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, clicks);
    }
}
