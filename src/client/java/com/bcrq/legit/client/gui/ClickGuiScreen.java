package com.bcrq.legit.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Floating, independently draggable category windows for the module list. */
public final class ClickGuiScreen extends Screen {
	private static final int SEARCH_X = 10;
	private static final int SEARCH_Y = 10;
	private static final int SEARCH_WIDTH = 200;
	private static final int SEARCH_HEIGHT = 18;
	private static final int WINDOW_GAP = 10;
	private static final int WINDOW_TOP = 36;
	private static final int WINDOW_MARGIN = 8;
	private final List<CategoryWindow> windows = new ArrayList<>();
	private String search = "";
	private boolean searchFocused;

	public ClickGuiScreen(List<IModule> allModules) {
		super(Text.literal("Legitimity"));
		Map<String, GuiLayoutConfig.WindowState> saved = GuiLayoutConfig.load();
		Map<String, List<IModule>> byCategory = new LinkedHashMap<>();
		for (IModule module : allModules) byCategory.computeIfAbsent(module.getCategory(), ignored -> new ArrayList<>()).add(module);
		for (Map.Entry<String, List<IModule>> entry : byCategory.entrySet()) {
			GuiLayoutConfig.WindowState state = saved.get(entry.getKey());
			windows.add(new CategoryWindow(entry.getKey(), entry.getValue(), state == null ? Double.NaN : state.x,
				state == null ? Double.NaN : state.y, state != null && state.collapsed));
		}
	}

	@Override
	protected void init() {
		layoutWindows();
	}

	private void layoutWindows() {
		int columns = Math.max(1, (width - WINDOW_MARGIN * 2 + WINDOW_GAP) / (CategoryWindow.WIDTH + WINDOW_GAP));
		int rows = Math.max(1, (int) Math.ceil(windows.size() / (double) columns));
		int availableHeight = Math.max(CategoryWindow.ROW_HEIGHT, height - WINDOW_TOP - WINDOW_MARGIN);
		int cellHeight = Math.max(CategoryWindow.TITLE_HEIGHT + CategoryWindow.ROW_HEIGHT, (availableHeight - WINDOW_GAP * (rows - 1)) / rows);
		for (int index = 0; index < windows.size(); index++) {
			CategoryWindow window = windows.get(index);
			window.setMaxVisibleHeight(cellHeight - CategoryWindow.TITLE_HEIGHT);
			if (Double.isNaN(window.x) || Double.isNaN(window.y)) {
				int column = index % columns;
				int row = index / columns;
				window.x = WINDOW_MARGIN + column * (CategoryWindow.WIDTH + WINDOW_GAP);
				window.y = WINDOW_TOP + row * (cellHeight + WINDOW_GAP);
			}
			window.clampToScreen(width, height, WINDOW_TOP, WINDOW_MARGIN);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, 0x80000000);
		int searchWidth = Math.min(SEARCH_WIDTH, width - SEARCH_X * 2);
		context.fill(SEARCH_X, SEARCH_Y, SEARCH_X + searchWidth, SEARCH_Y + SEARCH_HEIGHT, 0xE024151C);
		context.drawBorder(SEARCH_X, SEARCH_Y, searchWidth, SEARCH_HEIGHT, searchFocused ? 0xFFFF4D6D : 0xFF6A2B3A);
		String label = search.isEmpty() ? "Search modules or categories..." : search;
		context.drawText(textRenderer, label, SEARCH_X + 5, SEARCH_Y + 5, search.isEmpty() ? 0xFF777777 : 0xFFFFFFFF, false);
		for (CategoryWindow window : windows) {
			window.setSearch(search);
			if (search.isEmpty() || window.hasVisibleModules()) window.render(context, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int searchWidth = Math.min(SEARCH_WIDTH, width - SEARCH_X * 2);
		if (mouseX >= SEARCH_X && mouseX <= SEARCH_X + searchWidth && mouseY >= SEARCH_Y && mouseY <= SEARCH_Y + SEARCH_HEIGHT) {
			searchFocused = true;
			return true;
		}
		searchFocused = false;
		for (int i = windows.size() - 1; i >= 0; i--) {
			CategoryWindow window = windows.get(i);
			if (!search.isEmpty() && !window.hasVisibleModules()) continue;
			if (window.handleClick(mouseX, mouseY, button)) {
				windows.remove(i);
				windows.add(window);
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		for (CategoryWindow window : windows) {
			window.handleDrag(mouseX, deltaX, deltaY);
			window.clampToScreen(width, height, WINDOW_TOP, WINDOW_MARGIN);
		}
		return true;
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		for (CategoryWindow window : windows) window.handleRelease();
		return true;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		for (int i = windows.size() - 1; i >= 0; i--) if (windows.get(i).handleScroll(mouseX, mouseY, verticalAmount)) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (!searchFocused || Character.isISOControl(chr)) return super.charTyped(chr, modifiers);
		search += chr;
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (searchFocused && keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
			search = search.substring(0, search.length() - 1);
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void removed() {
		Map<String, GuiLayoutConfig.WindowState> layout = new HashMap<>();
		for (CategoryWindow window : windows) layout.put(window.category, window.toState());
		GuiLayoutConfig.save(layout);
		super.removed();
	}
}
