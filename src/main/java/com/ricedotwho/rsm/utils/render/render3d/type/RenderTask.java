package com.ricedotwho.rsm.utils.render.render3d.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RenderTask {
    protected final RenderType type;
    protected final boolean depth;
}