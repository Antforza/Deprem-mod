package com.depremmod;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Deprem telefonu. Sag tiklayinca (istemcide) deprem uygulamasi acilir.
 * Ekrani acan kod istemci tarafinda DepremClient icinde baglanir; bu sinif istemci
 * siniflarina dogrudan dokunmaz, boylece sunucuda da sorunsuz yuklenir.
 */
public class TelefonItem extends Item {
	/** DepremClient tarafindan ayarlanir. */
	public static Runnable openScreenHook = () -> {};

	public TelefonItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide()) {
			openScreenHook.run();
		}
		return InteractionResult.SUCCESS;
	}
}
