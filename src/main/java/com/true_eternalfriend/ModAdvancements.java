package com.true_eternalfriend;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class ModAdvancements {
    public static final Identifier FIRST_FRIEND = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, "true_eternal_friend");
    public static final Identifier FIRST_REVIVAL = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, "goodbye_hello");
    private static final String CRITERION = "event";

    private ModAdvancements() { }

    public static boolean grant(ServerPlayer player, Identifier id) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
        if (advancement == null) return false;
        if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            player.getAdvancements().award(advancement, CRITERION);
        }
        return true;
    }
}
