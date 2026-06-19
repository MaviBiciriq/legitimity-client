package com.bcrq.legit.client.config;

import com.bcrq.legit.LegitimityMod;
import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleManager;
import com.bcrq.legit.client.setting.Setting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final ModuleManager moduleManager;
	private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("bcrqlegit-client.json");
	private boolean dirty;

	public ConfigManager(ModuleManager moduleManager) {
		this.moduleManager = moduleManager;
	}

	public void load() {
		if (!Files.exists(configPath)) {
			return;
		}

		try {
			String raw = Files.readString(configPath, StandardCharsets.UTF_8);
			JsonObject root = GSON.fromJson(raw, JsonObject.class);
			if (root == null) {
				return;
			}

			HomeNameConfig.load(root);
			if (!root.has("modules")) {
				dirty = false;
				return;
			}

			JsonObject modules = root.getAsJsonObject("modules");
			for (Module module : moduleManager.getModules()) {
				if (!modules.has(module.getName())) {
					continue;
				}

				JsonObject moduleJson = modules.getAsJsonObject(module.getName());
				if (moduleJson.has("enabled")) {
					module.setEnabled(moduleJson.get("enabled").getAsBoolean());
				}
				if (moduleJson.has("key")) {
					module.setKeyCode(moduleJson.get("key").getAsInt());
				}
				if (moduleJson.has("settings")) {
					JsonObject settings = moduleJson.getAsJsonObject("settings");
					for (Setting<?> setting : module.getSettings()) {
						JsonElement settingJson = settings.get(setting.getName());
						if (settingJson != null) {
							setting.fromJson(settingJson);
						}
					}
				}
			}

			dirty = false;
		} catch (Exception exception) {
			LegitimityMod.LOGGER.error("Failed to load config {}", configPath, exception);
		}
	}

	public void markDirty() {
		dirty = true;
	}

	public void flushIfDirty() {
		if (!dirty) {
			return;
		}

		save();
		dirty = false;
	}

	private void save() {
		try {
			Files.createDirectories(configPath.getParent());

			JsonObject root = new JsonObject();
			HomeNameConfig.save(root);
			JsonObject modules = new JsonObject();
			root.add("modules", modules);

			for (Module module : moduleManager.getModules()) {
				JsonObject moduleJson = new JsonObject();
				moduleJson.addProperty("enabled", module.isEnabled());
				moduleJson.addProperty("key", module.getKeyCode());

				JsonObject settings = new JsonObject();
				for (Setting<?> setting : module.getSettings()) {
					settings.add(setting.getName(), setting.toJson());
				}

				moduleJson.add("settings", settings);
				modules.add(module.getName(), moduleJson);
			}

			Files.writeString(configPath, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			LegitimityMod.LOGGER.error("Failed to save config {}", configPath, exception);
		}
	}
}
