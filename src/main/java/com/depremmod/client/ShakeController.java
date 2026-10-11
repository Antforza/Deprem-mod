package com.depremmod.client;

import com.depremmod.QuakeMath;
import com.depremmod.net.DepremStartPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

/**
 * Kamera sallanmasi. Mixin kullanmaz: her tick oyuncunun bakis acisina kucuk bir
 * rastgele sapma ekler, bir sonraki tick'te onceki sapmayi geri alir. Boylece
 * fare ile bakmaya devam edebilirsiniz ve deprem bitince kamera yerine oturur.
 */
public final class ShakeController {
	private static final Random RNG = new Random();

	private static int remaining = 0;
	private static int total = 1;
	private static float strength = 0f;     // 0..1

	private static float lastYaw = 0f;      // son tick'te eklenen sapma (derece)
	private static float lastPitch = 0f;

	private ShakeController() {}

	public static void start(DepremStartPayload p) {
		float s = QuakeMath.shakeStrength(p.felt());
		if (s <= 0f) {
			return;   // hissedilmeyecek kadar uzak/zayif: sadece telefon haber verir
		}
		strength = s;
		total = Math.max(1, p.durationTicks());
		remaining = total;
	}

	public static void stop() {
		remaining = 0;
	}

	public static void tick(Player player) {
		if (player == null) {
			remaining = 0;
			lastYaw = 0f;
			lastPitch = 0f;
			return;
		}

		float prevYaw = lastYaw;
		float prevPitch = lastPitch;
		lastYaw = 0f;
		lastPitch = 0f;

		if (remaining > 0) {
			remaining--;
			double t = 1.0 - (double) remaining / total;          // 0 -> 1
			double env = envelope(t);
			double amp = (0.3 + 6.0 * Math.pow(strength, 1.5)) * env;   // derece

			lastYaw = (float) ((RNG.nextDouble() * 2.0 - 1.0) * amp);
			lastPitch = (float) ((RNG.nextDouble() * 2.0 - 1.0) * amp * 0.7);

			// Guclu depremlerde oyuncuyu hafifce yalpalat
			if (strength > 0.45f && player.onGround()) {
				double push = (0.01 + 0.05 * strength) * env;
				player.setDeltaMovement(player.getDeltaMovement().add(
						(RNG.nextDouble() * 2.0 - 1.0) * push, 0.0, (RNG.nextDouble() * 2.0 - 1.0) * push));
			}
		}

		if (prevYaw != 0f || prevPitch != 0f || lastYaw != 0f || lastPitch != 0f) {
			player.setYRot(player.getYRot() - prevYaw + lastYaw);
			float pitch = player.getXRot() - prevPitch + lastPitch;
			player.setXRot(Math.max(-90f, Math.min(90f, pitch)));
		}
	}

	/** Hizli yukselis, dalgali orta kisim, yavas sonme. */
	private static double envelope(double t) {
		if (t < 0.1) {
			return t / 0.1;
		}
		if (t > 0.7) {
			return Math.max(0.0, (1.0 - t) / 0.3);
		}
		return 0.85 + 0.15 * Math.sin(t * 40.0);
	}
}
