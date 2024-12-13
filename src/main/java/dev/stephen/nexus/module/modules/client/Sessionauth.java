package dev.stephen.nexus.module.modules.client;

import dev.stephen.nexus.Client;
import dev.stephen.nexus.event.bus.Listener;
import dev.stephen.nexus.event.bus.annotations.EventLink;
import dev.stephen.nexus.event.impl.player.EventTickPre;
import dev.stephen.nexus.module.Module;
import dev.stephen.nexus.module.ModuleCategory;
import dev.stephen.nexus.module.setting.impl.BooleanSetting;
import dev.stephen.nexus.module.setting.impl.StringSetting;
import dev.stephen.nexus.utils.mc.ChatFormatting;
import dev.stephen.nexus.utils.mc.ChatUtils;
import dev.stephen.nexus.utils.mc.PacketUtils;
import lombok.Getter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sessionauth extends Module {
    // Module Settings
    public static final StringSetting sessionIDSetting = new StringSetting("Session ID", "");
    public static final BooleanSetting loginSetting = new BooleanSetting("Login", true);
    public static final BooleanSetting displaySessionSetting = new BooleanSetting("Display Session", false);
    public static final StringSetting skinURLSetting = new StringSetting("Skin URL", "");
    public static final BooleanSetting changeSkinSetting = new BooleanSetting("Change Skin", false);
    public static final StringSetting newNameSetting = new StringSetting("New Name", "");
    public static final BooleanSetting changeNameSetting = new BooleanSetting("Change Name", false);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Sessionauth() {
        super("Catauth", "Legit pasted from a rise script", 0, ModuleCategory.CLIENT);
        addSettings(sessionIDSetting, loginSetting, displaySessionSetting, skinURLSetting, changeSkinSetting, newNameSetting, changeNameSetting);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ChatUtils.addMessageToChat(ChatFormatting.GREEN + "Catauth module enabled.");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        executor.shutdownNow();
        ChatUtils.addMessageToChat(ChatFormatting.RED + "Catauth module disabled.");
    }

    @EventLink
    public final Listener<EventTickPre> onTickListener = event -> {
        if (loginSetting.getValue()) {
            String sessionID = sessionIDSetting.getValue().trim();
            if (!sessionID.isEmpty()) {
                validateSession(sessionID);
                loginSetting.setValue(false);
            } else {
                ChatUtils.addMessageToChat(ChatFormatting.RED + "Session ID is empty.");
            }
        }

        if (displaySessionSetting.getValue()) {
            String currentSession = getSessionID();
            if (currentSession != null) {
                ChatUtils.addMessageToChat(ChatFormatting.YELLOW + "Current Session ID: " + currentSession);
            } else {
                ChatUtils.addMessageToChat(ChatFormatting.RED + "No active session.");
            }
            displaySessionSetting.setValue(false);
        }

        if (changeSkinSetting.getValue()) {
            String skinURL = skinURLSetting.getValue().trim();
            String sessionID = getSessionID();
            if (!skinURL.isEmpty() && sessionID != null) {
                changeSkin(skinURL, sessionID);
            } else {
                ChatUtils.addMessageToChat(ChatFormatting.RED + "Skin URL is empty or no active session.");
            }
            changeSkinSetting.setValue(false);
        }

        if (changeNameSetting.getValue()) {
            String newName = newNameSetting.getValue().trim();
            String sessionID = getSessionID();
            if (!newName.isEmpty() && sessionID != null) {
                changeName(newName, sessionID);
            } else {
                ChatUtils.addMessageToChat(ChatFormatting.RED + "New name is empty or no active session.");
            }
            changeNameSetting.setValue(false);
        }
    };


    private String getSessionID() {
        try {
            MinecraftClient client = MinecraftClient.getInstance();

        } catch (Exception e) {
            ChatUtils.addMessageToChat(ChatFormatting.RED + "Error retrieving session ID: " + e.getMessage());
            return null;
        }
        return null;
    }


    private void validateSession(String sessionID) {
        executor.submit(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet("https://api.minecraftservices.com/minecraft/profile");
                request.setHeader("Authorization", "Bearer " + sessionID);

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
                        String username = jsonObject.get("name").getAsString();
                        String uuid = jsonObject.get("id").getAsString();
                        setSession(new Session(username, UUID.fromString(uuid), sessionID, Session.AccountType.MOJANG));
                        ChatUtils.addMessageToChat(ChatFormatting.GREEN + "Logged in as: " + username);
                        ChatUtils.addMessageToChat(ChatFormatting.GRAY + "Session validated for user: " + username);
                    } else {
                        ChatUtils.addMessageToChat(ChatFormatting.RED + "Invalid session.");
                        ChatUtils.addMessageToChat(ChatFormatting.RED + "Failed to validate session. Status code: " + statusCode);
                    }
                }
            } catch (Exception e) {
                ChatUtils.addMessageToChat(ChatFormatting.RED + "Error during validation: " + e.getMessage());
                ChatUtils.addMessageToChat(ChatFormatting.RED + "Error validating session.");
            }
        });
    }


    private void setSession(Session newSession) {
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            VarHandle sessionHandle = MethodHandles.lookup().in(MinecraftClient.class)
                    .unreflectVarHandle(MinecraftClient.class.getDeclaredField("session"));

            sessionHandle.set(client, newSession);
            ChatUtils.addMessageToChat(ChatFormatting.GREEN + "Logged in as " + newSession.getUsername());
            ChatUtils.addMessageToChat(ChatFormatting.GRAY + "Session changed to: " + newSession.getUsername());
        } catch (Exception e) {
            ChatUtils.addMessageToChat(ChatFormatting.RED + "Error setting session: " + e.getMessage());
            ChatUtils.addMessageToChat(ChatFormatting.RED + "Error setting session.");
        }
    }


    private void changeSkin(String skinURL, String sessionID) {
        executor.submit(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost request = new HttpPost("https://api.minecraftservices.com/minecraft/profile/skins");
                request.setHeader("Authorization", "Bearer " + sessionID);
                request.setHeader("Content-Type", "application/json");

                JsonObject skinData = new JsonObject();
                skinData.addProperty("variant", "classic");
                skinData.addProperty("url", skinURL);

                StringEntity entity = new StringEntity(skinData.toString());
                request.setEntity(entity);

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        ChatUtils.addMessageToChat(ChatFormatting.GREEN + "Skin changed successfully!");
                    } else if (statusCode == 429) {
                        ChatUtils.addMessageToChat(ChatFormatting.RED + "Too many requests!");
                    } else if (statusCode == 401) {
                        ChatUtils.addMessageToChat(ChatFormatting.RED + "Invalid Session!");
                    } else {
                        ChatUtils.addMessageToChat(ChatFormatting.RED + "Failed to change skin. Status code: " + statusCode);
                    }
                }
            } catch (Exception e) {
                ChatUtils.addMessageToChat(ChatFormatting.RED + "Error changing skin: " + e.getMessage());
            }
        });
    }


    private void changeName(String newName, String sessionID) {
        executor.submit(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPut request = new HttpPut("https://api.minecraftservices.com/minecraft/profile/name/" + newName);
                request.setHeader("Authorization", "Bearer " + sessionID);

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        ChatUtils.addMessageToChat(ChatFormatting.GREEN + "Successfully changed name to " + newName);
                    } else if (statusCode == 429) {
                        ChatUtils.addMessageToChat(ChatFormatting.RED + "Too many requests!");
                    } else if (statusCode == 400) {
                        ChatUtils.addMessageToChat(ChatFormatting.RED + "Invalid name!");
                    } else if (statusCode == 401) {
                        ChatUtils.addMessageToChat(ChatFormatting.RED + "Invalid Session!");
                    } else if (statusCode == 403) {
                        ChatUtils.addMessageToChat(ChatFormatting.RED + "Name is unavailable or player already changed name in the last 35 days.");
                    } else {
                        ChatUtils.addMessageToChat(ChatFormatting.RED + "Failed to change name. Status code: " + statusCode);
                    }
                }
            } catch (Exception e) {
                ChatUtils.addMessageToChat(ChatFormatting.RED + "Error changing name: " + e.getMessage());
            }
        });
    }

    @Getter
    public static class Session {
        private String username;
        private UUID uuid;
        private String token;
        private Optional<Object> legacyProfile;
        private Optional<Object> profilePublicKey;
        private AccountType accountType;

        public Session(String username, UUID uuid, String token, AccountType accountType) {
            this.username = username;
            this.uuid = uuid;
            this.token = token;
            this.legacyProfile = Optional.empty();
            this.profilePublicKey = Optional.empty();
            this.accountType = accountType;
        }

        public enum AccountType {
            LEGACY,
            MOJANG
        }
    }
}
