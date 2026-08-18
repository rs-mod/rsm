package com.ricedotwho.rsm.module.impl.render;

import com.ricedotwho.rsm.module.api.Category;
import com.ricedotwho.rsm.module.api.Module;
import com.ricedotwho.rsm.module.api.ModuleInfo;
import com.ricedotwho.rsm.render.render3d.Render3DPipelines;
import lombok.Getter;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@ModuleInfo(aliases = "Breaking Texture", id = "breaking-texture", category = Category.RENDER)
public class BreakingTexture extends Module {
    private static final Function<Identifier, RenderType> CRUMBLING = Util.memoize(
            texture -> net.minecraft.client.renderer.rendertype.RenderType.create(
                    "crumbling_rsm", RenderSetup.builder(Render3DPipelines.CRUMBLING).withTexture("Sampler0", texture).sortOnUpload().createRenderSetup()
            )
    );

    @Getter
    private static final BreakingTexture instance = new BreakingTexture();


    public List<RenderType> DESTROY_TYPES;
    public BreakingTexture() {
        DESTROY_TYPES = ModelBakery.BREAKING_LOCATIONS.stream().map(BreakingTexture::apply).collect(Collectors.toList());
    }

    private static RenderType apply(Identifier identifier) {
        return CRUMBLING.apply(identifier);
    }
}
