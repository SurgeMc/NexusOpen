package dev.stephen.nexus.module.modules.client;

import dev.stephen.nexus.Client;
import dev.stephen.nexus.event.bus.Listener;
import dev.stephen.nexus.event.bus.annotations.EventLink;
import dev.stephen.nexus.event.impl.input.EventMovementInput;
import dev.stephen.nexus.event.impl.network.EventPacket;
import dev.stephen.nexus.event.impl.player.EventSilentRotation;
import dev.stephen.nexus.event.impl.player.EventTickPre;
import dev.stephen.nexus.event.types.TransferOrder;
import dev.stephen.nexus.mixin.accesors.KeyBindingAccessor;
import dev.stephen.nexus.module.Module;
import dev.stephen.nexus.module.ModuleCategory;
import dev.stephen.nexus.module.modules.other.Disabler;
import dev.stephen.nexus.module.setting.impl.BooleanSetting;
import dev.stephen.nexus.module.setting.impl.StringSetting;
import dev.stephen.nexus.utils.mc.ChatUtils;
import dev.stephen.nexus.utils.mc.PacketUtils;
import lombok.Getter;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;
import java.util.UUID;
import java.util.Random;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.session.Session;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CrackedName extends Module {
    public static final StringSetting usernameSetting = new StringSetting("Username", "Cubzyn_net_ACC");
    public static final BooleanSetting randomUsernameSetting = new BooleanSetting("Random Username", false); // new thigny

    private net.minecraft.client.session.Session oldSession;

    public CrackedName() {
        super("Session Changer", "Changes displayed name to the one you typed", 0, ModuleCategory.CLIENT);
        addSettings(usernameSetting, randomUsernameSetting);
    }

    public static void firstLoad() throws NoSuchFieldException, IllegalAccessException {

        MinecraftClient client = MinecraftClient.getInstance();

        String chosenName;

        chosenName = "Cubzyn_net_ACC";


        net.minecraft.client.session.Session customSession = new net.minecraft.client.session.Session(
                chosenName,
                UUID.randomUUID(),
                "tokenCubzynToken00000000",
                Optional.empty(),
                Optional.empty(),
                net.minecraft.client.session.Session.AccountType.LEGACY
        );


        VarHandle sessionHandle = MethodHandles.lookup().in(MinecraftClient.class)
                .unreflectVarHandle(MinecraftClient.class.getDeclaredField("session"));

        sessionHandle.set(client, customSession);

    }

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            MinecraftClient client = MinecraftClient.getInstance();

            oldSession = client.getSession();

            String chosenName;
            if (randomUsernameSetting.getValue()) {
                chosenName = generateRandomUsername(5); // 8 lengh
                usernameSetting.setValue(chosenName);
            } else {
                chosenName = usernameSetting.getValue().trim();
                if (chosenName.isEmpty()) {
                    chosenName = "Cubzyn_net_ACC";
                }
            }

            net.minecraft.client.session.Session customSession = new net.minecraft.client.session.Session(
                    chosenName,
                    UUID.randomUUID(),
                    "tokenCubzynToken00000000",
                    Optional.empty(),
                    Optional.empty(),
                    net.minecraft.client.session.Session.AccountType.LEGACY
            );


            VarHandle sessionHandle = MethodHandles.lookup().in(MinecraftClient.class)
                    .unreflectVarHandle(MinecraftClient.class.getDeclaredField("session"));

            sessionHandle.set(client, customSession);

            ChatUtils.addMessageToChat("Session changed to: " + chosenName);

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
                ChatUtils.addMessageToChat("Session reverted to: " + oldSession.getUsername());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String generateRandomUsername(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String name = "Cubzyn_net_"+sb.toString();
        return name;
    }

    @Getter
    public static class Session {
        private String username;
        private UUID uuid;
        private String token;
        private Optional<Object> legacyProfile;
        private Optional<Object> profilePublicKey;
        private AccountType accountType;

        public Session(String username, UUID uuid, String token, Optional<Object> legacyProfile,
                       Optional<Object> profilePublicKey, AccountType accountType) {
            this.username = username;
            this.uuid = uuid;
            this.token = token;
            this.legacyProfile = legacyProfile;
            this.profilePublicKey = profilePublicKey;
            this.accountType = accountType;
        }

        public enum AccountType {
            LEGACY,
            MOJANG,
            MICROSOFT
        }
    }
}
