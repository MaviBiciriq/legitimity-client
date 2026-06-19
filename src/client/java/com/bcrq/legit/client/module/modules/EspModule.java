package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.ColorSetting;
import com.bcrq.legit.client.setting.ColorValue;
import com.bcrq.legit.client.setting.EnumSetting;
import com.bcrq.legit.client.setting.NumberSetting;
import com.bcrq.legit.client.trust.TrustListManager;
import com.bcrq.legit.client.trust.TrustState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.block.entity.CrafterBlockEntity;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.TrappedChestBlockEntity;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

public final class EspModule extends Module {
	private static final Set<Block> RARE_ORES = Set.of(
		Blocks.COAL_ORE,
		Blocks.DEEPSLATE_COAL_ORE,
		Blocks.COPPER_ORE,
		Blocks.DEEPSLATE_COPPER_ORE,
		Blocks.IRON_ORE,
		Blocks.DEEPSLATE_IRON_ORE,
		Blocks.GOLD_ORE,
		Blocks.DEEPSLATE_GOLD_ORE,
		Blocks.REDSTONE_ORE,
		Blocks.DEEPSLATE_REDSTONE_ORE,
		Blocks.LAPIS_ORE,
		Blocks.DEEPSLATE_LAPIS_ORE,
		Blocks.DIAMOND_ORE,
		Blocks.DEEPSLATE_DIAMOND_ORE,
		Blocks.EMERALD_ORE,
		Blocks.DEEPSLATE_EMERALD_ORE,
		Blocks.NETHER_GOLD_ORE,
		Blocks.NETHER_QUARTZ_ORE,
		Blocks.ANCIENT_DEBRIS,
		Blocks.BUDDING_AMETHYST
	);

	private final BooleanSetting outline = addSetting(new BooleanSetting(this, "Outline", "Draw crisp outline boxes around targets.", true));
	private final BooleanSetting fill = addSetting(new BooleanSetting(this, "Fill", "Draw translucent filled boxes inside outlines.", true));
	private final BooleanSetting targetTracers = addSetting(new BooleanSetting(this, "Target Tracers", "Draw tracers for entities, items and projectiles.", true));
	private final BooleanSetting blockTracers = addSetting(new BooleanSetting(this, "Block Tracers", "Draw tracers for highlighted block targets.", false));
	private final EnumSetting<TracerOrigin> tracerOrigin = addSetting(new EnumSetting<>(this, "Tracer Origin", "Where tracers should begin on screen.", TracerOrigin.CURSOR, TracerOrigin.class));
	private final NumberSetting targetRange = addSetting(new NumberSetting(this, "Target Range", "Range for entities, items and projectiles.", 96.0D, 16.0D, 192.0D, 4.0D));
	private final NumberSetting blockRange = addSetting(new NumberSetting(this, "Block Range", "Range for storage and utility block entity scans.", 64.0D, 16.0D, 128.0D, 4.0D));
	private final NumberSetting rareRange = addSetting(new NumberSetting(this, "Rare Range", "Range for ore and rare block scans.", 32.0D, 8.0D, 64.0D, 4.0D));
	private final NumberSetting fillStrength = addSetting(new NumberSetting(this, "Fill Strength", "How strong the box fill alpha should be.", 28.0D, 5.0D, 100.0D, 1.0D));
	private final NumberSetting maxBlocks = addSetting(new NumberSetting(this, "Max Blocks", "Caps how many block targets are rendered each frame.", 96.0D, 16.0D, 256.0D, 8.0D));

