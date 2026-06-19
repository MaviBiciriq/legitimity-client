package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public final class InventoryMoveModule extends Module {
	public InventoryMoveModule() {
		super("Inventory Move", "Keeps movement keys alive while container screens are open.", ModuleCategory.UTILITY);
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (!(client.currentScreen instanceof HandledScreen) || client.currentScreen instanceof ChatScreen) {
			return;
		}
		if (client.currentScreen instanceof ParentElement parent && parent.getFocused() instanceof TextFieldWidget) {
			return;
		}

		long handle = client.getWindow().getHandle();
		sync(client.options.forwardKey, handle);
		sync(client.options.backKey, handle);
		sync(client.options.leftKey, handle);
		sync(client.options.rightKey, handle);
		sync(client.options.jumpKey, handle);
		sync(client.options.sprintKey, handle);
		sync(client.options.sneakKey, handle);
	}

	private void sync(KeyBinding keyBinding, long handle) {
		InputUtil.Key key = InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey());
		KeyBinding.setKeyPressed(key, InputUtil.isKeyPressed(handle, key.getCode()));
	}
}
