package com.true_eternalfriend;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public final class TrueEternalFriendClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        MenuScreens.register(WolfCompanionMenu.TYPE, WolfCompanionScreen::new);
    }
}
