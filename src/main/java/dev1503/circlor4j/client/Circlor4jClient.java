package dev1503.circlor4j.client;

import dev1503.circlor4j.client.keybind.KeyBindManager;
import dev1503.circlor4j.client.module.ModuleManager;
import dev1503.circlor4j.client.module.modules.AutoClickerModule;
import dev1503.circlor4j.client.module.modules.AimBotModule;
import dev1503.circlor4j.client.module.modules.AntiDebuffModule;
import dev1503.circlor4j.client.module.modules.AntiKnockbackModule;
import dev1503.circlor4j.client.module.modules.ArraylistModule;
import dev1503.circlor4j.client.module.modules.AutoTotemModule;
import dev1503.circlor4j.client.module.modules.AutoSprintModule;
import dev1503.circlor4j.client.module.modules.AirJumpModule;
import dev1503.circlor4j.client.module.modules.ClickGuiModule;
import dev1503.circlor4j.client.module.modules.CriticalsModule;
import dev1503.circlor4j.client.module.modules.CrystalAuraModule;
import dev1503.circlor4j.client.module.modules.DerpModule;
import dev1503.circlor4j.client.module.modules.EagleModule;
import dev1503.circlor4j.client.module.modules.EspModule;
import dev1503.circlor4j.client.module.modules.FastStopModule;
import dev1503.circlor4j.client.module.modules.FlyModule;
import dev1503.circlor4j.client.module.modules.FullBrightModule;
import dev1503.circlor4j.client.module.modules.LowFireModule;
import dev1503.circlor4j.client.module.modules.NoHurtCamModule;
import dev1503.circlor4j.client.module.modules.FreecamModule;
import dev1503.circlor4j.client.module.modules.FreelookModule;
import dev1503.circlor4j.client.module.modules.HitboxModule;
import dev1503.circlor4j.client.module.modules.HighJumpModule;
import dev1503.circlor4j.client.module.modules.InventoryMoveModule;
import dev1503.circlor4j.client.module.modules.ItemTagModule;
import dev1503.circlor4j.client.module.modules.JetpackModule;
import dev1503.circlor4j.client.module.modules.KillAuraModule;
import dev1503.circlor4j.client.module.modules.LanguageModule;
import dev1503.circlor4j.client.module.modules.NoCameraClipModule;
import dev1503.circlor4j.client.module.modules.NoFallModule;
import dev1503.circlor4j.client.module.modules.NoSlowDownModule;
import dev1503.circlor4j.client.module.modules.NoWebModule;
import dev1503.circlor4j.client.module.modules.NukerModule;
import dev1503.circlor4j.client.module.modules.NoJumpDelayModule;
import dev1503.circlor4j.client.module.modules.ReachModule;
import dev1503.circlor4j.client.module.modules.ScaffoldModule;
import dev1503.circlor4j.client.module.modules.SpeedModule;
import dev1503.circlor4j.client.module.modules.TimerModule;
import dev1503.circlor4j.client.module.modules.TracerModule;
import dev1503.circlor4j.client.module.modules.TrueSightModule;
import dev1503.circlor4j.client.module.modules.XrayModule;
import dev1503.circlor4j.client.module.modules.ZoomModule;
import dev1503.circlor4j.ui.StatusManager;
import dev1503.circlor4j.ui.clickgui.ClickGuiScreen;
import dev1503.circlor4j.ui.component.CategoryWindow;
import dev1503.circlor4j.ui.screen.KeyBindScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Client-side business initialisation, called by the Fabric entry point.
 * Everything after this point is driven by Mixins (see the `mixin` package).
 */
public final class Circlor4jClient {
	private Circlor4jClient() {
	}

	public static void init() {
		StatusManager status = StatusManager.getInstance();
		status.setListener(new StatusManager.Listener() {
			@Override
			public void onValueChange(String path, double value) {
				ModuleManager.applyStatus(path, value);
			}

			@Override
			public void onActionTrigger(String path) {
				if (CategoryWindow.KEYBINDS_ACTION_PATH.equals(path)) {
					Minecraft mc = Minecraft.getInstance();
					Screen current = mc.gui.screen();
					mc.gui.setScreen(new KeyBindScreen(current));
					if (current instanceof ClickGuiScreen) {
						StatusManager.getInstance().setValue(ClickGuiModule.ID + "/enabled", 1.0);
					}
				}
			}
		});

		ModuleManager.register(new ArraylistModule(status));
		ModuleManager.register(new AirJumpModule(status));
		ModuleManager.register(new AntiDebuffModule(status));
		ModuleManager.register(new AimBotModule(status));
		ModuleManager.register(new AntiKnockbackModule(status));
		ModuleManager.register(new AutoClickerModule(status));
		ModuleManager.register(new AutoTotemModule(status));
		ModuleManager.register(new AutoSprintModule(status));
		ModuleManager.register(new ClickGuiModule(status));
		ModuleManager.register(new CriticalsModule(status));
		ModuleManager.register(new CrystalAuraModule(status));
		ModuleManager.register(new DerpModule(status));
		ModuleManager.register(new EagleModule(status));
		ModuleManager.register(new EspModule(status));
		ModuleManager.register(new FastStopModule(status));
		ModuleManager.register(new FlyModule(status));
		ModuleManager.register(new FreecamModule(status));
		ModuleManager.register(new FreelookModule(status));
		ModuleManager.register(new FullBrightModule(status));
		ModuleManager.register(new HitboxModule(status));
		ModuleManager.register(new HighJumpModule(status));
		ModuleManager.register(new InventoryMoveModule(status));
		ModuleManager.register(new ItemTagModule(status));
		ModuleManager.register(new JetpackModule(status));
		ModuleManager.register(new KillAuraModule(status));
		ModuleManager.register(new LanguageModule(status));
		ModuleManager.register(new LowFireModule(status));
		ModuleManager.register(new NoHurtCamModule(status));
		ModuleManager.register(new NoCameraClipModule(status));
		ModuleManager.register(new NoFallModule(status));
		ModuleManager.register(new NoSlowDownModule(status));
		ModuleManager.register(new NoWebModule(status));
		ModuleManager.register(new NukerModule(status));
		ModuleManager.register(new NoJumpDelayModule(status));
		ModuleManager.register(new ReachModule(status));
		ModuleManager.register(new ScaffoldModule(status));
		ModuleManager.register(new SpeedModule(status));
		ModuleManager.register(new TimerModule(status));
		ModuleManager.register(new TracerModule(status));
		ModuleManager.register(new TrueSightModule(status));
		ModuleManager.register(new XrayModule(status));
		ModuleManager.register(new ZoomModule(status));

		KeyBindManager.init();
	}
}
