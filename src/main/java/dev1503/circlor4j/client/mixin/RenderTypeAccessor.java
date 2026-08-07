package dev1503.circlor4j.client.mixin;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes the package-private {@link RenderType#create(String, RenderSetup)} factory. */
@Mixin(RenderType.class)
public interface RenderTypeAccessor {

	@Invoker("create")
	static RenderType circlor4jCreate(String name, RenderSetup state) {
		throw new AssertionError("Mixin stub");
	}
}