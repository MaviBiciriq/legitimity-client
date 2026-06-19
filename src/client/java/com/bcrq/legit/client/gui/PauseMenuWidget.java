package com.bcrq.legit.client.gui;

import com.bcrq.legit.client.LegitimityClient;
import com.bcrq.legit.client.util.UiRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;

public final class PauseMenuWidget extends PressableWidget {
	public PauseMenuWidget(int x, int y, int width, int height) {
		super(x, y, width, height, Text.literal("Legitimity"));
	}

	@Override
	public void onPress() {
		LegitimityClient.get().openClickGui(MinecraftClient.getInstance());
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		boolean hovered = isHovered();
		int background = hovered ? 0xB8222833 : UiRenderUtil.PANEL;
		int border = hovered ? UiRenderUtil.ACCENT : UiRenderUtil.OUTLINE;

		UiRenderUtil.drawPanel(context, getX(), getY(), width, height, background, border);
		UiRenderUtil.drawAccentStrip(context, getX(), getY(), width, UiRenderUtil.ACCENT);
		context.drawText(MinecraftClient.getInstance().textRenderer, getMessage(), getX() + 12, getY() + 9, UiRenderUtil.TEXT, false);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		appendDefaultNarrations(builder);
	}
}
