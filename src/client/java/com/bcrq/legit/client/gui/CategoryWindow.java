package com.bcrq.legit.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class CategoryWindow {
	public static final int WIDTH = 150;
	public static final int TITLE_HEIGHT = 18;
	public static final int ROW_HEIGHT = 18;
	private static final int DEFAULT_MAX_VISIBLE_HEIGHT = 200;
	private static final int TITLE_BG = 0xE024151C;
	private static final int BODY_BG = 0xD0141016;
	private static final int BORDER = 0xFF6A2B3A;
	private static final int ROW_HOVER = 0x30FF4D6D;
	private static final int ENABLED = 0xFFFF4D6D;
	private static final int TEXT = 0xFFDDDDDD;
	private static final int TEXT_DIM = 0xFF888888;

	public final String category;
	private final List<IModule> modules;
	private final List<String> expanded = new ArrayList<>();
	public double x;
	public double y;
	public boolean collapsed;
	private double scroll;
	private int maxVisibleHeight = DEFAULT_MAX_VISIBLE_HEIGHT;
	private String search = "";
	private boolean draggingTitle;
	private ModuleSetting<?> draggingSlider;

	public CategoryWindow(String category, List<IModule> modules, double x, double y, boolean collapsed) {
		this.category = category;
		this.modules = modules;
		this.x = x;
		this.y = y;
		this.collapsed = collapsed;
	}

	public void setSearch(String search) {
		this.search = search == null ? "" : search.strip().toLowerCase(Locale.ROOT);
	}

	public boolean hasVisibleModules() {
		return !visibleModules().isEmpty();
	}

	private List<IModule> visibleModules() {
		if (search.isEmpty() || category.toLowerCase(Locale.ROOT).contains(search)) return modules;
		List<IModule> result = new ArrayList<>();
		for (IModule module : modules) {
			if (module.getName().toLowerCase(Locale.ROOT).contains(search)
					|| module.getDescription().toLowerCase(Locale.ROOT).contains(search)) result.add(module);
		}
		return result;
	}

	private int contentHeight() {
		int height = 0;
		for (IModule module : visibleModules()) {
			height += ROW_HEIGHT;
			if (expanded.contains(module.getName())) height += module.getSettings().size() * ROW_HEIGHT;
		}
		return height;
	}

	private int visibleHeight() {
		return collapsed ? 0 : Math.min(contentHeight(), maxVisibleHeight);
	}

	public int totalHeight() {
		return TITLE_HEIGHT + visibleHeight();
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + totalHeight();
	}

	public void setMaxVisibleHeight(int maxVisibleHeight) {
		this.maxVisibleHeight = Math.max(ROW_HEIGHT, maxVisibleHeight);
		scroll = Math.clamp(scroll, 0, Math.max(0, contentHeight() - this.maxVisibleHeight));
	}

	public void clampToScreen(int screenWidth, int screenHeight, int topInset, int margin) {
		x = Math.clamp(x, margin, Math.max(margin, screenWidth - WIDTH - margin));
		y = Math.clamp(y, topInset, Math.max(topInset, screenHeight - totalHeight() - margin));
	}

	public void render(DrawContext context, int mouseX, int mouseY) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int left = (int) x;
		int top = (int) y;
		context.fill(left, top, left + WIDTH, top + TITLE_HEIGHT, TITLE_BG);
		context.drawBorder(left, top, WIDTH, TITLE_HEIGHT, BORDER);
		context.drawText(textRenderer, category, left + 5, top + 5, TEXT, false);
		context.drawText(textRenderer, collapsed ? "+" : "-", left + WIDTH - 10, top + 5, TEXT, false);
		if (collapsed) return;

		int bodyHeight = visibleHeight();
		context.fill(left, top + TITLE_HEIGHT, left + WIDTH, top + TITLE_HEIGHT + bodyHeight, BODY_BG);
		context.drawBorder(left, top + TITLE_HEIGHT, WIDTH, bodyHeight, BORDER);
		context.enableScissor(left, top + TITLE_HEIGHT, left + WIDTH, top + TITLE_HEIGHT + bodyHeight);

		int rowY = (int) (y + TITLE_HEIGHT - scroll);
		for (IModule module : visibleModules()) {
			if (inView(rowY, bodyHeight)) renderModule(context, textRenderer, module, rowY, mouseX, mouseY);
			rowY += ROW_HEIGHT;
			if (expanded.contains(module.getName())) {
				for (ModuleSetting<?> setting : module.getSettings()) {
					if (inView(rowY, bodyHeight)) renderSetting(context, textRenderer, setting, rowY);
					rowY += ROW_HEIGHT;
				}
			}
		}
		context.disableScissor();
	}

	private boolean inView(int rowY, int bodyHeight) {
		return rowY + ROW_HEIGHT >= y + TITLE_HEIGHT && rowY <= y + TITLE_HEIGHT + bodyHeight;
	}

	private void renderModule(DrawContext context, TextRenderer textRenderer, IModule module, int rowY, int mouseX, int mouseY) {
		if (mouseX >= x && mouseX <= x + WIDTH && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
			context.fill((int) x, rowY, (int) x + WIDTH, rowY + ROW_HEIGHT, ROW_HOVER);
		}
		context.drawText(textRenderer, ellipsize(textRenderer, module.getName(), WIDTH - 22), (int) x + 5, rowY + 5, module.isEnabled() ? ENABLED : TEXT, false);
		if (!module.getSettings().isEmpty()) {
			context.drawText(textRenderer, expanded.contains(module.getName()) ? "v" : ">", (int) x + WIDTH - 10, rowY + 5, TEXT_DIM, false);
		}
	}

	private void renderSetting(DrawContext context, TextRenderer textRenderer, ModuleSetting<?> setting, int rowY) {
		context.drawText(textRenderer, ellipsize(textRenderer, setting.getName(), 58), (int) x + 10, rowY + 5, TEXT_DIM, false);
		if (setting.getType() == ModuleSetting.Type.BOOLEAN) {
			boolean enabled = (Boolean) setting.getValue();
			int boxX = (int) x + WIDTH - 16;
			context.fill(boxX, rowY + 5, boxX + 9, rowY + 13, enabled ? ENABLED : 0xFF555555);
		} else {
			double range = setting.getMax() - setting.getMin();
			double percentage = range == 0 ? 0 : Math.clamp(((Double) setting.getValue() - setting.getMin()) / range, 0, 1);
			int trackX = (int) x + 76;
			int trackWidth = WIDTH - 80;
			context.drawText(textRenderer, String.format(Locale.ROOT, "%.2f", (Double) setting.getValue()), trackX, rowY + 2, TEXT_DIM, false);
			context.fill(trackX, rowY + 14, trackX + trackWidth, rowY + 16, 0xFF555555);
			int handleX = (int) (trackX + percentage * trackWidth);
			context.fill(handleX - 1, rowY + 12, handleX + 2, rowY + 17, ENABLED);
		}
	}

	public boolean handleClick(double mouseX, double mouseY, int button) {
		if (!isMouseOver(mouseX, mouseY)) return false;
		if (mouseY <= y + TITLE_HEIGHT) {
			if (button == 0 && mouseX >= x + WIDTH - 14) collapsed = !collapsed;
			else if (button == 0) draggingTitle = true;
			return true;
		}
		if (collapsed) return true;
		int rowY = (int) (y + TITLE_HEIGHT - scroll);
		for (IModule module : visibleModules()) {
			if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
				if (button == 1 && !module.getSettings().isEmpty()) toggleExpanded(module.getName());
				else if (button == 0) module.toggle();
				return true;
			}
			rowY += ROW_HEIGHT;
			if (expanded.contains(module.getName())) {
				for (ModuleSetting<?> setting : module.getSettings()) {
					if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
						if (button == 0) {
							applySettingClick(setting, mouseX);
							if (setting.getType() == ModuleSetting.Type.DOUBLE) draggingSlider = setting;
						}
						return true;
					}
					rowY += ROW_HEIGHT;
				}
			}
		}
		return true;
	}

	private void applySettingClick(ModuleSetting<?> setting, double mouseX) {
		if (setting.getType() == ModuleSetting.Type.BOOLEAN) setting.setValue(!(Boolean) setting.getValue());
		else setSliderFromMouse(setting, mouseX);
	}

	private void setSliderFromMouse(ModuleSetting<?> setting, double mouseX) {
		int trackX = (int) x + 76;
		int trackWidth = WIDTH - 80;
		double percentage = Math.clamp((mouseX - trackX) / trackWidth, 0, 1);
		setting.setValue(setting.getMin() + percentage * (setting.getMax() - setting.getMin()));
	}

	private void toggleExpanded(String name) {
		if (!expanded.remove(name)) expanded.add(name);
	}

	public void handleDrag(double mouseX, double deltaX, double deltaY) {
		if (draggingTitle) {
			x += deltaX;
			y += deltaY;
		}
		if (draggingSlider != null) setSliderFromMouse(draggingSlider, mouseX);
	}

	public void handleRelease() {
		draggingTitle = false;
		draggingSlider = null;
	}

	public boolean handleScroll(double mouseX, double mouseY, double amount) {
		if (!isMouseOver(mouseX, mouseY) || collapsed) return false;
		scroll = Math.clamp(scroll - amount * ROW_HEIGHT, 0, Math.max(0, contentHeight() - maxVisibleHeight));
		return true;
	}

	public GuiLayoutConfig.WindowState toState() {
		return new GuiLayoutConfig.WindowState(x, y, collapsed);
	}

	private String ellipsize(TextRenderer textRenderer, String text, int maxWidth) {
		if (textRenderer.getWidth(text) <= maxWidth) return text;
		return textRenderer.trimToWidth(text, Math.max(0, maxWidth - textRenderer.getWidth("..."))) + "...";
	}
}
