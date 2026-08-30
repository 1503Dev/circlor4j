package dev1503.circlor4j;

import dev1503.circlor4j.client.Circlor4jClient;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.lang.instrument.Instrumentation;

public class Circlor4JavaAgent {
	private static volatile boolean initialized = false;

	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("[Circlor4J] Version: " + ModStatic.VERSION);
		System.out.println("[Circlor4J] Waiting for Fabric loader...");

		Thread waitThread = new Thread(() -> {
			int maxAttempts = 120;
			int attempts = 0;

			while (!initialized && attempts < maxAttempts) {
				try {
					Thread.sleep(1000);
					attempts++;
					try {
						Class<?> fabricLoader = Class.forName("net.fabricmc.loader.api.FabricLoader");
						Object instance = fabricLoader.getMethod("getInstance").invoke(null);

						if (instance != null) {
							System.out.println("[Circlor4J] Fabric Loader ready");
							initialized = true;

							Circlor4jClient.init();

							System.out.println("[Circlor4J] Initialization complete");
							return;
						}
					} catch (ClassNotFoundException e) {
						if (attempts % 10 == 0) {
							System.out.println("[Circlor4J] Fabric Loader not found, waiting... (" +
									(maxAttempts - attempts) + "s remaining)");
						}
					} catch (Exception e) {
						if (attempts % 10 == 0) {
							System.out.println("[Circlor4J] Error checking Fabric Loader: " +
									e.getMessage() + " (" + (maxAttempts - attempts) + "s remaining)");
						}
						e.printStackTrace();
					}

				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					System.err.println("[Circlor4J] Waiting thread interrupted");
					return;
				}
			}

			if (!initialized) {
				System.err.println("[Circlor4J] Timeout waiting for Fabric Loader after " +
						maxAttempts + " seconds");
			}
		}, "Circlor4J-WaitThread");

		waitThread.setDaemon(true);
		waitThread.start();
	}
}