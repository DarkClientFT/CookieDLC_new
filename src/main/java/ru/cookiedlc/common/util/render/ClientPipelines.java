package ru.cookiedlc.common.util.render;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.Function;

public class ClientPipelines {

    public static final Function<Identifier, RenderLayer> BLOOM_ESP =
            Util.memoize(texture -> {
                RenderLayer.MultiPhaseParameters params = RenderLayer.MultiPhaseParameters.builder()
                        .program(RenderPhase.POSITION_TEXTURE_COLOR_PROGRAM)
                        .texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
                        .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                        .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                        .writeMaskState(RenderPhase.COLOR_MASK)
                        .cull(RenderPhase.DISABLE_CULLING)
                        .build(false);

                return RenderLayer.of(
                        "bloom_esp",
                        VertexFormats.POSITION_TEXTURE_COLOR,
                        VertexFormat.DrawMode.QUADS,
                        2048,
                        false,
                        true,
                        params
                );
            });
}