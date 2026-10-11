package com.depremmod.client;

import com.depremmod.net.DepremStartPayload;

import java.util.ArrayList;
import java.util.List;

/** Telefon uygulamasinda gosterilen son depremler (oyun acik kaldigi surece). */
public final class QuakeHistory {
	public record Entry(DepremStartPayload data, long timeMillis) {}

	private static final int MAX = 8;
	private static final List<Entry> ENTRIES = new ArrayList<>();

	private QuakeHistory() {}

	public static void add(DepremStartPayload payload) {
		ENTRIES.add(0, new Entry(payload, System.currentTimeMillis()));
		while (ENTRIES.size() > MAX) {
			ENTRIES.remove(ENTRIES.size() - 1);
		}
	}

	public static List<Entry> all() {
		return ENTRIES;
	}
}
