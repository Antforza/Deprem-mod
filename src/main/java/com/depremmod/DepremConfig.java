package com.depremmod;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** config/deprem.properties dosyasinda saklanan ayarlar. */
public final class DepremConfig {
	/** Kendiliginden (rastgele) deprem olsun mu? */
	public static boolean autoEnabled = true;
	/** Iki otomatik deprem arasi en az / en cok bekleme (dakika). */
	public static int minMinutes = 10;
	public static int maxMinutes = 30;

	private DepremConfig() {}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("deprem.properties");
	}

	public static void load() {
		Path p = path();
		if (!Files.exists(p)) {
			save();
			return;
		}
		Properties props = new Properties();
		try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
			props.load(r);
			autoEnabled = Boolean.parseBoolean(props.getProperty("otomatik", "true"));
			minMinutes = parse(props.getProperty("min_dakika"), 10);
			maxMinutes = parse(props.getProperty("max_dakika"), 30);
			if (maxMinutes < minMinutes) {
				maxMinutes = minMinutes;
			}
		} catch (IOException | RuntimeException e) {
			DepremMod.LOGGER.warn("deprem.properties okunamadi, varsayilanlar kullaniliyor", e);
		}
	}

	public static void save() {
		Properties props = new Properties();
		props.setProperty("otomatik", Boolean.toString(autoEnabled));
		props.setProperty("min_dakika", Integer.toString(minMinutes));
		props.setProperty("max_dakika", Integer.toString(maxMinutes));
		try (Writer w = Files.newBufferedWriter(path(), StandardCharsets.UTF_8)) {
			props.store(w, "Deprem modu ayarlari");
		} catch (IOException e) {
			DepremMod.LOGGER.warn("deprem.properties yazilamadi", e);
		}
	}

	private static int parse(String s, int def) {
		try {
			return Math.max(1, Integer.parseInt(s.trim()));
		} catch (RuntimeException e) {
			return def;
		}
	}
}
