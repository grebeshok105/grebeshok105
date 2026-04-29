package com.example.superheroes.item;

import com.example.superheroes.ModId;
import com.example.superheroes.entity.ModEntities;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;

public final class ModItems {
	public static final HomelanderSuitItem HOMELANDER_SUIT = register(
			"homelander_suit",
			new HomelanderSuitItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC))
	);

	public static final IronManSuitItem IRON_MAN_SUIT = register(
			"iron_man_suit",
			new IronManSuitItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC))
	);

	public static final CompoundVItem COMPOUND_V = register(
			"compound_v",
			new CompoundVItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON))
	);

	public static final MilkBottleItem MILK_BOTTLE = register(
			"milk_bottle",
			new MilkBottleItem(new Item.Properties().stacksTo(8).rarity(Rarity.RARE))
	);

	public static final IronManReactorItem IRON_MAN_REACTOR = register(
			"iron_man_reactor",
			new IronManReactorItem(new Item.Properties().stacksTo(4).rarity(Rarity.RARE))
	);

	public static final UraniumIsotopeItem URANIUM_ISOTOPE = register(
			"uranium_isotope",
			new UraniumIsotopeItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE))
	);

	public static final UraniumDaggerItem URANIUM_DAGGER = register(
			"uranium_dagger",
			new UraniumDaggerItem(new Item.Properties().stacksTo(1).durability(250).rarity(Rarity.EPIC))
	);

	public static final RegulusSuitItem REGULUS_SUIT = register(
			"regulus_suit",
			new RegulusSuitItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC))
	);

	public static final EvangelionItem EVANGELION = register(
			"evangelion",
			new EvangelionItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC))
	);

	public static final VoughtSignalItem VOUGHT_SIGNAL = register(
			"vought_signal",
			new VoughtSignalItem(new Item.Properties().stacksTo(4).rarity(Rarity.EPIC))
	);

	public static final SpawnEggItem HOMELANDER_BOSS_SPAWN_EGG = register(
			"homelander_boss_spawn_egg",
			new SpawnEggItem(ModEntities.HOMELANDER_BOSS, 0x2FB200, 0x6BD43A,
					new Item.Properties().rarity(Rarity.EPIC))
	);

	public static final ShadowMonarchsCloakItem SHADOW_MONARCHS_CLOAK = register(
			"shadow_monarchs_cloak",
			new ShadowMonarchsCloakItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC))
	);

	public static final DoomsdaySuitItem DOOMSDAY_GENOME = register(
			"doomsday_genome",
			new DoomsdaySuitItem(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC))
	);

	private ModItems() {
	}

	private static <T extends Item> T register(String name, T item) {
		return Registry.register(BuiltInRegistries.ITEM, ModId.of(name), item);
	}

	public static void init() {
	}
}
