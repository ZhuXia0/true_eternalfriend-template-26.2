package com.true_eternalfriend.mixin;

import com.true_eternalfriend.EternalWolf;
import com.true_eternalfriend.TrueEternalFriend;
import com.true_eternalfriend.CompanionWolf;
import com.true_eternalfriend.CompanionLogic;
import com.true_eternalfriend.CompanionItems;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfSaveMixin implements EternalWolf, CompanionWolf {
    @Unique private boolean trueEternalFriend$eternal;
    @Unique private int trueEternalFriend$eatCooldown;
    @Unique private final SimpleContainer trueEternalFriend$gear = new SimpleContainer(2) {
        @Override public void setChanged() {
            super.setChanged();
            CompanionLogic.applyClaw((Wolf)(Object)WolfSaveMixin.this);
        }
    };

    @Override public Container trueEternalFriend$gear() { return trueEternalFriend$gear; }
    @Override public int trueEternalFriend$eatCooldown() { return trueEternalFriend$eatCooldown; }
    @Override public void trueEternalFriend$setEatCooldown(int ticks) { trueEternalFriend$eatCooldown = Math.max(0, ticks); }

    @Override public boolean trueEternalFriend$isEternal() { return trueEternalFriend$eternal; }
    @Override public void trueEternalFriend$setEternal(boolean value) { trueEternalFriend$eternal = value; }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void trueEternalFriend$save(ValueOutput output, CallbackInfo ci) {
        output.putBoolean(TrueEternalFriend.NBT_KEY, trueEternalFriend$eternal);
        output.putInt("true_eternal_friend:eat_cd", trueEternalFriend$eatCooldown);
        output.store("true_eternal_friend:food", ItemStack.OPTIONAL_CODEC, trueEternalFriend$gear.getItem(0));
        output.store("true_eternal_friend:claw", ItemStack.OPTIONAL_CODEC, trueEternalFriend$gear.getItem(1));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void trueEternalFriend$load(ValueInput input, CallbackInfo ci) {
        trueEternalFriend$eternal = input.getBooleanOr(TrueEternalFriend.NBT_KEY, false);
        trueEternalFriend$eatCooldown = input.getIntOr("true_eternal_friend:eat_cd", 0);
        trueEternalFriend$gear.setItem(0, input.read("true_eternal_friend:food", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        trueEternalFriend$gear.setItem(1, input.read("true_eternal_friend:claw", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        CompanionLogic.ensureHealth((Wolf)(Object)this);
        CompanionLogic.applyClaw((Wolf)(Object)this);
        if (trueEternalFriend$eternal) TrueEternalFriend.applyAttack((Wolf)(Object)this);
    }

    @Inject(method = "canArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void trueEternalFriend$armorAbsorb(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Wolf wolf = (Wolf)(Object)this;
        if (CompanionItems.isArmor(wolf.getItemBySlot(EquipmentSlot.BODY).getItem()) && !source.is(DamageTypeTags.BYPASSES_WOLF_ARMOR))
            cir.setReturnValue(true);
    }
}