	private final BooleanSetting players = addSetting(new BooleanSetting(this, "Players", "Render ESP on other players.", true));
	private final BooleanSetting trustColors = addSetting(new BooleanSetting(this, "Trust Colors", "Use the trust-list palette for players.", true));
	private final BooleanSetting hostiles = addSetting(new BooleanSetting(this, "Hostiles", "Render ESP on hostile mobs.", true));
	private final BooleanSetting passives = addSetting(new BooleanSetting(this, "Passives", "Render ESP on animals and villagers.", false));
	private final BooleanSetting neutrals = addSetting(new BooleanSetting(this, "Neutral Mobs", "Render ESP on mobs that are neither passive nor hostile.", false));
	private final BooleanSetting items = addSetting(new BooleanSetting(this, "Items", "Render ESP on dropped items.", true));
	private final BooleanSetting projectiles = addSetting(new BooleanSetting(this, "Projectiles", "Render ESP on live projectiles.", true));
	private final BooleanSetting ownProjectiles = addSetting(new BooleanSetting(this, "Own Projectiles", "Also render projectiles you fired yourself.", false));
	private final BooleanSetting invisibleTargets = addSetting(new BooleanSetting(this, "Invisible Targets", "Include invisible entities in ESP.", false));

	private final BooleanSetting storageBlocks = addSetting(new BooleanSetting(this, "Storage Blocks", "Highlight storage and container blocks.", true));
	private final BooleanSetting utilityBlocks = addSetting(new BooleanSetting(this, "Utility Blocks", "Highlight crafting and functional blocks.", true));
	private final BooleanSetting rareBlocks = addSetting(new BooleanSetting(this, "Rare Blocks", "Highlight spawners, vaults and all ore-type rare blocks.", true));

	private final ColorSetting playerColor = addSetting(new ColorSetting(this, "Player Color", "Fallback color for player ESP.", new ColorValue(85, 195, 255, 190, false, 0.14D)));
	private final ColorSetting safeColor = addSetting(new ColorSetting(this, "Safe Color", "Color used for trusted players.", new ColorValue(92, 255, 135, 205, false, 0.14D)));
	private final ColorSetting enemyColor = addSetting(new ColorSetting(this, "Enemy Color", "Color used for enemy players.", new ColorValue(255, 88, 88, 210, false, 0.14D)));
	private final ColorSetting suspiciousColor = addSetting(new ColorSetting(this, "Suspicious Color", "Color used for untagged players.", new ColorValue(255, 205, 90, 205, false, 0.14D)));
	private final ColorSetting hostileColor = addSetting(new ColorSetting(this, "Hostile Color", "Color used for hostile mobs.", new ColorValue(255, 110, 120, 185, false, 0.14D)));
	private final ColorSetting passiveColor = addSetting(new ColorSetting(this, "Passive Color", "Color used for passive mobs.", new ColorValue(125, 255, 172, 185, false, 0.14D)));
	private final ColorSetting neutralColor = addSetting(new ColorSetting(this, "Neutral Color", "Color used for neutral mobs.", new ColorValue(170, 135, 255, 185, false, 0.14D)));
	private final ColorSetting itemColor = addSetting(new ColorSetting(this, "Item Color", "Color used for dropped items.", new ColorValue(255, 244, 130, 185, false, 0.14D)));
	private final ColorSetting projectileColor = addSetting(new ColorSetting(this, "Projectile Color", "Color used for projectiles.", new ColorValue(255, 150, 70, 185, false, 0.14D)));
	private final ColorSetting storageColor = addSetting(new ColorSetting(this, "Storage Color", "Color used for storage block ESP.", new ColorValue(110, 225, 255, 195, false, 0.14D)));
	private final ColorSetting utilityColor = addSetting(new ColorSetting(this, "Utility Color", "Color used for utility block ESP.", new ColorValue(180, 140, 255, 195, false, 0.14D)));
	private final ColorSetting rareColor = addSetting(new ColorSetting(this, "Rare Block Color", "Color used for rare blocks and ores.", new ColorValue(255, 110, 210, 205, false, 0.14D)));

	private final List<BlockCandidate> cachedRareBlocks = new ArrayList<>();
	private BlockPos lastRareScanOrigin = BlockPos.ORIGIN;
	private int rareScanCooldown;

