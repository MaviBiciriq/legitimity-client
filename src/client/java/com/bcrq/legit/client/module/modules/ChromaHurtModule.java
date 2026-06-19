package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.ColorSetting;
import com.bcrq.legit.client.setting.ColorValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class ChromaHurtModule extends Module {
	private final ColorSetting tint = addSetting(new ColorSetting(this, "Tint", "Custom hurt flash tint.", new ColorValue(255, 85, 110, 170, false, 0.18D)));

	public ChromaHurtModule() {
		super("Chroma Hurt", "Replaces the vanilla red hurt flash with a custom tint.", ModuleCategory.RENDER);
	}

	public int getTintColor() {
		return tint.resolveColor();
	}

	@Override
	public void onHudRender(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.player.hurtTime <= 0) {
			return;
		}

		float progress = Math.min(1.0f, client.player.hurtTime / 10.0f);
		int color = getTintColor();
		int alpha = Math.min(255, (int) (((color >>> 24) & 255) * progress));
		context.fill(0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), (color & 0x00FFFFFF) | (alpha << 24));
	}
}
