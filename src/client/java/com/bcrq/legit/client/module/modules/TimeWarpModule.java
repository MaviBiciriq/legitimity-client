package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public final class TimeWarpModule extends Module {
	private final NumberSetting time = addSetting(new NumberSetting(this, "Time", "The visual time of day to force.", 6000.0D, 0.0D, 24000.0D, 50.0D));
	private long serverTime;
	private long serverTimeOfDay;
	private boolean serverTickDayTime = true;
	private boolean hasServerSnapshot;

	public TimeWarpModule() {
		super("Time Warp", "Overrides client time without touching the server.", ModuleCategory.RENDER);
	}

	@Override
	public void onEnable() {
		ClientWorld world = MinecraftClient.getInstance().world;
		hasServerSnapshot = false;
		if (world != null) {
			captureServerTime(world.getTime(), world.getTimeOfDay(), true);
		}
	}

	@Override
	public void onDisable() {
		restoreWorldTime(MinecraftClient.getInstance().world);
	}

	@Override
	public void onTick(MinecraftClient client) {
		ClientWorld world = client.world;
		if (world == null) {
			return;
		}

		if (!hasServerSnapshot) {
			captureServerTime(world.getTime(), world.getTimeOfDay(), true);
		}

		world.setTime(world.getTime(), time.getValue().longValue(), false);
	}

	public void captureServerTime(long time, long timeOfDay, boolean tickDayTime) {
		serverTime = time;
		serverTimeOfDay = timeOfDay;
		serverTickDayTime = tickDayTime;
		hasServerSnapshot = true;
	}

	private void restoreWorldTime(ClientWorld world) {
		if (world != null && hasServerSnapshot) {
			world.setTime(serverTime, serverTimeOfDay, serverTickDayTime);
		}
	}
}
