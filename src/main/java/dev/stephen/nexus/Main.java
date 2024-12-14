package dev.stephen.nexus;

import net.fabricmc.api.ModInitializer;

public final class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        try {
            new Client();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Loading...");
    }
}
