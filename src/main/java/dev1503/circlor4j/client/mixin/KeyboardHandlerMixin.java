package dev1503.circlor4j.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev1503.circlor4j.client.keybind.KeyBindManager;
import dev1503.circlor4j.ui.clickgui.ClickGuiScreen;
import dev1503.circlor4j.ui.screen.AddKeyBindDialog;
import dev1503.circlor4j.ui.screen.FilePickerScreen;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

	@Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
	private void circlor4jOnKeyPress(long handle, @KeyEvent.Action int action, KeyEvent event, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (handle != mc.getWindow().handle()) {
			return;
		}

		InputConstants.Key key = InputConstants.getKey(event);
		if (!isTextInputFocused()
			&& KeyBindManager.onKeyInput(key, event.hasShiftDown(), event.hasControlDown(), event.hasAltDown(), action)) {
			ci.cancel();
		}
	}

	/** Whether one of the hand-drawn text fields currently owns raw key input. */
	private static boolean isTextInputFocused() {
		return AddKeyBindDialog.isInputFocused()
			|| FilePickerScreen.isInputFocused()
			|| ClickGuiScreen.isBlockListInputFocused();
	}
}