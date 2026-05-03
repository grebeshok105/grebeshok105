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

public class QuantumCoreItem extends Item {
	private static final int TARGET_MARK = 3;

	public QuantumCoreItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		TooltipFrame.openDivider(tooltip, ChatFormatting.LIGHT_PURPLE);
		tooltip.add(TooltipFrame.flavor("item.superheroes.quantum_core.lore.line1", ChatFormatting.LIGHT_PURPLE));
		tooltip.add(TooltipFrame.flavor("item.superheroes.quantum_core.lore.line2", ChatFormatting.DARK_GRAY));
		tooltip.add(Component.empty());
		tooltip.add(TooltipFrame.bullet("item.superheroes.quantum_core.lore.usage", ChatFormatting.AQUA));
		tooltip.add(TooltipFrame.bullet("item.superheroes.quantum_core.lore.requires", ChatFormatting.YELLOW));
		TooltipFrame.closeDivider(tooltip, ChatFormatting.LIGHT_PURPLE);
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
		if (IronManMarkPromotion.currentMark(sp) < 2) {
			sp.displayClientMessage(Component.translatable(
					"ability.superheroes.iron_man.need_mark_vii_first").withStyle(ChatFormatting.YELLOW), true);
			return InteractionResultHolder.fail(stack);
		}
		if (IronManMarkPromotion.currentMark(sp) >= TARGET_MARK) {
			sp.displayClientMessage(Component.translatable(
					"ability.superheroes.iron_man.already_at_mark_l").withStyle(ChatFormatting.YELLOW), true);
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
