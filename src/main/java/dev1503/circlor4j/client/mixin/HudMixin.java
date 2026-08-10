package dev1503.circlor4j.client.mixin;

import dev1503.circlor4j.client.render.ItemTagRenderer;
import dev1503.circlor4j.client.render.TracerRenderer;
import dev1503.circlor4j.ui.horionarraylist.HorionArrayList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws the Tracer lines and the Arraylist on top of the HUD once the world has rendered. */
@Mixin(Hud.class)
public abstract class HudMixin {

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void circlor4jOverlays(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		TracerRenderer.render(graphics);
		ItemTagRenderer.render(graphics);
		HorionArrayList.render(graphics);
	}
}