package dev1503.circlor4j.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev1503.circlor4j.client.mixin.RenderTypeAccessor;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class XrayRenderType {
	public static final RenderType XRAY = create();

	private XrayRenderType() {
	}

	private static RenderType create() {
		RenderPipeline.Snippet globals = RenderPipeline.builder()
			.withBindGroupLayout(BindGroupLayouts.GLOBALS)
			.buildSnippet();
		RenderPipeline.Snippet matricesFog = RenderPipeline.builder(globals)
			.withBindGroupLayout(BindGroupLayouts.PROJECTION)
			.withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
			.withBindGroupLayout(BindGroupLayouts.FOG)
			.buildSnippet();
		RenderPipeline pipeline = RenderPipeline.builder(matricesFog)
			.withLocation("circlor4j/xray")
			.withVertexShader("core/rendertype_lines")
			.withFragmentShader("core/rendertype_lines")
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withCull(false)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
			.withPrimitiveTopology(PrimitiveTopology.LINES)
			.withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
			.build();
		RenderSetup setup = RenderSetup.builder(pipeline)
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.createRenderSetup();
		return RenderTypeAccessor.circlor4jCreate("circlor4j_xray", setup);
	}
}