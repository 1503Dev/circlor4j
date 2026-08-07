package dev1503.circlor4j.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev1503.circlor4j.client.mixin.RenderTypeAccessor;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * The {@link RenderType} used for ESP boxes. Mirrors the vanilla {@code lines} render type
 * but uses an {@code ALWAYS_PASS} depth test with depth writes disabled, so the wireframe
 * stays visible through terrain and other entities (no occlusion).
 */
public final class EspRenderType {
	public static final RenderType LINES = create();

	private EspRenderType() {
	}

	private static RenderType create() {
		RenderPipeline.Snippet globals = RenderPipeline.builder()
			.withBindGroupLayout(BindGroupLayouts.GLOBALS)
			.buildSnippet();
		RenderPipeline.Snippet matricesFog = RenderPipeline.builder(globals)
			.withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
			.withBindGroupLayout(BindGroupLayouts.FOG)
			.buildSnippet();
		RenderPipeline pipeline = RenderPipeline.builder(matricesFog)
			.withLocation("circlor4j/esp_lines")
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
			.setOutputTarget(OutputTarget.MAIN_TARGET)
			.createRenderSetup();
		return RenderTypeAccessor.circlor4jCreate("circlor4j_esp_lines", setup);
	}
}