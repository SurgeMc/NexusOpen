package dev.stephen.nexus.module.modules.client;

import dev.stephen.nexus.module.Module;
import dev.stephen.nexus.module.ModuleCategory;

public class RefreshClientData extends Module {

    public RefreshClientData() {
        super("RefreshClientData", "Refreshes your client data. (e.g. username,id and status)", 0, ModuleCategory.CLIENT);

    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public void onEnable() {
        try {
            net.cubzyn.Auth.startWebServer();

            onDisable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
