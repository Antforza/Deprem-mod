package com.depremmod;

/**
 * Deprem hesaplari (Minecraft'a bagli degil).
 * 1 blok = 1 metre kabul edilir, mesafeler km cinsindendir.
 */
public final class QuakeMath {
	private QuakeMath() {}

	/** Hissedilen siddet (MMI benzeri, 0-10). Buyukluk arttikca artar, uzaklik/derinlik arttikca azalir. */
	public static double feltIntensity(double magnitude, double distanceKm, double depthKm) {
		double r = Math.sqrt(distanceKm * distanceKm + depthKm * depthKm);
		r = Math.max(r, 1.0);
		double mmi = 1.5 * magnitude - 3.5 * Math.log10(r) + 3.0;
		return Math.max(0.0, Math.min(10.0, mmi));
	}

	/** Hissedilen siddeti 0-1 arasi sallanma gucune cevirir. 0 = hissedilmez. */
	public static float shakeStrength(double felt) {
		return (float) Math.max(0.0, Math.min(1.0, (felt - 2.0) / 7.0));
	}

	/** Sallanma suresi (tick). Buyukluk 5 icin ~20 sn. */
	public static int durationTicks(double magnitude) {
		double seconds = Math.max(3.0, Math.min(60.0, 5.0 * (magnitude - 1.0)));
		return (int) (seconds * 20.0);
	}

	/** Oyuncudan merkez ussune yon (Minecraft: +x dogu, -z kuzey). */
	public static String direction(double dx, double dz) {
		if (Math.sqrt(dx * dx + dz * dz) < 50.0) {
			return "Tam altınızda";
		}
		double angle = Math.toDegrees(Math.atan2(dx, -dz));
		if (angle < 0) {
			angle += 360.0;
		}
		String[] names = {"Kuzey", "Kuzeydoğu", "Doğu", "Güneydoğu", "Güney", "Güneybatı", "Batı", "Kuzeybatı"};
		return names[(int) Math.round(angle / 45.0) % 8];
	}

	public static String feltLabel(double felt) {
		if (felt < 2.0) return "Hissedilmedi";
		if (felt < 3.5) return "Hafif";
		if (felt < 5.0) return "Orta";
		if (felt < 6.5) return "Güçlü";
		if (felt < 8.0) return "Çok güçlü";
		return "Yıkıcı";
	}
}
