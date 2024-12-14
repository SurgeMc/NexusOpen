package net.cubzyn;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.Screen;

public class CustomMainMenuMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Replace the default TitleScreen with your custom one
        //MinecraftClient.getInstance().getToasts().add(new CustomMainMenuScreen());
    }
}
