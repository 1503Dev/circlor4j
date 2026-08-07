package dev1503.circlor4j;

import dev1503.circlor4j.client.Circlor4jClient;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric entry point. Fabric is only used as the loader entry;
 * all other business logic is hooked into the game through Mixins.
 */
public class Circlor4J implements ModInitializer {
	public static final String MOD_ID = "circlor4j";

	@Override
	public void onInitialize() {
		Circlor4jClient.init();
	}
}
