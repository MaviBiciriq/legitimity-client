package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.ColorSetting;
import com.bcrq.legit.client.setting.ColorValue;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class HitboxAuraModule extends Module {
	private final ColorSetting color = addSetting(new ColorSetting(this, "Color", "Hitbox color.", new ColorValue(96, 229, 255, 180, false, 0.22D)));
	private final BooleanSetting playersOnly = addSetting(new BooleanSetting(this, "Players Only", "Only draw boxes on player entities.", true));

	public HitboxAuraModule() {
		super("Hitbox Aura", "Draws visible client-side hitboxes with custom color or rainbow mode.", ModuleCategory.RENDER);
	}

	@Override
	public void onWorldRender(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null || context.consumers() == null || context.matrixStack() == null) {
			return;
		}

		Vec3d camera = client.gameRenderer.getCamera().getPos();
		VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLines());
		int colorValue = color.resolveColor();
		float red = ((colorValue >> 16) & 255) / 255.0f;
		float green = ((colorValue >> 8) & 255) / 255.0f;
		float blue = (colorValue & 255) / 255.0f;
		float alpha = ((colorValue >> 24) & 255) / 255.0f;

		for (Entity entity : client.world.getEntities()) {
			if (!(entity instanceof LivingEntity) || entity == client.player || !entity.isAlive()) {
				continue;
			}
			if (playersOnly.getValue() && !(entity instanceof PlayerEntity)) {
				continue;
			}
			if (entity.squaredDistanceTo(client.player) > 4096.0D) {
				continue;
			}

			Box box = entity.getBoundingBox().offset(-camera.x, -camera.y, -camera.z).expand(0.02D);
			VertexRendering.drawBox(context.matrixStack(), consumer, box, red, green, blue, alpha);
		}
	}
}
