package com.bcrq.legit.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

public final class GuiLayoutConfig {
	public static final class WindowState {
		public double x;
		public double y;
		public boolean collapsed;

		public WindowState(double x, double y, boolean collapsed) {
			this.x = x;
			this.y = y;
			this.collapsed = collapsed;
		}
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("legitimity").resolve("guilayout.json");

	private GuiLayoutConfig() {}

	public static Map<String, WindowState> load() {
		try {
			if (!Files.exists(FILE)) return new HashMap<>();
			try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
				Map<String, WindowState> layout = GSON.fromJson(reader, new TypeToken<HashMap<String, WindowState>>() {}.getType());
				return layout == null ? new HashMap<>() : layout;
			}
		} catch (IOException | RuntimeException ignored) {
			return new HashMap<>();
		}
	}

	public static void save(Map<String, WindowState> layout) {
		try {
			Files.createDirectories(FILE.getParent());
			try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
				GSON.toJson(layout, writer);
			}
		} catch (IOException ignored) {
		}
	}
}