	public EspModule() {
		super("ESP", "Comprehensive target ESP with entity, projectile, item and block overlays.", ModuleCategory.RENDER);
	}

	@Override
	public void onDisable() {
		cachedRareBlocks.clear();
		rareScanCooldown = 0;
	}

	@Override
	public void onTick(MinecraftClient client) {
		refreshRareBlocks(client);
	}

	@Override
	public void onWorldRender(WorldRenderContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.world == null || client.player == null || context.consumers() == null || context.matrixStack() == null) {
			return;
		}

		Vec3d camera = context.camera().getPos();
		Vec3d playerPos = client.player.getPos();

		renderDynamicTargets(context, client, camera, playerPos);
		renderBlockTargets(context, client, camera, playerPos);
	}

	private void refreshRareBlocks(MinecraftClient client) {
		if (client.world == null || client.player == null || !rareBlocks.getValue()) {
			cachedRareBlocks.clear();
			rareScanCooldown = 0;
			return;
		}

		BlockPos playerPos = client.player.getBlockPos();
		boolean movedEnough =
			Math.abs(playerPos.getX() - lastRareScanOrigin.getX()) >= 4 ||
			Math.abs(playerPos.getY() - lastRareScanOrigin.getY()) >= 4 ||
			Math.abs(playerPos.getZ() - lastRareScanOrigin.getZ()) >= 4;
		if (!cachedRareBlocks.isEmpty() && rareScanCooldown > 0 && !movedEnough) {
			rareScanCooldown--;
			return;
		}

		cachedRareBlocks.clear();
		lastRareScanOrigin = playerPos.toImmutable();
		rareScanCooldown = 8;

		int radius = (int) Math.round(rareRange.getValue());
		int minChunkX = (playerPos.getX() - radius) >> 4;
		int maxChunkX = (playerPos.getX() + radius) >> 4;
		int minChunkZ = (playerPos.getZ() - radius) >> 4;
		int maxChunkZ = (playerPos.getZ() + radius) >> 4;
		int minY = Math.max(client.world.getBottomY(), playerPos.getY() - radius);
		int maxY = Math.min(client.world.getTopYInclusive(), playerPos.getY() + radius);
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
			for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
				WorldChunk chunk = client.world.getChunkManager().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
				if (chunk == null) {
					continue;
				}

				int startX = Math.max(chunkX << 4, playerPos.getX() - radius);
				int endX = Math.min((chunkX << 4) + 15, playerPos.getX() + radius);
				int startZ = Math.max(chunkZ << 4, playerPos.getZ() - radius);
				int endZ = Math.min((chunkZ << 4) + 15, playerPos.getZ() + radius);

				for (int x = startX; x <= endX; x++) {
					for (int z = startZ; z <= endZ; z++) {
						for (int y = minY; y <= maxY; y++) {
							mutable.set(x, y, z);
							BlockState state = chunk.getBlockState(mutable);
							if (!isRareOre(state)) {
								continue;
							}

							Vec3d center = Vec3d.ofCenter(mutable);
							double distanceSq = center.squaredDistanceTo(client.player.getPos());
							if (distanceSq <= square(radius)) {
								cachedRareBlocks.add(new BlockCandidate(mutable.toImmutable(), distanceSq, rareColor.resolveColor(), blockTracers.getValue()));
							}
						}
					}
				}
			}
		}

		cachedRareBlocks.sort(Comparator.comparingDouble(BlockCandidate::distanceSq));
		if (cachedRareBlocks.size() > 256) {
			cachedRareBlocks.subList(256, cachedRareBlocks.size()).clear();
		}
	}

	private void renderDynamicTargets(WorldRenderContext context, MinecraftClient client, Vec3d camera, Vec3d playerPos) {
		double maxDistanceSq = square(targetRange.getValue());
		float tickDelta = context.tickCounter().getTickDelta(true);

		for (Entity entity : client.world.getEntities()) {
			VisualProfile profile = resolveDynamicProfile(entity, client, maxDistanceSq);
			if (profile == null) {
				continue;
			}

			Box box = getInterpolatedBox(entity, tickDelta).expand(profile.expand()).offset(-camera.x, -camera.y, -camera.z);
			renderBox(context, box, profile.color());
			if (profile.tracer()) {
				renderTracer(context, camera, playerPos, box.getCenter().add(camera), profile.color());
			}
		}
	}

	private void renderBlockTargets(WorldRenderContext context, MinecraftClient client, Vec3d camera, Vec3d playerPos) {
		List<BlockCandidate> matches = new ArrayList<>();
		collectBlockEntities(client, playerPos, matches);
		if (rareBlocks.getValue()) {
			matches.addAll(cachedRareBlocks);
		}

		matches.sort(Comparator.comparingDouble(BlockCandidate::distanceSq));
		int maxRendered = (int) Math.round(maxBlocks.getValue());
		for (int i = 0; i < matches.size() && i < maxRendered; i++) {
			BlockCandidate candidate = matches.get(i);
			Box box = new Box(candidate.pos()).expand(0.018D).offset(-camera.x, -camera.y, -camera.z);
			renderBox(context, box, candidate.color());
			if (candidate.tracer()) {
				renderTracer(context, camera, playerPos, Vec3d.ofCenter(candidate.pos()), candidate.color());
			}
		}
	}

	private void collectBlockEntities(MinecraftClient client, Vec3d playerPos, List<BlockCandidate> matches) {
		if (!storageBlocks.getValue() && !utilityBlocks.getValue() && !rareBlocks.getValue()) {
			return;
		}

		double maxDistanceSq = square(blockRange.getValue());
		int chunkRadius = Math.max(1, (int) Math.ceil(blockRange.getValue() / 16.0D));
		int centerChunkX = client.player.getBlockX() >> 4;
		int centerChunkZ = client.player.getBlockZ() >> 4;

		for (int chunkX = centerChunkX - chunkRadius; chunkX <= centerChunkX + chunkRadius; chunkX++) {
			for (int chunkZ = centerChunkZ - chunkRadius; chunkZ <= centerChunkZ + chunkRadius; chunkZ++) {
				WorldChunk chunk = client.world.getChunkManager().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
				if (chunk == null) {
					continue;
				}

				for (var entry : chunk.getBlockEntities().entrySet()) {
					BlockPos pos = entry.getKey();
					Vec3d center = Vec3d.ofCenter(pos);
					double distanceSq = center.squaredDistanceTo(playerPos);
					if (distanceSq > maxDistanceSq) {
						continue;
					}

					int color = resolveBlockEntityColor(entry.getValue());
					if (color == -1) {
						continue;
					}

					matches.add(new BlockCandidate(pos, distanceSq, color, blockTracers.getValue()));
				}
			}
		}
	}

	private VisualProfile resolveDynamicProfile(Entity entity, MinecraftClient client, double maxDistanceSq) {
		if (entity == client.player || entity == client.getCameraEntity() || entity.squaredDistanceTo(client.player) > maxDistanceSq) {
			return null;
		}
		if (!invisibleTargets.getValue() && entity.isInvisibleTo(client.player)) {
			return null;
		}

		if (entity instanceof PlayerEntity player) {
			if (!players.getValue() || !player.isAlive()) {
				return null;
			}
			return new VisualProfile(resolvePlayerColor(player), targetTracers.getValue(), 0.05D);
		}
		if (entity instanceof ItemEntity) {
			return items.getValue() ? new VisualProfile(itemColor.resolveColor(), targetTracers.getValue(), 0.07D) : null;
		}
		if (entity instanceof ProjectileEntity projectile) {
			if (!projectiles.getValue()) {
				return null;
			}
			if (!ownProjectiles.getValue() && projectile.getOwner() == client.player) {
				return null;
			}
			return new VisualProfile(projectileColor.resolveColor(), targetTracers.getValue(), 0.04D);
		}
		if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
			return null;
		}
		if (living instanceof HostileEntity) {
			return hostiles.getValue() ? new VisualProfile(hostileColor.resolveColor(), targetTracers.getValue(), 0.05D) : null;
		}
		if (isPassive(living)) {
			return passives.getValue() ? new VisualProfile(passiveColor.resolveColor(), targetTracers.getValue(), 0.05D) : null;
		}
		if (living instanceof MobEntity) {
			return neutrals.getValue() ? new VisualProfile(neutralColor.resolveColor(), targetTracers.getValue(), 0.05D) : null;
		}
		return null;
	}

	private boolean isPassive(LivingEntity entity) {
		return entity instanceof AnimalEntity
			|| entity instanceof WaterCreatureEntity
			|| entity instanceof AmbientEntity
			|| entity instanceof MerchantEntity
			|| entity instanceof VillagerEntity
			|| entity instanceof TameableEntity;
	}

	private int resolveBlockEntityColor(net.minecraft.block.entity.BlockEntity blockEntity) {
		if (storageBlocks.getValue() && isStorageBlock(blockEntity)) {
			return storageColor.resolveColor();
		}
		if (utilityBlocks.getValue() && isUtilityBlock(blockEntity)) {
			return utilityColor.resolveColor();
		}
		if (rareBlocks.getValue() && isRareSpecialBlock(blockEntity)) {
			return rareColor.resolveColor();
		}
		return -1;
	}

	private boolean isStorageBlock(net.minecraft.block.entity.BlockEntity blockEntity) {
		return blockEntity instanceof ChestBlockEntity
			|| blockEntity instanceof TrappedChestBlockEntity
			|| blockEntity instanceof EnderChestBlockEntity
			|| blockEntity instanceof ShulkerBoxBlockEntity
			|| blockEntity instanceof BarrelBlockEntity
			|| blockEntity instanceof HopperBlockEntity
			|| blockEntity instanceof DispenserBlockEntity
			|| blockEntity instanceof DropperBlockEntity
			|| blockEntity instanceof ChiseledBookshelfBlockEntity
			|| blockEntity instanceof DecoratedPotBlockEntity
			|| blockEntity instanceof BeehiveBlockEntity;
	}

	private boolean isUtilityBlock(net.minecraft.block.entity.BlockEntity blockEntity) {
		return blockEntity instanceof AbstractFurnaceBlockEntity
			|| blockEntity instanceof BrewingStandBlockEntity
			|| blockEntity instanceof CrafterBlockEntity
			|| blockEntity instanceof EnchantingTableBlockEntity
			|| blockEntity instanceof LecternBlockEntity
			|| blockEntity instanceof BeaconBlockEntity
			|| blockEntity instanceof CampfireBlockEntity
			|| blockEntity instanceof JukeboxBlockEntity
			|| blockEntity instanceof BellBlockEntity
			|| blockEntity instanceof ConduitBlockEntity;
	}

	private boolean isRareSpecialBlock(net.minecraft.block.entity.BlockEntity blockEntity) {
		return blockEntity instanceof MobSpawnerBlockEntity
			|| blockEntity instanceof TrialSpawnerBlockEntity
			|| blockEntity instanceof VaultBlockEntity
			|| blockEntity instanceof EndGatewayBlockEntity;
	}

	private boolean isRareOre(BlockState state) {
		return RARE_ORES.contains(state.getBlock());
	}

	private Box getInterpolatedBox(Entity entity, float tickDelta) {
		Vec3d currentPos = entity.getPos();
		Vec3d lerpedPos = entity.getLerpedPos(tickDelta);
		return entity.getBoundingBox().offset(lerpedPos.x - currentPos.x, lerpedPos.y - currentPos.y, lerpedPos.z - currentPos.z);
	}

	private int resolvePlayerColor(PlayerEntity player) {
		if (!trustColors.getValue()) {
			return playerColor.resolveColor();
		}

		TrustState state = TrustListManager.get().get(player.getUuid());
		return switch (state) {
			case SAFE -> safeColor.resolveColor();
			case ENEMY -> enemyColor.resolveColor();
			case SUSPICIOUS -> suspiciousColor.resolveColor();
		};
	}

	private void renderBox(WorldRenderContext context, Box box, int color) {
		float red = red(color);
		float green = green(color);
		float blue = blue(color);

		if (fill.getValue()) {
			float alpha = alpha(color) * (float) (fillStrength.getValue() / 100.0D);
			VertexRendering.drawFilledBox(
				context.matrixStack(),
				context.consumers().getBuffer(RenderLayer.getDebugFilledBox()),
				box.minX,
				box.minY,
				box.minZ,
				box.maxX,
				box.maxY,
				box.maxZ,
				red,
				green,
				blue,
				alpha
			);
		}

		if (outline.getValue()) {
			VertexRendering.drawBox(
				context.matrixStack(),
				context.consumers().getBuffer(RenderLayer.getLines()),
				box,
				red,
				green,
				blue,
				alpha(color)
			);
		}
	}

	private void renderTracer(WorldRenderContext context, Vec3d camera, Vec3d playerPos, Vec3d targetPos, int color) {
		Vec3d from = tracerOrigin.getValue().resolve(context, camera, playerPos);
		Vec3d to = targetPos.subtract(camera);
		Vec3d delta = to.subtract(from);
		if (delta.lengthSquared() < 1.0E-6D) {
			return;
		}

		Vec3d normal = delta.normalize();
		var entry = context.matrixStack().peek();
		var consumer = context.consumers().getBuffer(RenderLayer.getLines());
		float red = red(color);
		float green = green(color);
		float blue = blue(color);
		float alpha = alpha(color);

		consumer.vertex(entry.getPositionMatrix(), (float) from.x, (float) from.y, (float) from.z)
			.color(red, green, blue, alpha)
			.normal((float) normal.x, (float) normal.y, (float) normal.z);
		consumer.vertex(entry.getPositionMatrix(), (float) to.x, (float) to.y, (float) to.z)
			.color(red, green, blue, alpha)
			.normal((float) normal.x, (float) normal.y, (float) normal.z);
	}

	private double square(double value) {
		return value * value;
	}

	private float red(int color) {
		return ((color >> 16) & 255) / 255.0f;
	}

	private float green(int color) {
		return ((color >> 8) & 255) / 255.0f;
	}

	private float blue(int color) {
		return (color & 255) / 255.0f;
	}

	private float alpha(int color) {
		return ((color >> 24) & 255) / 255.0f;
	}

	private record VisualProfile(int color, boolean tracer, double expand) {
	}

	private record BlockCandidate(BlockPos pos, double distanceSq, int color, boolean tracer) {
	}

	private enum TracerOrigin {
		CURSOR("Cursor") {
			@Override
			Vec3d resolve(WorldRenderContext context, Vec3d camera, Vec3d playerPos) {
				return Vec3d.fromPolar(context.camera().getPitch(), context.camera().getYaw()).normalize().multiply(0.35D);
			}
		},
		CAMERA("Camera") {
			@Override
			Vec3d resolve(WorldRenderContext context, Vec3d camera, Vec3d playerPos) {
				return Vec3d.ZERO;
			}
		},
		FEET("Feet") {
			@Override
			Vec3d resolve(WorldRenderContext context, Vec3d camera, Vec3d playerPos) {
				return playerPos.subtract(camera);
			}
		};

		private final String label;

		TracerOrigin(String label) {
			this.label = label;
		}

		abstract Vec3d resolve(WorldRenderContext context, Vec3d camera, Vec3d playerPos);

		@Override
		public String toString() {
			return label;
		}
	}
}
