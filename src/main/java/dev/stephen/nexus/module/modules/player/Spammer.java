package dev.stephen.nexus.module.modules.player;

import dev.stephen.nexus.event.bus.Listener;
import dev.stephen.nexus.event.bus.annotations.EventLink;
import dev.stephen.nexus.module.Module;
import dev.stephen.nexus.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Spammer extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final String message = "Cubzyn dot net - Register an account!"; // untill i add settings also "dot" to bypass most chats
    private int delay = 20; // untill i add setitingf
    private int tickCounter = 0;

    public Spammer() {
        super("Spammer", "Sends repeated messages", 0, ModuleCategory.PLAYER);
    }

    public void setDelay(int delay) {
        this.delay = Math.max(1, delay); // should work
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        tickCounter = 0;
        super.onDisable();
    }

    private void customTickSystem() {
        if (mc.player == null || mc.world == null) return;

        tickCounter++;
        if (tickCounter >= delay) {
            mc.getNetworkHandler().sendChatMessage(message);
            tickCounter = 0;
        }
    }

    @EventLink
    public final Listener<Object> onGameLoop = event -> {
        customTickSystem();
    };
}
