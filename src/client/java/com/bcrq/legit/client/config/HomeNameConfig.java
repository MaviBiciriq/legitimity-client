package com.bcrq.legit.client.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class HomeNameConfig {
	private static final int HOME_COUNT = 5;
	private static final String[] labels = {
		"Home 1",
		"Home 2",
		"Home 3",
		"Home 4",
		"Home 5"
	};

	private HomeNameConfig() {
	}

	public static int size() {
		return HOME_COUNT;
	}

	public static String getLabel(int slot) {
		if (slot < 1 || slot > HOME_COUNT) {
			return "Home";
		}
		return labels[slot - 1];
	}

	public static void setLabel(int slot, String value) {
		if (slot < 1 || slot > HOME_COUNT) {
			return;
		}

		String trimmed = value == null ? "" : value.trim();
		labels[slot - 1] = trimmed.isEmpty() ? "Home " + slot : trimmed;
	}

	public static void load(JsonObject root) {
		resetDefaults();
		if (root == null || !root.has("homes")) {
			return;
		}

		JsonArray homes = root.getAsJsonArray("homes");
		for (int i = 0; i < HOME_COUNT && i < homes.size(); i++) {
			JsonElement element = homes.get(i);
			if (element != null && element.isJsonPrimitive()) {
				setLabel(i + 1, element.getAsString());
			}
		}
	}

	public static void save(JsonObject root) {
		JsonArray homes = new JsonArray();
		for (String label : labels) {
			homes.add(label);
		}
		root.add("homes", homes);
	}

	private static void resetDefaults() {
		for (int i = 0; i < HOME_COUNT; i++) {
			labels[i] = "Home " + (i + 1);
		}
	}
}
