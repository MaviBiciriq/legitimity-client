package com.bcrq.legit.client.trust;

import com.bcrq.legit.LegitimityMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;

public final class TrustListManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final TrustListManager INSTANCE = new TrustListManager();

	private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("bcrqlegit-trust-list.json");
	private final Map<UUID, TrustState> states = new HashMap<>();

	private TrustListManager() {
	}

	public static TrustListManager get() {
		return INSTANCE;
	}

	public void load() {
		states.clear();
		if (!Files.exists(configPath)) {
			return;
		}

		try {
			JsonObject root = GSON.fromJson(Files.readString(configPath, StandardCharsets.UTF_8), JsonObject.class);
			if (root == null) {
				return;
			}

			for (String key : root.keySet()) {
				try {
					states.put(UUID.fromString(key), TrustState.valueOf(root.get(key).getAsString()));
				} catch (Exception ignored) {
				}
			}
		} catch (Exception exception) {
			LegitimityMod.LOGGER.error("Failed to load trust list {}", configPath, exception);
		}
	}

	public void save() {
		try {
			Files.createDirectories(configPath.getParent());
			JsonObject root = new JsonObject();
			for (Map.Entry<UUID, TrustState> entry : states.entrySet()) {
				root.addProperty(entry.getKey().toString(), entry.getValue().name());
			}
			Files.writeString(configPath, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			LegitimityMod.LOGGER.error("Failed to save trust list {}", configPath, exception);
		}
	}

	public TrustState get(UUID uuid) {
		return states.getOrDefault(uuid, TrustState.SUSPICIOUS);
	}

	public void set(UUID uuid, TrustState state) {
		states.put(uuid, state);
		save();
	}
}
