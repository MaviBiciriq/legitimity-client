package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.ColorSetting;
import com.bcrq.legit.client.setting.ColorValue;
import com.bcrq.legit.client.setting.NumberSetting;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class LandingMarkerModule extends Module {
	private final ColorSetting lineColor = addSetting(
			new ColorSetting(this, "Line Color", "Predicted fall path color.", new ColorValue(80, 220, 255, 150, false, 0.12D)));
	private final ColorSetting markerColor = addSetting(
			new ColorSetting(this, "Marker Color", "Landing marker color.", new ColorValue(80, 220, 255, 220, false, 0.12D)));
	private final NumberSetting maxTicks = addSetting(
			new NumberSetting(this, "Max Ticks", "Maximum prediction ticks.", 160.0D, 20.0D, 300.0D, 5.0D));
	private final BooleanSetting onlyFalling = addSetting(
			new BooleanSetting(this, "Only Falling", "Render only while airborne or falling.", true));

	public LandingMarkerModule() {
		super("Landing Marker", "Predicts and marks where your current fall path will land.", ModuleCategory.RENDER);
	}

	@Override
	public void onWorldRender(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null || context.consumers() == null || context.matrixStack() == null) {
			return;
		}
		if (onlyFalling.getValue() && client.player.isOnGround()) {
			return;
		}

		Prediction prediction = predict(client.player);
		if (prediction == null) {
			return;
		}

		Vec3d camera = client.gameRenderer.getCamera().getPos();
		VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLines());
		Vec3d previous = null;
		for (Vec3d point : prediction.points()) {
			if (previous != null) {
				drawLine(context, consumer, previous.subtract(camera), point.subtract(camera), lineColor.resolveColor());
			}
			previous = point;
		}

		Vec3d landing = prediction.landing();
		Box marker = new Box(
				landing.x - 0.45D,
				landing.y + 0.01D,
				landing.z - 0.45D,
				landing.x + 0.45D,
				landing.y + 0.06D,
				landing.z + 0.45D
		).offset(-camera.x, -camera.y, -camera.z);
		drawBox(context, consumer, marker, markerColor.resolveColor());
	}

	private Prediction predict(Entity entity) {
		MinecraftClient client = MinecraftClient.getInstance();
		Vec3d position = entity.getPos();
		Vec3d velocity = entity.getVelocity();
		List<Vec3d> points = new ArrayList<>();
		points.add(position);

		for (int i = 0; i < maxTicks.getValue().intValue(); i++) {
			Vec3d next = position.add(velocity);
			BlockHitResult hit = client.world.raycast(new RaycastContext(
					position,
					next,
					RaycastContext.ShapeType.COLLIDER,
					RaycastContext.FluidHandling.ANY,
					entity
			));
			if (hit.getType() != HitResult.Type.MISS) {
				Vec3d landing = hit.getPos();
				points.add(landing);
				return new Prediction(points, landing);
			}

			position = next;
			points.add(position);
			velocity = new Vec3d(velocity.x * 0.91D, (velocity.y - 0.08D) * 0.98D, velocity.z * 0.91D);
			if (position.y < client.world.getBottomY() - 8) {
				break;
			}
		}

		return null;
	}

	private void drawBox(WorldRenderContext context, VertexConsumer consumer, Box box, int color) {
		float r = ((color >> 16) & 255) / 255.0f;
		float g = ((color >> 8) & 255) / 255.0f;
		float b = (color & 255) / 255.0f;
		float a = ((color >> 24) & 255) / 255.0f;
		VertexRendering.drawBox(context.matrixStack(), consumer, box, r, g, b, a);
	}

	private void drawLine(WorldRenderContext context, VertexConsumer consumer, Vec3d from, Vec3d to, int color) {
		float r = ((color >> 16) & 255) / 255.0f;
		float g = ((color >> 8) & 255) / 255.0f;
		float b = (color & 255) / 255.0f;
		float a = ((color >> 24) & 255) / 255.0f;
		Vec3d normal = to.subtract(from).normalize();
		var entry = context.matrixStack().peek();

		consumer.vertex(entry.getPositionMatrix(), (float) from.x, (float) from.y, (float) from.z)
				.color(r, g, b, a)
				.normal((float) normal.x, (float) normal.y, (float) normal.z);
		consumer.vertex(entry.getPositionMatrix(), (float) to.x, (float) to.y, (float) to.z)
				.color(r, g, b, a)
				.normal((float) normal.x, (float) normal.y, (float) normal.z);
	}

	private record Prediction(List<Vec3d> points, Vec3d landing) {
	}
}
