package com.depremmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.Random;

/**
 * /deprem start [siddet] [derinlik_km] [uzaklik_km]
 * /deprem stop
 * /deprem rastgele
 * /deprem otomatik ac|kapat
 * /deprem aralik <min_dk> <max_dk>
 * /deprem durum
 */
public final class DepremCommand {
	private static final Random RNG = new Random();

	private DepremCommand() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("deprem")
				.requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))

				.then(Commands.literal("start")
						.executes(ctx -> start(ctx, 5.0, -1, -1))
						.then(Commands.argument("siddet", DoubleArgumentType.doubleArg(1.0, 10.0))
								.executes(ctx -> start(ctx,
										DoubleArgumentType.getDouble(ctx, "siddet"), -1, -1))
								.then(Commands.argument("derinlik", DoubleArgumentType.doubleArg(1.0, 700.0))
										.executes(ctx -> start(ctx,
												DoubleArgumentType.getDouble(ctx, "siddet"),
												DoubleArgumentType.getDouble(ctx, "derinlik"), -1))
										.then(Commands.argument("uzaklik", DoubleArgumentType.doubleArg(0.0, 1000.0))
												.executes(ctx -> start(ctx,
														DoubleArgumentType.getDouble(ctx, "siddet"),
														DoubleArgumentType.getDouble(ctx, "derinlik"),
														DoubleArgumentType.getDouble(ctx, "uzaklik")))))))

				.then(Commands.literal("stop").executes(DepremCommand::stop))

				.then(Commands.literal("rastgele").executes(DepremCommand::random))

				.then(Commands.literal("otomatik")
						.then(Commands.literal("ac").executes(ctx -> setAuto(ctx, true)))
						.then(Commands.literal("kapat").executes(ctx -> setAuto(ctx, false))))

				.then(Commands.literal("aralik")
						.then(Commands.argument("min", IntegerArgumentType.integer(1, 600))
								.then(Commands.argument("max", IntegerArgumentType.integer(1, 600))
										.executes(DepremCommand::setInterval))))

				.then(Commands.literal("durum").executes(DepremCommand::status)));
	}

	private static int start(CommandContext<CommandSourceStack> ctx, double magnitude, double depth, double distance) {
		CommandSourceStack src = ctx.getSource();
		MinecraftServer server = src.getServer();

		ServerPlayer ref = src.getPlayer();
		if (ref == null) {
			ref = EarthquakeManager.anyOverworldPlayer(server);
		}
		if (ref == null) {
			src.sendFailure(Component.literal("Overworld'de deprem hissedecek oyuncu yok."));
			return 0;
		}

		final double depthKm = depth >= 0 ? depth : 5.0 + RNG.nextDouble() * 15.0;
		final double distKm = distance >= 0 ? distance : 8.0 + RNG.nextDouble() * 17.0;

		EarthquakeManager.Result r = EarthquakeManager.trigger(server, ref, magnitude, depthKm, distKm);
		src.sendSuccess(() -> Component.literal("Deprem başladı: büyüklük " + EarthquakeManager.f1(r.magnitude())
				+ ", derinlik " + EarthquakeManager.f1(r.depthKm()) + " km, merkez üssü "
				+ EarthquakeManager.f1(r.distanceKm()) + " km uzakta. Durdurmak için /deprem stop"), true);
		return 1;
	}

	private static int stop(CommandContext<CommandSourceStack> ctx) {
		EarthquakeManager.stop(ctx.getSource().getServer());
		ctx.getSource().sendSuccess(() -> Component.literal("Deprem durduruldu."), true);
		return 1;
	}

	private static int random(CommandContext<CommandSourceStack> ctx) {
		EarthquakeManager.Result r = EarthquakeManager.triggerRandom(ctx.getSource().getServer());
		if (r == null) {
			ctx.getSource().sendFailure(Component.literal("Overworld'de oyuncu yok."));
			return 0;
		}
		ctx.getSource().sendSuccess(() -> Component.literal("Rastgele deprem oldu: büyüklük "
				+ EarthquakeManager.f1(r.magnitude())), true);
		return 1;
	}

	private static int setAuto(CommandContext<CommandSourceStack> ctx, boolean on) {
		DepremConfig.autoEnabled = on;
		DepremConfig.save();
		EarthquakeManager.resetTimer();
		ctx.getSource().sendSuccess(() -> Component.literal(on
				? "Kendiliğinden deprem AÇIK (" + DepremConfig.minMinutes + "-" + DepremConfig.maxMinutes + " dk arayla)."
				: "Kendiliğinden deprem KAPALI."), true);
		return 1;
	}

	private static int setInterval(CommandContext<CommandSourceStack> ctx) {
		int a = IntegerArgumentType.getInteger(ctx, "min");
		int b = IntegerArgumentType.getInteger(ctx, "max");
		DepremConfig.minMinutes = Math.min(a, b);
		DepremConfig.maxMinutes = Math.max(a, b);
		DepremConfig.save();
		EarthquakeManager.resetTimer();
		ctx.getSource().sendSuccess(() -> Component.literal("Rastgele depremler artık "
				+ DepremConfig.minMinutes + "-" + DepremConfig.maxMinutes + " dakika arayla olacak."), true);
		return 1;
	}

	private static int status(CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.literal("Otomatik deprem: "
				+ (DepremConfig.autoEnabled ? "açık" : "kapalı")
				+ " | aralık: " + DepremConfig.minMinutes + "-" + DepremConfig.maxMinutes + " dk"), false);
		return 1;
	}
}
