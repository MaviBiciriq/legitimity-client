package com.bcrq.legit.client;

import com.bcrq.legit.LegitimityMod;
import com.bcrq.legit.client.config.ConfigManager;
import com.bcrq.legit.client.gui.LegitClickGuiScreen;
import com.bcrq.legit.client.module.ModuleManager;
import com.bcrq.legit.client.trust.TrustListManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class LegitimityClient {
	private static final LegitimityClient INSTANCE = new LegitimityClient();

	private final ModuleManager moduleManager = new ModuleManager();
	private final ConfigManager configManager = new ConfigManager(moduleManager);
	private LegitClickGuiScreen clickGuiScreen;
	private KeyBinding menuKey;
	private boolean initialized;

	private LegitimityClient() {
	}

	public static LegitimityClient get() {
		return INSTANCE;
	}

	@SuppressWarnings("deprecation") // HudRenderCallback works correctly; migration to HudLayerRegistrationCallback is deferred
	public void initialize() {
		if (initialized) {
			return;
		}

		initialized = true;
		menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.bcrqlegit.open_menu",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_RIGHT_SHIFT,
			"category.bcrqlegit"
		));

		moduleManager.bootstrap();
		configManager.load();
		TrustListManager.get().load();

		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
		HudRenderCallback.EVENT.register((drawContext, _tickDelta) -> moduleManager.renderHud(drawContext));
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(moduleManager::renderWorld);

		LegitimityMod.LOGGER.info("Legitimity client systems initialized with {} modules", moduleManager.getModules().size());
	}

	private void onEndTick(MinecraftClient client) {
		InputMetrics.trim();

		while (menuKey.wasPressed()) {
			if (client.currentScreen instanceof LegitClickGuiScreen) {
				client.setScreen(null);
			} else {
				openClickGui(client);
			}
		}

		moduleManager.handleKeybinds(client);
		moduleManager.tick(client);
		configManager.flushIfDirty();
	}

	public void openClickGui(MinecraftClient client) {
		if (clickGuiScreen == null) {
			clickGuiScreen = new LegitClickGuiScreen();
		}
		client.setScreen(clickGuiScreen);
	}

	public void requestSave() {
		configManager.markDirty();
	}

	public ModuleManager getModuleManager() {
		return moduleManager;
	}
}
