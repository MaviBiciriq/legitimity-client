package com.bcrq.legit.client.module.modules;

import com.bcrq.legit.client.module.Module;
import com.bcrq.legit.client.module.ModuleCategory;
import com.bcrq.legit.client.setting.BooleanSetting;
import com.bcrq.legit.client.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public final class AutoExpModule extends Module {
	private final NumberSetting repairRatio = addSetting(
			new NumberSetting(this, "Repair Below", "Throw XP when durability remaining is below this ratio.", 0.875D, 0.10D, 0.99D, 0.005D));
	private final NumberSetting useDelay = addSetting(
			new NumberSetting(this, "Use Delay", "Ticks between XP bottle throws.", 2.0D, 0.0D, 10.0D, 1.0D));
	private final BooleanSetting includeInventory = addSetting(
			new BooleanSetting(this, "Inventory Items", "Check all inventory items, not only armor and hands.", true));
	private final BooleanSetting restoreSlot = addSetting(
			new BooleanSetting(this, "Restore Slot", "Return to the previous hotbar slot when repairing stops.", true));

	private int cooldown;
	private int originalSlot = -1;
	private int swappedScreenSlot = -1;
	private float originalPitch;
	private boolean lookingDown;

	public AutoExpModule() {
		super("Auto EXP", "Throws experience bottles downward when gear durability drops below the configured threshold.", ModuleCategory.UTILITY);
	}

	@Override
	public void onDisable() {
		restore(MinecraftClient.getInstance());
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.interactionManager == null || client.currentScreen != null) {
			return;
		}

		ClientPlayerEntity player = client.player;
		if (!needsRepair(player)) {
			restore(client);
			return;
		}

		if (originalSlot < 0) {
			originalSlot = player.getInventory().selectedSlot;
		}
		if (!lookingDown) {
			originalPitch = player.getPitch();
			lookingDown = true;
		}

		if (!ensureXpSelected(client, player)) {
			restore(client);
			return;
		}

		player.setPitch(90.0f);
		if (cooldown > 0) {
			cooldown--;
			return;
		}

		client.interactionManager.interactItem(player, Hand.MAIN_HAND);
		player.swingHand(Hand.MAIN_HAND);
		cooldown = useDelay.getValue().intValue();
	}

	private boolean needsRepair(ClientPlayerEntity player) {
		double threshold = repairRatio.getValue();
		for (ItemStack stack : player.getInventory().armor) {
			if (belowThreshold(stack, threshold)) {
				return true;
			}
		}
		for (ItemStack stack : player.getInventory().offHand) {
			if (belowThreshold(stack, threshold)) {
				return true;
			}
		}
		if (belowThreshold(player.getMainHandStack(), threshold)) {
			return true;
		}
		if (!includeInventory.getValue()) {
			return false;
		}
		for (ItemStack stack : player.getInventory().main) {
			if (belowThreshold(stack, threshold)) {
				return true;
			}
		}
		return false;
	}

	private boolean belowThreshold(ItemStack stack, double threshold) {
		if (stack.isEmpty() || !stack.isDamageable() || stack.getMaxDamage() <= 0) {
			return false;
		}
		double remaining = (stack.getMaxDamage() - stack.getDamage()) / (double) stack.getMaxDamage();
		return remaining < threshold;
	}

	private boolean ensureXpSelected(MinecraftClient client, ClientPlayerEntity player) {
		if (player.getMainHandStack().isOf(Items.EXPERIENCE_BOTTLE)) {
			return true;
		}

		for (int slot = 0; slot < 9; slot++) {
			if (player.getInventory().getStack(slot).isOf(Items.EXPERIENCE_BOTTLE)) {
				player.getInventory().selectedSlot = slot;
				return true;
			}
		}

		for (int slot = 9; slot < player.getInventory().main.size(); slot++) {
			if (player.getInventory().getStack(slot).isOf(Items.EXPERIENCE_BOTTLE)) {
				swappedScreenSlot = inventoryToScreenSlot(slot);
				client.interactionManager.clickSlot(
						player.currentScreenHandler.syncId,
						swappedScreenSlot,
						player.getInventory().selectedSlot,
						SlotActionType.SWAP,
						player
				);
				return player.getMainHandStack().isOf(Items.EXPERIENCE_BOTTLE);
			}
		}

		return false;
	}

	private void restore(MinecraftClient client) {
		if (client.player == null || client.interactionManager == null) {
			resetState();
			return;
		}

		ClientPlayerEntity player = client.player;
		if (lookingDown) {
			player.setPitch(originalPitch);
		}

		if (restoreSlot.getValue() && originalSlot >= 0) {
			if (swappedScreenSlot >= 0) {
				client.interactionManager.clickSlot(
						player.currentScreenHandler.syncId,
						swappedScreenSlot,
						originalSlot,
						SlotActionType.SWAP,
						player
				);
			}
			player.getInventory().selectedSlot = originalSlot;
		}

		resetState();
	}

	private void resetState() {
		cooldown = 0;
		originalSlot = -1;
		swappedScreenSlot = -1;
		lookingDown = false;
		originalPitch = 0.0f;
	}

	private int inventoryToScreenSlot(int inventorySlot) {
		return inventorySlot < 9 ? 36 + inventorySlot : inventorySlot;
	}
}
