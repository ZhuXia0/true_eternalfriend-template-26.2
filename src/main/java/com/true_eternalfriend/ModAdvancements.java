package com.true_eternalfriend;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class ModAdvancements {
    public static final Identifier FIRST_FRIEND = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, "true_eternal_friend");
    public static final Identifier FIRST_REVIVAL = Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, "goodbye_hello");
    public static final Identifier COMPANION_SCREEN = id("companion_screen");
    public static final Identifier FIRST_ARMOR = id("first_armor");
    public static final Identifier FIRST_CLAW_HIT = id("first_claw_hit");
    public static final Identifier FIRST_FEEDING = id("first_feeding");
    private static final String CRITERION = "event";

    private ModAdvancements() { }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(TrueEternalFriend.MOD_ID, path); }

    public static boolean grant(ServerPlayer player, Identifier id) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
        if (advancement == null) return false;
        if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
            player.getAdvancements().award(advancement, CRITERION);
        }
        return true;
    }
}
