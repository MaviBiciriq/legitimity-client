package com.bcrq.legit.client.module;

import com.bcrq.legit.client.module.modules.ArrayListModule;
import com.bcrq.legit.client.module.modules.AirJumpModule;
import com.bcrq.legit.client.module.modules.AutoExpModule;
import com.bcrq.legit.client.module.modules.BladeStanceModule;
import com.bcrq.legit.client.module.modules.ChamsModule;
import com.bcrq.legit.client.module.modules.ChromaHurtModule;
import com.bcrq.legit.client.module.modules.AutoCrystalModule;
import com.bcrq.legit.client.module.modules.AutoToolModule;
import com.bcrq.legit.client.module.modules.CrispHitModule;
import com.bcrq.legit.client.module.modules.EchoBotModule;
import com.bcrq.legit.client.module.modules.EspModule;
import com.bcrq.legit.client.module.modules.FreecamModule;
import com.bcrq.legit.client.module.modules.KillAuraModule;
import com.bcrq.legit.client.module.modules.FreelookModule;
import com.bcrq.legit.client.module.modules.FullBrightModule;
import com.bcrq.legit.client.module.modules.HandStyleModule;
import com.bcrq.legit.client.module.modules.HitboxAuraModule;
import com.bcrq.legit.client.module.modules.InventoryMoveModule;
import com.bcrq.legit.client.module.modules.LandingMarkerModule;
import com.bcrq.legit.client.module.modules.LowFireModule;
import com.bcrq.legit.client.module.modules.MotionHudModule;
import com.bcrq.legit.client.module.modules.NoParticlesModule;
import com.bcrq.legit.client.module.modules.ParkourModule;
import com.bcrq.legit.client.module.modules.PearlLandingPredictorModule;
import com.bcrq.legit.client.module.modules.RapidEquipModule;
import com.bcrq.legit.client.module.modules.ReachModule;
import com.bcrq.legit.client.module.modules.ScaffoldModule;
import com.bcrq.legit.client.module.modules.SpinModule;
import com.bcrq.legit.client.module.modules.TimeWarpModule;
import com.bcrq.legit.client.module.modules.ClearWaterModule;
import com.bcrq.legit.client.module.modules.TrustListModule;
import com.bcrq.legit.client.module.modules.VelocityModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class ModuleManager {
	private final List<Module> modules = new ArrayList<>();
	private final Map<Integer, Boolean> keyStates = new HashMap<>();

	public void bootstrap() {
		modules.clear();

		register(new CrispHitModule());
		register(new AutoCrystalModule());
		register(new KillAuraModule());
		register(new ReachModule());
		register(new FreecamModule());
		register(new FreelookModule());
		register(new InventoryMoveModule());
		register(new FullBrightModule());
		register(new LowFireModule());
		register(new ClearWaterModule());
		register(new EspModule());
		register(new HitboxAuraModule());
		register(new PearlLandingPredictorModule());
		register(new LandingMarkerModule());
		register(new TimeWarpModule());
		register(new NoParticlesModule());
		register(new MotionHudModule());
		register(new RapidEquipModule());
		register(new SpinModule());
		register(new ChromaHurtModule());
		register(new BladeStanceModule());
		register(new HandStyleModule());
		register(new EchoBotModule());
		register(new ScaffoldModule());
		register(new ParkourModule());
		register(new AirJumpModule());
		register(new AutoExpModule());
		register(new AutoToolModule());
		register(new TrustListModule());
		register(new VelocityModule());
		register(new ChamsModule());
		register(new ArrayListModule());
	}

	private <T extends Module> T register(T module) {
		modules.add(module);
		return module;
	}

	public void handleKeybinds(MinecraftClient client) {
		if (client.getWindow() == null) {
			return;
		}

		long handle = client.getWindow().getHandle();
		boolean allowToggles = client.currentScreen == null;

		for (Module module : modules) {
			int keyCode = module.getKeyCode();
			if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
				continue;
			}

			boolean down = InputUtil.isKeyPressed(handle, keyCode);
			boolean wasDown = keyStates.getOrDefault(keyCode, false);

			if (allowToggles && down && !wasDown) {
				module.toggle();
			}

			keyStates.put(keyCode, down);
		}
	}

	public void tick(MinecraftClient client) {
		for (Module module : modules) {
			module.updateAnimation();
			if (module.isEnabled()) {
				module.onTick(client);
			}
		}
	}

	public void renderHud(DrawContext context) {
		for (Module module : modules) {
			if (module.isEnabled()) {
				module.onHudRender(context);
			}
		}
	}

	public void renderWorld(WorldRenderContext context) {
		for (Module module : modules) {
			if (module.isEnabled()) {
				module.onWorldRender(context);
			}
		}
	}

	public <T extends Module> T getModule(Class<T> type) {
		for (Module module : modules) {
			if (type.isInstance(module)) {
				return type.cast(module);
			}
		}

		return null;
	}

	public List<Module> getModules() {
		return Collections.unmodifiableList(modules);
	}

	public List<Module> getModulesInCategory(ModuleCategory category) {
		List<Module> result = new ArrayList<>();
		for (Module module : modules) {
			if (module.getCategory() == category) {
				result.add(module);
			}
		}
		return result;
	}
}
