package dev.stephen.nexus.module.modules.client;

import dev.stephen.nexus.module.Module;
import dev.stephen.nexus.module.ModuleCategory;
import dev.stephen.nexus.module.setting.impl.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;
import java.util.UUID;

public class CrackedName extends Module {
    public static final StringSetting usernameSetting = new StringSetting("Username", "cracked");

    private Session oldSession;

    public CrackedName() {
        super("Session Changer", "Changes displayed name to the one you typed", 0, ModuleCategory.CLIENT);
        addSettings(usernameSetting);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            MinecraftClient client = MinecraftClient.getInstance();

            oldSession = client.getSession();

            String chosenName = usernameSetting.getValue().trim();
            if (chosenName.isEmpty()) {
                chosenName = "cracked";
            }

            Session customSession = new Session(
                    chosenName,
                    UUID.randomUUID(),
                    "token123131231231",
                    Optional.empty(),
                    Optional.empty(),
                    Session.AccountType.LEGACY
            );

            VarHandle sessionHandle = MethodHandles.lookup().in(MinecraftClient.class)
                    .unreflectVarHandle(MinecraftClient.class.getDeclaredField("session"));

            sessionHandle.set(client, customSession);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (oldSession != null) {
            try {
                MinecraftClient client = MinecraftClient.getInstance();
                VarHandle sessionHandle = MethodHandles.lookup().in(MinecraftClient.class)
                        .unreflectVarHandle(MinecraftClient.class.getDeclaredField("session"));

                sessionHandle.set(client, oldSession);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
