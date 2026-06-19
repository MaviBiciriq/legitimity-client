package com.bcrq.legit.client.gui;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.util.UiRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public final class PauseMenuOverlay {
	private PauseMenuOverlay() {
	}

	public static void render(Screen screen, DrawContext context, int mouseX, int mouseY) {
		int width = 110;
		int height = 24;
		int x = screen.width - width - 18;
		int y = 16;
		boolean hovered = UiRenderUtil.isHovered(mouseX, mouseY, x, y, width, height);
		int background = hovered ? 0xB8222833 : UiRenderUtil.PANEL;
		int border = hovered ? UiRenderUtil.ACCENT : UiRenderUtil.OUTLINE;

		UiRenderUtil.drawPanel(context, x, y, width, height, background, border);
		UiRenderUtil.drawAccentStrip(context, x, y, width, UiRenderUtil.ACCENT);
		context.drawText(screen.getTextRenderer(), "Legitimity", x + 12, y + 9, UiRenderUtil.TEXT, false);
	}

	public static boolean click(Screen screen, double mouseX, double mouseY, int button) {
		if (button != 0) {
			return false;
		}

		int width = 110;
		int height = 24;
		int x = screen.width - width - 18;
		int y = 16;

		if (UiRenderUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
			LegitimityClient.get().openClickGui(MinecraftClient.getInstance());
			return true;
		}

		return false;
	}
}
