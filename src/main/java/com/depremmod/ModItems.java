package com.depremmod;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ModItems {
	public static final ResourceKey<Item> TELEFON_KEY =
			ResourceKey.create(Registries.ITEM, DepremMod.id("telefon"));

	public static final Item TELEFON = register(TELEFON_KEY, TelefonItem::new, new Item.Properties().stacksTo(1));

	private ModItems() {}

	private static Item register(ResourceKey<Item> key, Function<Item.Properties, Item> factory, Item.Properties props) {
		Item item = factory.apply(props.setId(key));
		Registry.register(BuiltInRegistries.ITEM, key, item);
		return item;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
				.register(tab -> tab.accept(TELEFON));
	}
}
