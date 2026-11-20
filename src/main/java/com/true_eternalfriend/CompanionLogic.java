package com.true_eternalfriend;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class CompanionLogic {
    private CompanionLogic() { }
    public static final double WOLF_MAX_HEALTH_BONUS = 10;
    public static final float EAT_HEALTH_THRESHOLD = 30;
    public static final int EAT_CD_TICKS = 1200;
    public static final int NUTRITION_MIN = 1;
    public static final Identifier HEALTH_ID = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, "companion_max_health");
    public static final Identifier CLAW_ID = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, "companion_claw_attack");
    public static final TagKey<Item> DOG_FOOD = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, "dog_food"));

    public static void ensureHealth(Wolf wolf) {
        if (!wolf.isTame()) return;
        AttributeInstance health = wolf.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getModifier(HEALTH_ID) == null) {
            health.addPermanentModifier(new AttributeModifier(HEALTH_ID, WOLF_MAX_HEALTH_BONUS, AttributeModifier.Operation.ADD_VALUE));
        }
    }
    public static void applyClaw(Wolf wolf) {
        AttributeInstance attack = wolf.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) return;
        attack.removeModifier(CLAW_ID);
        Item item = ((CompanionWolf) wolf).trueEternalFriend$gear().getItem(1).getItem();
        int bonus = CompanionItems.clawDamage(item);
        if (bonus > 0) attack.addPermanentModifier(new AttributeModifier(CLAW_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
    }
    public static int nextHeal(ItemStack stack) {
        FoodProperties food = stack.get(DataComponents.FOOD);
        return food == null ? NUTRITION_MIN : Math.max(NUTRITION_MIN, food.nutrition());
    }
    public static void tick(Wolf wolf) {
        if (!(wolf.level() instanceof ServerLevel level) || !wolf.isTame()) return;
        ensureHealth(wolf);
        if (wolf.getItemBySlot(EquipmentSlot.BODY).is(CompanionItems.GOLDEN_WOLF_ARMOR)) {
            wolf.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, 1, true, false));
        }
        CompanionWolf data = (CompanionWolf) wolf;
        if (data.trueEternalFriend$eatCooldown() > 0) {
            data.trueEternalFriend$setEatCooldown(data.trueEternalFriend$eatCooldown() - 1);
            return;
        }
        Container gear = data.trueEternalFriend$gear();
        ItemStack food = gear.getItem(0);
        if (food.isEmpty() || !food.is(DOG_FOOD) || wolf.getHealth() >= EAT_HEALTH_THRESHOLD || wolf.getHealth() >= wolf.getMaxHealth()) return;
        float heal = Math.min(nextHeal(food), wolf.getMaxHealth() - wolf.getHealth());
        if (heal <= 0) return;
        food.shrink(1);
        gear.setChanged();
        wolf.setHealth(wolf.getHealth() + heal);
        data.trueEternalFriend$setEatCooldown(CompanionConfig.eatCdTicks());
        TrueEternalFriend.awardOwner(wolf, ModAdvancements.FIRST_FEEDING);
        level.playSound(null, wolf.blockPosition(), SoundEvents.GENERIC_EAT.value(), SoundSource.NEUTRAL, 1, 1);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, wolf.getX(), wolf.getY() + .5, wolf.getZ(), 5, .2, .2, .2, 0);
    }
}
