package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.ColorSetting;
import com.bcrq.legit.client.setting.ColorValue;
import com.bcrq.legit.client.setting.NumberSetting;
import com.bcrq.legit.client.trust.TrustListManager;
import com.bcrq.legit.client.trust.TrustState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Chams — true 2D cel-shaded outlines around entity models, visible
 * through walls. Uses the vanilla glowing/spectral effect pipeline
 * via {@code EntityGlowMixin} hooks on {@code isGlowing()} and
 * {@code getTeamColorValue()}.
 * <p>
 * The outline follows the exact entity model silhouette (not a box),
 * and is always drawn on top of all geometry — cartoon-style.
 */
public final class ChamsModule extends Module {

	// ── Target toggles ───────────────────────────────────────────────────────
	private final BooleanSetting renderPlayers = addSetting(
			new BooleanSetting(this, "Players", "Show player outlines through walls.", true));
	private final BooleanSetting renderHostiles = addSetting(
			new BooleanSetting(this, "Hostiles", "Show hostile mob outlines.", true));
	private final BooleanSetting renderPassives = addSetting(
			new BooleanSetting(this, "Passives", "Show passive mob outlines.", false));
	private final BooleanSetting renderItems = addSetting(
			new BooleanSetting(this, "Items", "Show dropped item outlines.", false));
	private final BooleanSetting renderInvisible = addSetting(
			new BooleanSetting(this, "Invisibles", "Show invisible entities.", false));
	private final BooleanSetting useTrustColors = addSetting(
			new BooleanSetting(this, "Trust Colors", "Use trust-list palette for player outlines.", true));

	// ── Range ────────────────────────────────────────────────────────────────
	private final NumberSetting range = addSetting(
			new NumberSetting(this, "Range", "Max render distance.", 64.0D, 16.0D, 128.0D, 4.0D));

	// ── Colors ───────────────────────────────────────────────────────────────
	private final ColorSetting playerColor = addSetting(new ColorSetting(this, "Player Color", "Default player outline.", new ColorValue(85, 195, 255, 255, false, 0.14D)));
	private final ColorSetting safeColor = addSetting(new ColorSetting(this, "Safe Color", "Trusted player outline.", new ColorValue(92, 255, 135, 255, false, 0.14D)));
	private final ColorSetting enemyColor = addSetting(new ColorSetting(this, "Enemy Color", "Enemy player outline.", new ColorValue(255, 88, 88, 255, false, 0.14D)));
	private final ColorSetting suspiciousColor = addSetting(new ColorSetting(this, "Suspicious Color", "Untagged player outline.", new ColorValue(255, 205, 90, 255, false, 0.14D)));
	private final ColorSetting hostileColor = addSetting(new ColorSetting(this, "Hostile Color", "Hostile mob outline.", new ColorValue(255, 110, 120, 255, false, 0.14D)));
	private final ColorSetting passiveColor = addSetting(new ColorSetting(this, "Passive Color", "Passive mob outline.", new ColorValue(125, 255, 172, 255, false, 0.14D)));
	private final ColorSetting itemColor = addSetting(new ColorSetting(this, "Item Color", "Dropped item outline.", new ColorValue(255, 244, 130, 255, false, 0.14D)));

	public ChamsModule() {
		super("Chams", "2D outline around entity models through walls (cel-shading style).", ModuleCategory.RENDER);
	}

	// ── API for EntityGlowMixin ──────────────────────────────────────────────

	/**
	 * Should this entity have the glowing outline?
	 * Called from {@code EntityGlowMixin.isGlowing()} every frame.
	 */
	public boolean shouldGlow(Entity entity) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return false;
		if (entity == client.player || entity == client.getCameraEntity()) return false;
		if (entity.squaredDistanceTo(client.player) > range.getValue() * range.getValue()) return false;
		if (!renderInvisible.getValue() && entity.isInvisibleTo(client.player)) return false;

		return resolveColor(entity) != -1;
	}

	/**
	 * Returns the RGB outline color for this entity (no alpha), or -1 if
	 * chams shouldn't apply. Called from {@code EntityGlowMixin.getTeamColorValue()}.
	 */
	public int getOutlineColor(Entity entity) {
		int argb = resolveColor(entity);
		if (argb == -1) return -1;
		// Strip alpha — getTeamColorValue returns 0xRRGGBB
		return argb & 0x00FFFFFF;
	}

	// ── Color resolution ─────────────────────────────────────────────────────

	private int resolveColor(Entity entity) {
		if (entity instanceof PlayerEntity player) {
			if (!renderPlayers.getValue() || !player.isAlive()) return -1;
			if (useTrustColors.getValue()) {
				TrustState state = TrustListManager.get().get(player.getUuid());
				return switch (state) {
					case SAFE -> safeColor.resolveColor();
					case ENEMY -> enemyColor.resolveColor();
					case SUSPICIOUS -> suspiciousColor.resolveColor();
				};
			}
			return playerColor.resolveColor();
		}
		if (entity instanceof HostileEntity h) {
			return (renderHostiles.getValue() && h.isAlive()) ? hostileColor.resolveColor() : -1;
		}
		if (entity instanceof LivingEntity living) {
			if (!living.isAlive()) return -1;
			if (living instanceof AnimalEntity || living instanceof MerchantEntity || living instanceof VillagerEntity) {
				return renderPassives.getValue() ? passiveColor.resolveColor() : -1;
			}
			return -1;
		}
		if (entity instanceof ItemEntity) {
			return renderItems.getValue() ? itemColor.resolveColor() : -1;
		}
		return -1;
	}
}
