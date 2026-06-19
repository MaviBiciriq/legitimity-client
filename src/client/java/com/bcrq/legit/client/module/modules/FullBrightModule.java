package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

public final class FullBrightModule extends Module {
	public FullBrightModule() {
		super("Fullbright", "Applies a clean fullbright effect using night vision style lighting.", ModuleCategory.RENDER);
	}

	@Override
	public void onDisable() {
		MinecraftClient client = MinecraftClient.getInstance();
		client.gameRenderer.getLightmapTextureManager().update(0.0f);
	}

	@Override
	public void onTick(MinecraftClient client) {
		client.gameRenderer.getLightmapTextureManager().update(0.0f);
	}
}
