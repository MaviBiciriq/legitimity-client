package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.ColorSetting;
import com.bcrq.legit.client.setting.ColorValue;
import com.bcrq.legit.client.trust.TrustListManager;
import com.bcrq.legit.client.trust.TrustState;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class TrustListModule extends Module {
	private final ColorSetting safeColor = addSetting(new ColorSetting(this, "Safe Color", "Color used for trusted players.", new ColorValue(90, 255, 125, 210, false, 0.12D)));
	private final ColorSetting enemyColor = addSetting(new ColorSetting(this, "Enemy Color", "Color used for enemy players.", new ColorValue(255, 75, 75, 210, false, 0.12D)));
	private final ColorSetting suspiciousColor = addSetting(new ColorSetting(this, "Suspicious Color", "Color used for untagged players.", new ColorValue(255, 210, 90, 210, false, 0.12D)));
	private int lastHurtTime;

	public TrustListModule() {
		super("Trust List", "Marks players as safe, enemy or suspicious and shows an icon above them.", ModuleCategory.UTILITY);
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null) {
			return;
		}

		if (client.player.hurtTime > 0 && client.player.hurtTime > lastHurtTime) {
			DamageSource source = client.player.getRecentDamageSource();
			if (source != null && source.getAttacker() instanceof PlayerEntity attacker) {
				mark(attacker, TrustState.ENEMY);
			}
		}

		lastHurtTime = client.player.hurtTime;
	}

	@Override
	public void onWorldRender(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null || context.consumers() == null || context.matrixStack() == null) {
			return;
		}

		Vec3d camera = client.gameRenderer.getCamera().getPos();
		VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLines());
		for (Entity entity : client.world.getEntities()) {
			if (!(entity instanceof PlayerEntity player) || player == client.player || !player.isAlive()) {
				continue;
			}

			int color = resolveColor(markState(player));
			float r = ((color >> 16) & 255) / 255.0f;
			float g = ((color >> 8) & 255) / 255.0f;
			float b = (color & 255) / 255.0f;
			float a = ((color >> 24) & 255) / 255.0f;

			Vec3d pos = player.getLerpedPos(context.tickCounter().getTickDelta(true));
			Box icon = new Box(
				pos.x - 0.15D,
				pos.y + player.getHeight() + 0.35D,
				pos.z - 0.15D,
				pos.x + 0.15D,
				pos.y + player.getHeight() + 0.65D,
				pos.z + 0.15D
			).offset(-camera.x, -camera.y, -camera.z);
			VertexRendering.drawBox(context.matrixStack(), consumer, icon, r, g, b, a);
		}
	}

	public void markSafe(PlayerEntity player) {
		mark(player, TrustState.SAFE);
	}

	public void markEnemy(Entity entity) {
		if (entity instanceof PlayerEntity player) {
			mark(player, TrustState.ENEMY);
		}
	}

	public TrustState markState(PlayerEntity player) {
		return TrustListManager.get().get(player.getUuid());
	}

	private void mark(PlayerEntity player, TrustState state) {
		TrustListManager.get().set(player.getUuid(), state);
	}

	private int resolveColor(TrustState state) {
		return switch (state) {
			case SAFE -> safeColor.resolveColor();
			case ENEMY -> enemyColor.resolveColor();
			case SUSPICIOUS -> suspiciousColor.resolveColor();
		};
	}
}
