package dev.stephen.nexus.module.modules.player;

import dev.stephen.nexus.module.Module;
import dev.stephen.nexus.module.ModuleCategory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Spammer extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final String message = "Cubzyn dot net - Register an account!";
    private int delay = 20;
    private int tickCounter = 0;

    public Spammer() {
        super("Spammer", "Sends repeated messages", 0, ModuleCategory.PLAYER);

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }


    public void setDelay(int delay) {
        this.delay = Math.max(1, delay);
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

    private void onClientTick(MinecraftClient client) {
        if (!this.isEnabled()) return;
        customTickSystem();
    }


    private void customTickSystem() {
        if (mc.player == null || mc.world == null) return;

        tickCounter++;
        if (tickCounter >= delay) {
            sendMessage();
            tickCounter = 0;
        }
    }

    private void sendMessage() {
        if (mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatMessage(message);
        }
    }
}
