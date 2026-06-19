package com.bcrq.legit.client.entity;

import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;

public class LegitCameraEntity extends OtherClientPlayerEntity {
	public LegitCameraEntity(ClientWorld world, String name) {
		super(world, new GameProfile(UUID.nameUUIDFromBytes(("legitimity:" + name).getBytes(StandardCharsets.UTF_8)), name));
	}

	@Override
	public SkinTextures getSkinTextures() {
		MinecraftClient client = MinecraftClient.getInstance();
		GameProfile profile = getGameProfile();
		try {
			if (client != null) {
				return client.getSkinProvider().getSkinTextures(profile);
			}
		} catch (Exception ignored) {
		}
		return DefaultSkinHelper.getSkinTextures(profile);
	}
}
