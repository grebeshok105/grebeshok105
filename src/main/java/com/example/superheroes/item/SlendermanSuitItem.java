package com.example.superheroes.item;

import com.example.superheroes.hero.SlendermanHero;
import com.example.superheroes.transform.TransformationItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SlendermanSuitItem extends TransformationItem {
	public SlendermanSuitItem(Properties properties) {
		super(SlendermanHero.ID, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		TooltipFrame.openDivider(tooltip, ChatFormatting.DARK_GRAY);
		tooltip.add(TooltipFrame.flavor("item.superheroes.slenderman_suit.lore.line1", ChatFormatting.GRAY));
		tooltip.add(TooltipFrame.flavor("item.superheroes.slenderman_suit.lore.line2", ChatFormatting.DARK_GRAY));
		tooltip.add(Component.empty());
		tooltip.add(TooltipFrame.bullet("item.superheroes.slenderman_suit.lore.usage", ChatFormatting.LIGHT_PURPLE));
		tooltip.add(TooltipFrame.bullet("item.superheroes.slenderman_suit.lore.untransform", ChatFormatting.WHITE));
		TooltipFrame.closeDivider(tooltip, ChatFormatting.DARK_GRAY);
	}
}
