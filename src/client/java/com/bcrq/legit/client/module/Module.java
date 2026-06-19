package com.bcrq.legit.client.module;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.setting.Setting;
import com.bcrq.legit.client.util.AnimationUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public abstract class Module {
	private final String name;
	private final String description;
	private final ModuleCategory category;
	private final List<Setting<?>> settings = new ArrayList<>();
	private boolean enabled;
	private int keyCode = GLFW.GLFW_KEY_UNKNOWN;
	private float toggleAnimation;

	protected Module(String name, String description, ModuleCategory category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	protected <T extends Setting<?>> T addSetting(T setting) {
		settings.add(setting);
		return setting;
	}

	public final void toggle() {
		setEnabled(!enabled);
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}

		this.enabled = enabled;
		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}

		LegitimityClient.get().requestSave();
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
		LegitimityClient.get().requestSave();
	}

	public void updateAnimation() {
		toggleAnimation = AnimationUtil.smooth(toggleAnimation, enabled ? 1.0f : 0.0f, 0.25f);
	}

	public void onEnable() {
	}

	public void onDisable() {
	}

	public void onTick(MinecraftClient client) {
	}

	public void onHudRender(DrawContext context) {
	}

	public void onWorldRender(WorldRenderContext context) {
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public ModuleCategory getCategory() {
		return category;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getKeyCode() {
		return keyCode;
	}

	public float getToggleAnimation() {
		return toggleAnimation;
	}

	public List<Setting<?>> getSettings() {
		return Collections.unmodifiableList(settings);
	}
}
