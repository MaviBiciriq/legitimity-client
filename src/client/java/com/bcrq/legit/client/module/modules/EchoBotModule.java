package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.entity.LegitCameraEntity;
import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.NumberSetting;
import com.bcrq.legit.client.setting.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class EchoBotModule extends Module {
	private static final int ENTITY_ID = -190431;

	private final StringSetting name = addSetting(new StringSetting(this, "Name", "Bot display name.", "SteveBot", 16));
	private final NumberSetting health = addSetting(new NumberSetting(this, "Health", "Displayed bot health.", 20.0D, 1.0D, 40.0D, 1.0D));
	private final NumberSetting distance = addSetting(new NumberSetting(this, "Distance", "Spawn distance in front of the player.", 2.2D, 0.5D, 6.0D, 0.1D));

	private LegitCameraEntity bot;

	public EchoBotModule() {
		super("Echo Bot", "Spawns a client-only Steve-skinned practice bot.", ModuleCategory.MISC);
	}

	@Override
	public void onEnable() {
		spawnBot(MinecraftClient.getInstance());
	}

	@Override
	public void onDisable() {
		removeBot(MinecraftClient.getInstance());
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			setEnabled(false);
			return;
		}

		if (bot == null || client.world.getEntityById(ENTITY_ID) == null) {
			spawnBot(client);
			return;
		}

		bot.setCustomName(Text.literal(name.getValue()));
		bot.setCustomNameVisible(true);
		bot.setHealth((float) health.getValue().doubleValue());
	}

	private void spawnBot(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return;
		}

		removeBot(client);

		bot = new LegitCameraEntity(client.world, name.getValue());
		bot.setId(ENTITY_ID);
		bot.setCustomName(Text.literal(name.getValue()));
		bot.setCustomNameVisible(true);
		bot.setHealth((float) health.getValue().doubleValue());
		bot.setYaw(client.player.getYaw());
		bot.setPitch(client.player.getPitch());

		Vec3d forward = Vec3d.fromPolar(0.0f, client.player.getYaw()).normalize().multiply(distance.getValue());
		Vec3d position = client.player.getPos().add(forward);
		bot.refreshPositionAndAngles(position.x, position.y, position.z, client.player.getYaw(), client.player.getPitch());
		client.world.addEntity(bot);
	}

	private void removeBot(MinecraftClient client) {
		if (client.world == null) {
			bot = null;
			return;
		}

		Entity existing = client.world.getEntityById(ENTITY_ID);
		if (existing != null) {
			client.world.removeEntity(existing.getId(), RemovalReason.DISCARDED);
		}
		bot = null;
	}

	public void handleAttack(PlayerEntity attacker, Entity target) {
		if (bot == null || target != bot || attacker == null) {
			return;
		}

		float newHealth = Math.max(0.0f, bot.getHealth() - 1.0f);
		bot.setHealth(newHealth);
		bot.hurtTime = 10;
		bot.maxHurtTime = 10;

		Vec3d direction = bot.getPos().subtract(attacker.getPos());
		if (direction.lengthSquared() < 1.0E-4D) {
			direction = Vec3d.fromPolar(0.0f, attacker.getYaw());
		}
		direction = direction.normalize().multiply(0.45D);
		bot.setVelocity(direction.x, 0.28D, direction.z);
		bot.velocityModified = true;

		if (newHealth <= 0.0f) {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player != null) {
				Vec3d forward = Vec3d.fromPolar(0.0f, client.player.getYaw()).normalize().multiply(distance.getValue());
				Vec3d position = client.player.getPos().add(forward);
				bot.refreshPositionAndAngles(position.x, position.y, position.z, client.player.getYaw(), client.player.getPitch());
				bot.setHealth((float) health.getValue().doubleValue());
			}
		}
	}
}
