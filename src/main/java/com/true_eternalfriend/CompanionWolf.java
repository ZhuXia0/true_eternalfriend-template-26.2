package com.true_eternalfriend;

import net.minecraft.world.Container;

/** The wolf owns the two custom slots; BODY equipment remains vanilla-owned. */
public interface CompanionWolf {
    Container trueEternalFriend$gear();
    int trueEternalFriend$eatCooldown();
    void trueEternalFriend$setEatCooldown(int ticks);
}
