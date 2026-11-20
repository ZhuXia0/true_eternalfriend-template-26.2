package com.true_eternalfriend;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.item.ItemStack;

public final class CompanionCommands {
    private CompanionCommands() { }
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            var command = Commands.literal("eternalfriend");
            var debug = Commands.literal("debug");
            var gear = Commands.literal("gear");
            gear.then(Commands.argument("id", IntegerArgumentType.integer(1)).executes(context -> {
                int id = IntegerArgumentType.getInteger(context, "id");
                if (!(context.getSource().getLevel().getEntity(id) instanceof Wolf wolf)) {
                    context.getSource().sendFailure(Component.literal("No wolf with entity id " + id));
                    return 0;
                }
                CompanionWolf data = (CompanionWolf) wolf;
                ItemStack armor = wolf.getItemBySlot(EquipmentSlot.BODY);
                ItemStack food = data.trueEternalFriend$gear().getItem(0);
                ItemStack claw = data.trueEternalFriend$gear().getItem(1);
                String report = "Wolf " + id + " armor=" + armor + " food=" + food + " claw=" + claw
                    + " health=" + wolf.getHealth() + "/" + wolf.getMaxHealth()
                    + " threshold=" + CompanionLogic.EAT_HEALTH_THRESHOLD
                    + " nextHeal=" + (food.isEmpty() ? 0 : CompanionLogic.nextHeal(food))
                    + " eatCd=" + data.trueEternalFriend$eatCooldown()
                    + " armorDurability=" + (armor.getMaxDamage() - armor.getDamageValue()) + "/" + armor.getMaxDamage()
                    + " attackBonusTag=" + (((EternalWolf) wolf).trueEternalFriend$isEternal() ? 10 : 0)
                    + " attackBonusClaw=" + CompanionItems.clawDamage(claw.getItem());
                context.getSource().sendSuccess(() -> Component.literal(report), false);
                return 1;
            }));
            debug.then(gear);
            command.then(debug);
            dispatcher.register(command);
        });
    }
}
