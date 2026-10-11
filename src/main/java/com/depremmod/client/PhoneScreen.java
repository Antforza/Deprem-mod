package com.depremmod.client;

import com.depremmod.EarthquakeManager;
import com.depremmod.QuakeMath;
import com.depremmod.net.DepremStartPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Telefondaki "Deprem Takip" uygulamasi. */
public class PhoneScreen extends Screen {
	private static final int PANEL_W = 236;

	private int panelH;
	private int left;
	private int top;

	public PhoneScreen() {
		super(Component.literal("Deprem Takip"));
	}

	@Override
	protected void init() {
		this.panelH = Math.min(228, this.height - 44);
		this.left = (this.width - PANEL_W) / 2;
		this.top = (this.height - this.panelH - 26) / 2;

		// Kapat dugmesi panelin altinda (panelin ustune binmesin)
		this.addRenderableWidget(Button.builder(Component.literal("Kapat"), btn -> this.onClose())
				.bounds(this.width / 2 - 40, this.top + this.panelH + 6, 80, 20)
				.build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
		super.extractRenderState(g, mouseX, mouseY, delta);

		// Telefon govdesi ve ekrani
		g.fill(left, top, left + PANEL_W, top + panelH, 0xFF15171C);
		g.outline(left, top, PANEL_W, panelH, 0xFF6A7080);
		g.fill(left + 6, top + 6, left + PANEL_W - 6, top + panelH - 6, 0xFF0B1424);

		int x = left + 14;
		int y = top + 13;

		centered(g, "DEPREM TAKİP", y, 0xFFFFFFFF);
		y += 14;
		g.fill(left + 12, y, left + PANEL_W - 12, y + 1, 0xFF2B3A55);
		y += 8;

		List<QuakeHistory.Entry> all = QuakeHistory.all();
		if (all.isEmpty()) {
			centered(g, "Henüz deprem kaydı yok.", y + 20, 0xFF9FB3D1);
			centered(g, "Her şey sakin.", y + 34, 0xFF7FD18B);
			return;
		}

		QuakeHistory.Entry last = all.get(0);
		DepremStartPayload d = last.data();
		int magColor = colorFor(d.magnitude());

		line(g, "SON DEPREM  (" + ago(last.timeMillis()) + ")", x, y, 0xFF9FB3D1);
		y += 14;
		line(g, "Büyüklük : " + EarthquakeManager.f1(d.magnitude()), x, y, magColor);
		y += 12;
		line(g, "Derinlik : " + EarthquakeManager.f1(d.depthKm()) + " km", x, y, 0xFFFFFFFF);
		y += 12;
		line(g, "Uzaklık  : " + EarthquakeManager.f1(d.distanceKm()) + " km (" + d.direction() + ")", x, y, 0xFFFFFFFF);
		y += 12;
		line(g, "Hissedilen: " + QuakeMath.feltLabel(d.felt()), x, y, 0xFFFFFFFF);
		y += 18;

		if (all.size() > 1) {
			line(g, "ÖNCEKİ DEPREMLER", x, y, 0xFF9FB3D1);
			y += 12;
			for (int i = 1; i < all.size() && y < top + panelH - 16; i++) {
				QuakeHistory.Entry e = all.get(i);
				DepremStartPayload p = e.data();
				String s = EarthquakeManager.f1(p.magnitude()) + " | " + EarthquakeManager.f1(p.depthKm())
						+ " km derin | " + EarthquakeManager.f1(p.distanceKm()) + " km uzak | " + ago(e.timeMillis());
				line(g, s, x, y, colorFor(p.magnitude()));
				y += 11;
			}
		}
	}

	private void line(GuiGraphicsExtractor g, String text, int x, int y, int argb) {
		g.text(this.font, text, x, y, argb, false);
	}

	private void centered(GuiGraphicsExtractor g, String text, int y, int argb) {
		int x = left + (PANEL_W - this.font.width(text)) / 2;
		g.text(this.font, text, x, y, argb, false);
	}

	private static int colorFor(double magnitude) {
		if (magnitude < 3.0) return 0xFF55FF55;
		if (magnitude < 5.0) return 0xFFFFFF55;
		if (magnitude < 6.5) return 0xFFFFAA00;
		return 0xFFFF5555;
	}

	private static String ago(long timeMillis) {
		long s = Math.max(0L, (System.currentTimeMillis() - timeMillis) / 1000L);
		if (s < 60) return s + " sn önce";
		if (s < 3600) return (s / 60) + " dk önce";
		return (s / 3600) + " sa önce";
	}
}
