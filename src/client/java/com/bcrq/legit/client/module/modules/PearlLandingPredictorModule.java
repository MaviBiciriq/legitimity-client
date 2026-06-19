package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.ColorSetting;
import com.bcrq.legit.client.setting.ColorValue;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public final class PearlLandingPredictorModule extends Module {
	private final ColorSetting lineColor = addSetting(new ColorSetting(this, "Line Color", "Trajectory line color.", new ColorValue(255, 85, 85, 170, false, 0.12D)));
	private final ColorSetting boxColor = addSetting(new ColorSetting(this, "Box Color", "Landing box color.", new ColorValue(255, 55, 55, 190, false, 0.12D)));

	public PearlLandingPredictorModule() {
		super("Pearl Landing Predictor", "Marks the expected pearl landing area with a red 1x1 box.", ModuleCategory.RENDER);
	}

	@Override
	public void onWorldRender(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null || context.consumers() == null || context.matrixStack() == null) {
			return;
		}

		Vec3d camera = client.gameRenderer.getCamera().getPos();
		VertexConsumer consumer = context.consumers().getBuffer(RenderLayer.getLines());
		int lineArgb = lineColor.resolveColor();
		int boxArgb = boxColor.resolveColor();
		boolean renderedLivePearl = false;

		for (Entity entity : client.world.getEntities()) {
			if (!(entity instanceof EnderPearlEntity pearl) || pearl.getOwner() != client.player) {
				continue;
			}

			Prediction prediction = predict(pearl.getPos(), pearl.getVelocity(), pearl);
			if (prediction == null) {
				continue;
			}

			renderedLivePearl = true;
			renderPrediction(context, consumer, camera, prediction, lineArgb, boxArgb);
		}

		if (!renderedLivePearl && isHoldingPearl(client)) {
			Vec3d direction = client.player.getRotationVec(1.0f).normalize();
			Vec3d start = client.player.getEyePos().add(direction.multiply(0.16D));
			Vec3d velocity = direction.multiply(1.5D).add(client.player.getVelocity());
			Prediction prediction = predict(start, velocity, client.player);
			if (prediction != null) {
				renderPrediction(context, consumer, camera, prediction, lineArgb, boxArgb);
			}
		}
	}

	private boolean isHoldingPearl(MinecraftClient client) {
		return client.player != null && (
			client.player.getMainHandStack().isOf(Items.ENDER_PEARL) ||
			client.player.getOffHandStack().isOf(Items.ENDER_PEARL)
		);
	}

	private void renderPrediction(WorldRenderContext context, VertexConsumer consumer, Vec3d camera, Prediction prediction, int lineArgb, int boxArgb) {
		Vec3d previous = null;
		for (Vec3d point : prediction.points()) {
			if (previous != null) {
				drawLineSegment(context, consumer, previous.subtract(camera), point.subtract(camera), lineArgb);
			}
			previous = point;
		}

		Vec3d landing = prediction.landing();
		Box box = new Box(
			landing.x - 0.5D,
			landing.y,
			landing.z - 0.5D,
			landing.x + 0.5D,
			landing.y + 1.0D,
			landing.z + 0.5D
		).offset(-camera.x, -camera.y, -camera.z);

		float r = ((boxArgb >> 16) & 255) / 255.0f;
		float g = ((boxArgb >> 8) & 255) / 255.0f;
		float b = (boxArgb & 255) / 255.0f;
		float a = ((boxArgb >> 24) & 255) / 255.0f;
		VertexRendering.drawBox(context.matrixStack(), consumer, box, r, g, b, a);
	}

	private Prediction predict(Vec3d start, Vec3d startVelocity, Entity collider) {
		MinecraftClient client = MinecraftClient.getInstance();
		Vec3d position = start;
		Vec3d velocity = startVelocity;
		java.util.List<Vec3d> points = new java.util.ArrayList<>();
		points.add(position);

		for (int i = 0; i < 120; i++) {
			Vec3d next = position.add(velocity);
			BlockHitResult hit = client.world.raycast(new RaycastContext(position, next, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.ANY, collider));
			if (hit.getType() != HitResult.Type.MISS) {
				Vec3d landing = hit.getPos();
				points.add(landing);
				return new Prediction(points, landing);
			}

			position = next;
			points.add(position);
			velocity = velocity.multiply(0.99D).add(0.0D, -0.03D, 0.0D);

			if (position.y < client.world.getBottomY() - 4) {
				break;
			}
		}

		return null;
	}

	private void drawLineSegment(WorldRenderContext context, VertexConsumer consumer, Vec3d from, Vec3d to, int color) {
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

	private record Prediction(java.util.List<Vec3d> points, Vec3d landing) {
	}
}
