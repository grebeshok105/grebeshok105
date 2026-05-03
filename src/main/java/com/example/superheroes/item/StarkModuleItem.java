package com.example.superheroes.item;

import com.example.superheroes.effect.IronManMarkPromotion;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class StarkModuleItem extends Item {
	private static final int TARGET_MARK = 2;

	public StarkModuleItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		TooltipFrame.openDivider(tooltip, ChatFormatting.GOLD);
		tooltip.add(TooltipFrame.flavor("item.superheroes.stark_module.lore.line1", ChatFormatting.GOLD));
		tooltip.add(TooltipFrame.flavor("item.superheroes.stark_module.lore.line2", ChatFormatting.DARK_GRAY));
		tooltip.add(Component.empty());
		tooltip.add(TooltipFrame.bullet("item.superheroes.stark_module.lore.usage", ChatFormatting.AQUA));
		tooltip.add(TooltipFrame.bullet("item.superheroes.stark_module.lore.requires", ChatFormatting.YELLOW));
		TooltipFrame.closeDivider(tooltip, ChatFormatting.GOLD);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResultHolder.success(stack);
		}
		if (!(player instanceof ServerPlayer sp)) {
			return InteractionResultHolder.pass(stack);
		}
		if (!IronManMarkPromotion.isIronMan(sp)) {
			sp.displayClientMessage(Component.translatable(
					"ability.superheroes.iron_man.must_be_iron_man").withStyle(ChatFormatting.RED), true);
			return InteractionResultHolder.fail(stack);
		}
		if (IronManMarkPromotion.currentMark(sp) >= TARGET_MARK) {
			sp.displayClientMessage(Component.translatable(
					"ability.superheroes.iron_man.already_at_mark_vii").withStyle(ChatFormatting.YELLOW), true);
			return InteractionResultHolder.fail(stack);
		}
		boolean ok = IronManMarkPromotion.promote(sp, TARGET_MARK);
		if (!ok) {
			return InteractionResultHolder.fail(stack);
		}
		if (!sp.getAbilities().instabuild) {
			stack.shrink(1);
		}
		return InteractionResultHolder.consume(stack);
	}
}
