package com.depremmod;

import com.depremmod.net.DepremStartPayload;
import com.depremmod.net.DepremStopPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Sunucu tarafi: depremi baslatir, durdurur, rastgele deprem zamanlayicisini yonetir. */
public final class EarthquakeManager {
	private static final Random RNG = new Random();
	private static int ticksUntilNext = -1;

	private EarthquakeManager() {}

	public record Result(double magnitude, double depthKm, double distanceKm, int playersAffected) {}

	// ---------------------------------------------------------------- zamanlayici

	public static void resetTimer() {
		ticksUntilNext = -1;
	}

	private static int rollDelayTicks() {
		int min = Math.max(1, DepremConfig.minMinutes);
		int max = Math.max(min, DepremConfig.maxMinutes);
		double minutes = min + RNG.nextDouble() * (max - min);
		return (int) (minutes * 60.0 * 20.0);
	}

	public static void tick(MinecraftServer server) {
		if (!DepremConfig.autoEnabled) {
			ticksUntilNext = -1;
			return;
		}
		if (PlayerLookup.all(server).isEmpty()) {
			return;
		}
		if (ticksUntilNext < 0) {
			ticksUntilNext = rollDelayTicks();
		}
		ticksUntilNext--;
		if (ticksUntilNext <= 0) {
			ticksUntilNext = rollDelayTicks();
			triggerRandom(server);
		}
	}

	// ---------------------------------------------------------------- rastgele deprem

	/** %45 kucuk, %35 orta, %15 guclu, %5 buyuk. */
	public static double randomMagnitude() {
		double r = RNG.nextDouble();
		if (r < 0.45) return 2.0 + RNG.nextDouble() * 1.9;   // 2.0 - 3.9
		if (r < 0.80) return 4.0 + RNG.nextDouble() * 1.4;   // 4.0 - 5.4
		if (r < 0.95) return 5.5 + RNG.nextDouble() * 1.4;   // 5.5 - 6.9
		return 7.0 + RNG.nextDouble() * 1.5;                  // 7.0 - 8.5
	}

	public static double randomDepth() {
		if (RNG.nextDouble() < 0.07) {
			return 60.0 + RNG.nextDouble() * 90.0;   // nadir derin deprem
		}
		return 4.0 + RNG.nextDouble() * 26.0;
	}

	public static double randomDistanceFor(double magnitude) {
		double min;
		double max;
		if (magnitude < 4.0) { min = 3; max = 30; }
		else if (magnitude < 5.5) { min = 8; max = 70; }
		else if (magnitude < 7.0) { min = 15; max = 140; }
		else { min = 30; max = 300; }
		return min + RNG.nextDouble() * (max - min);
	}

	/** Rastgele bir deprem baslatir. Oyuncu yoksa null doner. */
	public static Result triggerRandom(MinecraftServer server) {
		ServerPlayer ref = anyOverworldPlayer(server);
		if (ref == null) {
			return null;
		}
		double mag = randomMagnitude();
		return trigger(server, ref, mag, randomDepth(), randomDistanceFor(mag));
	}

	/** Verilen oyuncudan bagimsiz, herhangi bir Overworld oyuncusu (rastgele). */
	public static ServerPlayer anyOverworldPlayer(MinecraftServer server) {
		List<ServerPlayer> candidates = new ArrayList<>();
		for (ServerPlayer p : PlayerLookup.all(server)) {
			if (p.level().dimension().equals(Level.OVERWORLD)) {
				candidates.add(p);
			}
		}
		if (candidates.isEmpty()) {
			return null;
		}
		return candidates.get(RNG.nextInt(candidates.size()));
	}

	// ---------------------------------------------------------------- baslat / durdur

	/**
	 * Depremi baslatir. Merkez ussu, "reference" oyuncudan rastgele bir yonde distanceKm uzakliktadir.
	 * Her oyuncu kendi mesafesine gore etkilenir.
	 */
	public static Result trigger(MinecraftServer server, ServerPlayer reference,
								 double magnitude, double depthKm, double distanceKm) {
		double angle = RNG.nextDouble() * Math.PI * 2.0;
		double epiX = reference.getX() + Math.cos(angle) * distanceKm * 1000.0;
		double epiZ = reference.getZ() + Math.sin(angle) * distanceKm * 1000.0;

		int affected = 0;
		for (ServerPlayer p : PlayerLookup.all(server)) {
			if (!p.level().dimension().equals(Level.OVERWORLD)) {
				continue;   // Nether / End'de deprem hissedilmez
			}
			double dx = epiX - p.getX();
			double dz = epiZ - p.getZ();
			double d = Math.sqrt(dx * dx + dz * dz) / 1000.0;
			double felt = QuakeMath.feltIntensity(magnitude, d, depthKm);

			DepremStartPayload payload = new DepremStartPayload(
					(float) magnitude, (float) depthKm, (float) d, (float) felt,
					QuakeMath.durationTicks(magnitude), QuakeMath.direction(dx, dz));

			ServerPlayNetworking.send(p, payload);
			if (hasPhone(p)) {
				notifyPhone(p, payload);
			}
			affected++;
		}
		return new Result(magnitude, depthKm, distanceKm, affected);
	}

	public static void stop(MinecraftServer server) {
		for (ServerPlayer p : PlayerLookup.all(server)) {
			ServerPlayNetworking.send(p, DepremStopPayload.INSTANCE);
		}
	}

	// ---------------------------------------------------------------- telefon

	private static boolean hasPhone(ServerPlayer p) {
		Inventory inv = p.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (inv.getItem(i).is(ModItems.TELEFON)) {
				return true;
			}
		}
		return false;
	}

	private static void notifyPhone(ServerPlayer p, DepremStartPayload d) {
		p.sendSystemMessage(Component.literal(
				"[Deprem Telefonu] DEPREM! Büyüklük: " + f1(d.magnitude())
						+ " | Derinlik: " + f1(d.depthKm()) + " km"
						+ " | Uzaklık: " + f1(d.distanceKm()) + " km (" + d.direction() + ")"));
		p.sendSystemMessage(Component.literal(
				"[Deprem Telefonu] Sizde hissedilen: " + QuakeMath.feltLabel(d.felt())
						+ " — ayrıntı için telefonu elinize alıp sağ tıklayın."));
	}

	public static String f1(double v) {
		return String.format(Locale.ROOT, "%.1f", v);
	}
}
