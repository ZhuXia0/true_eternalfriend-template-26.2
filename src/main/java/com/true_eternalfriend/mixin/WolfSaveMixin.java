package com.true_eternalfriend.mixin;

import com.true_eternalfriend.EternalWolf;
import com.true_eternalfriend.TrueEternalFriend;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Wolf.class)
public abstract class WolfSaveMixin implements EternalWolf {
    @Unique private boolean trueEternalFriend$eternal;

    @Override public boolean trueEternalFriend$isEternal() { return trueEternalFriend$eternal; }
    @Override public void trueEternalFriend$setEternal(boolean value) { trueEternalFriend$eternal = value; }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void trueEternalFriend$save(ValueOutput output, CallbackInfo ci) {
        output.putBoolean(TrueEternalFriend.NBT_KEY, trueEternalFriend$eternal);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void trueEternalFriend$load(ValueInput input, CallbackInfo ci) {
        trueEternalFriend$eternal = input.getBooleanOr(TrueEternalFriend.NBT_KEY, false);
        if (trueEternalFriend$eternal) TrueEternalFriend.applyAttack((Wolf)(Object)this);
    }
}
