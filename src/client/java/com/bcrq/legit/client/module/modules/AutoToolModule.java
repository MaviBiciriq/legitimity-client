package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShearsItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;

public final class AutoToolModule extends Module {
	private final BooleanSetting switchForBlocks = addSetting(
			new BooleanSetting(this, "Blocks", "Switch to the best hotbar tool for the targeted block.", true));
	private final BooleanSetting switchForEntities = addSetting(
			new BooleanSetting(this, "Entities", "Switch to the strongest hotbar weapon before attacking entities.", true));
	private final BooleanSetting switchForUse = addSetting(
			new BooleanSetting(this, "Use Actions", "Switch tools for right-click tool actions when hands are not holding a block.", true));

	public AutoToolModule() {
		super("Auto Tool", "Selects the best hotbar tool for the block or entity under your crosshair.", ModuleCategory.UTILITY);
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.currentScreen != null) {
			return;
		}

		if (switchForEntities.getValue() && client.options.attackKey.isPressed() && client.crosshairTarget instanceof EntityHitResult entityHit) {
			selectWeapon(client, entityHit.getEntity());
			return;
		}

		if (switchForBlocks.getValue() && client.options.attackKey.isPressed() && client.crosshairTarget instanceof BlockHitResult blockHit) {
			selectBlockTool(client, blockHit, true);
			return;
		}

		if (switchForUse.getValue()
				&& client.options.useKey.isPressed()
				&& client.crosshairTarget instanceof BlockHitResult blockHit
				&& !(client.player.getMainHandStack().getItem() instanceof BlockItem)) {
			selectBlockTool(client, blockHit, false);
		}
	}

	private void selectBlockTool(MinecraftClient client, BlockHitResult hit, boolean mining) {
		BlockState state = client.world.getBlockState(hit.getBlockPos());
		ToolKind preferred = preferredTool(state, mining);
		if (preferred == ToolKind.HAND) {
			return;
		}

		int bestSlot = -1;
		double bestScore = score(client.player.getInventory().getMainHandStack(), state, preferred);
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = client.player.getInventory().getStack(slot);
			double score = score(stack, state, preferred);
			if (score > bestScore + 0.001D) {
				bestScore = score;
				bestSlot = slot;
			}
		}

		if (bestSlot >= 0) {
			client.player.getInventory().selectedSlot = bestSlot;
		}
	}

	private ToolKind preferredTool(BlockState state, boolean mining) {
		if (state.isOf(Blocks.COBWEB) || state.isIn(BlockTags.WOOL)) {
			return ToolKind.SHEARS;
		}
		if (state.isOf(Blocks.BAMBOO) || state.isOf(Blocks.BAMBOO_SAPLING)) {
			return ToolKind.SWORD;
		}
		if (state.isIn(BlockTags.PICKAXE_MINEABLE)) {
			return ToolKind.PICKAXE;
		}
		if (state.isIn(BlockTags.AXE_MINEABLE)) {
			return ToolKind.AXE;
		}
		if (state.isIn(BlockTags.SHOVEL_MINEABLE)) {
			return ToolKind.SHOVEL;
		}
		if (state.isIn(BlockTags.HOE_MINEABLE)) {
			return ToolKind.HOE;
		}
		if (!mining && state.isOf(Blocks.GRASS_BLOCK)) {
			return ToolKind.SHOVEL;
		}
		return ToolKind.HAND;
	}

	private double score(ItemStack stack, BlockState state, ToolKind preferred) {
		if (stack.isEmpty()) {
			return 0.0D;
		}

		double speed = stack.getMiningSpeedMultiplier(state);
		double score = speed;
		if (matches(stack.getItem(), preferred)) {
			score += 100.0D;
		}
		if (stack.isSuitableFor(state)) {
			score += 10.0D;
		}
		if (stack.isDamageable()) {
			double remaining = (stack.getMaxDamage() - stack.getDamage()) / (double) stack.getMaxDamage();
			score += Math.max(0.0D, remaining);
		}
		return score;
	}

	private boolean matches(Item item, ToolKind kind) {
		return switch (kind) {
			case PICKAXE -> item instanceof PickaxeItem;
			case AXE -> item instanceof AxeItem;
			case SHOVEL -> item instanceof ShovelItem;
			case HOE -> item instanceof HoeItem;
			case SHEARS -> item instanceof ShearsItem;
			case SWORD -> item instanceof SwordItem;
			case HAND -> false;
		};
	}

	private void selectWeapon(MinecraftClient client, Entity target) {
		int bestSlot = -1;
		double bestScore = weaponScore(client.player.getInventory().getMainHandStack());
		for (int slot = 0; slot < 9; slot++) {
			ItemStack stack = client.player.getInventory().getStack(slot);
			double score = weaponScore(stack);
			if (score > bestScore + 0.001D) {
				bestScore = score;
				bestSlot = slot;
			}
		}

		if (target != null && bestSlot >= 0) {
			client.player.getInventory().selectedSlot = bestSlot;
		}
	}

	private double weaponScore(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0.0D;
		}

		Item item = stack.getItem();
		double score = 0.0D;
		if (item == Items.NETHERITE_SWORD) score = 8.0D;
		else if (item == Items.DIAMOND_SWORD) score = 7.0D;
		else if (item == Items.IRON_SWORD) score = 6.0D;
		else if (item == Items.STONE_SWORD) score = 5.0D;
		else if (item == Items.WOODEN_SWORD || item == Items.GOLDEN_SWORD) score = 4.0D;
		else if (item == Items.NETHERITE_AXE) score = 10.0D;
		else if (item == Items.DIAMOND_AXE || item == Items.IRON_AXE || item == Items.STONE_AXE) score = 9.0D;
		else if (item == Items.WOODEN_AXE || item == Items.GOLDEN_AXE) score = 7.0D;
		else if (item instanceof PickaxeItem || item instanceof ShovelItem || item instanceof HoeItem) score = 2.0D;

		if (stack.isDamageable() && score > 0.0D) {
			double remaining = (stack.getMaxDamage() - stack.getDamage()) / (double) stack.getMaxDamage();
			score += Math.max(0.0D, remaining);
		}
		return score;
	}

	private enum ToolKind {
		PICKAXE,
		AXE,
		SHOVEL,
		HOE,
		SHEARS,
		SWORD,
		HAND
	}
}
