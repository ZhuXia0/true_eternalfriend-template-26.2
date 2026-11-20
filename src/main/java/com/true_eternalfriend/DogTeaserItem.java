package com.true_eternalfriend;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public final class DogTeaserItem extends Item {
    public DogTeaserItem(Properties properties) { super(properties); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                          Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.true_eternal_friend.dog_teaser.tooltip"));
    }
}
