package com.bcrq.legit.client;

import java.util.ArrayDeque;
import java.util.Deque;

public final class InputMetrics {
	private static final long WINDOW_MS = 1000L;
	private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
	private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();

	private InputMetrics() {
	}

	public static void recordClick(int button) {
		long now = System.currentTimeMillis();

		if (button == 0) {
			LEFT_CLICKS.addLast(now);
		} else if (button == 1) {
			RIGHT_CLICKS.addLast(now);
		}

		trim();
	}

	public static int getLeftCps() {
		trim();
		return LEFT_CLICKS.size();
	}

	public static int getRightCps() {
		trim();
		return RIGHT_CLICKS.size();
	}

	public static void trim() {
		long threshold = System.currentTimeMillis() - WINDOW_MS;
		while (!LEFT_CLICKS.isEmpty() && LEFT_CLICKS.peekFirst() < threshold) {
			LEFT_CLICKS.removeFirst();
		}
		while (!RIGHT_CLICKS.isEmpty() && RIGHT_CLICKS.peekFirst() < threshold) {
			RIGHT_CLICKS.removeFirst();
		}
	}
}
