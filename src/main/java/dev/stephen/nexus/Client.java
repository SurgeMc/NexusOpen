package dev.stephen.nexus;
import net.cubzyn.Auth;

import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.stephen.nexus.anticheat.AntiCheatManager;
import dev.stephen.nexus.commands.CommandManager;
import dev.stephen.nexus.config.ConfigManager;
import dev.stephen.nexus.event.EventManager;
import dev.stephen.nexus.module.ModuleManager;
import dev.stephen.nexus.utils.font.FontManager;
import dev.stephen.nexus.utils.mc.DelayUtil;
import dev.stephen.nexus.utils.render.notifications.NotificationManager;
import dev.stephen.nexus.utils.rotation.manager.RotationManager;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;


@Getter
public final class Client {
    public static MinecraftClient mc;
    public static Client INSTANCE;

    private final EventManager eventManager;
    private final RotationManager rotationManager;
    private final ModuleManager moduleManager;

    private final NotificationManager notificationManager;
    private final AntiCheatManager antiCheatManager;

    private final FontManager fontManager;
    private final ConfigManager configManager;
    private final CommandManager commandManager;

    private final DelayUtil delayUtil;
    public static String verison = "1.0.0";

    public void startWebServer() throws Exception {
        // Create a CompletableFuture to block execution until the GET request is received
        CompletableFuture<String> dataFuture = new CompletableFuture<>();

        // Create an HTTP server on a random port
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();

        // Define the request handler
        server.createContext("/", exchange -> {
            if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getQuery() != null) {
                String query = exchange.getRequestURI().getQuery();
                if (query.startsWith("data=")) {
                    String data = query.substring(5); // Extract the data parameter
                    dataFuture.complete(data); // Complete the future

                    // Create the HTML response
                    String response = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Thanks</title>
        </head>
        <body>
            <p>Thanks. You can now go back to the application.</p>
            <script>
                window.close();
            </script>
        </body>
        </html>
        """;

                    // Set the response headers
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.getBytes().length);

                    // Write the response body
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();

                    // Stop the server after receiving the request
                    server.stop(0);
                    return;
                }

            }
            String response = "Invalid request";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });

        // Start the server
        ExecutorService executor = Executors.newSingleThreadExecutor();
        server.setExecutor(executor);
        server.start();

        // Open the URL in the default browser
        String url = "https://cubzyn.net/other/mc-client/auth/startclient.php?port=" + port;
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
        } else {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else {
                Runtime.getRuntime().exec(new String[]{"xdg-open", url});
            }
        }


        // Wait for the data to be received
        String receivedData = dataFuture.get();
        net.cubzyn.Auth.handleData(receivedData);


        // Clean up
        executor.shutdown();
    }
    public Client() throws Exception {
        startWebServer(); // Start the web server and freeze execution until the request is received

        INSTANCE = this;
        mc = MinecraftClient.getInstance();

        eventManager = new EventManager();
        notificationManager = new NotificationManager();
        antiCheatManager = new AntiCheatManager();
        rotationManager = new RotationManager();
        commandManager = new CommandManager();
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();

        fontManager = new FontManager();
        delayUtil = new DelayUtil();

        eventManager.subscribe(notificationManager);
        eventManager.subscribe(rotationManager);
        eventManager.subscribe(delayUtil);
    }
}
