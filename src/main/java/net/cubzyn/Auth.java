package net.cubzyn;

import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.net.httpserver.HttpServer;
import com.sun.jna.platform.win32.COM.util.Factory;

import com.sun.jna.platform.win32.COM.util.annotation.ComObject;
import com.sun.jna.platform.win32.COM.util.annotation.ComProperty;

import java.awt.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.*;

public class Auth {
    private static String[] splitString;
    private static boolean isContinuousCheckRunning = false;

    /**
     * Handles data by updating tokens and usernames or performing initialization logic.
     *
     * @param data The data to handle.
     * @return A string indicating success or failure of the operation.
     */
    public static String handleData(Object data) throws IOException, InterruptedException {
        String dataString = (String) data;

        // Split the string on the delimiter
        splitString = dataString.split("<<<>>>");

        if (splitString.length > 1) {
            try {
                String unixTimeString = splitString[1];
                if (unixTimeString == null || unixTimeString.isEmpty()) {
                    System.err.println("Timestamp is missing or empty.");
                    System.exit(3);
                }

                long timestamp = Long.parseLong(unixTimeString);
                ZonedDateTime nowInUK = ZonedDateTime.now(ZoneId.of("Europe/London"));
                long currentTime = nowInUK.toEpochSecond();
                long timeDifference = currentTime - timestamp;
                long fiveHoursInSeconds = 5 * 60 * 60;
                long gracePeriod = 60;

                if (timeDifference > fiveHoursInSeconds + gracePeriod || timeDifference < -gracePeriod) {
                    System.err.println("Invalid timestamp: outside acceptable range.");
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing timestamp: " + e.getMessage());
                System.exit(2);
            }
        } else {
            System.exit(3);
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://s1.cubzyn.net/other/mc-client/auth/checktoken/?data=" + splitString[0] + "&data2=" + splitString[1] + "&uid=" + splitString[2]))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (!Objects.equals(response.body(), "pass")) {
            System.exit(4);
        }
        // Show a Windows toast notification after successful authentication
        showWindowsToast("Hello, " + splitString[3] + " (" + splitString[2] + ")");
        if (!isContinuousCheckRunning) {
            startContinuousCheck(client);
            isContinuousCheckRunning = true;
        }



        return "Data handled successfully!";
    }

    /**
     * Starts a continuous asynchronous check every 30 seconds.
     *
     * @param client The HTTP client to use for requests.
     */
    private static void startContinuousCheck(HttpClient client) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable checkTask = () -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://s1.cubzyn.net/other/mc-client/auth/checktoken/?data=" + splitString[0] + "&data2=" + splitString[1] + "&uid=" + splitString[2]))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (!Objects.equals(response.body(), "pass")) {
                    System.exit(5);
                }
            } catch (IOException | InterruptedException e) {
                System.exit(6);
            }
        };

        scheduler.scheduleAtFixedRate(checkTask, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Shows a Windows toast notification with a given message.
     *
     * @param message The message to display in the notification.
     */
    private static void showWindowsToast(String message) {
        try {
            String command = String.format(
                    "powershell.exe -Command \"[Windows.UI.Notifications.ToastNotificationManager, Windows.UI.Notifications, ContentType = WindowsRuntime] | Out-Null; $template = [Windows.UI.Notifications.ToastTemplateType]::ToastText01; $toastXml = [Windows.UI.Notifications.ToastNotificationManager]::GetTemplateContent($template); $toastText = $toastXml.GetElementsByTagName('text')[0]; $toastText.AppendChild($toastXml.CreateTextNode('%s')) | Out-Null; $toast = [Windows.UI.Notifications.ToastNotification]::new($toastXml); $notifier = [Windows.UI.Notifications.ToastNotificationManager]::CreateToastNotifier('Cubzyn'); $notifier.Show($toast);\"",
                    message
            );
            Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            System.err.println("Failed to show toast notification: " + e.getMessage());
        }
    }


    /**
     * Returns the UID from the split data.
     *
     * @return The UID string if available, or "0" if not.
     */
    public static String getCubzynUid() {
        if (splitString != null && splitString.length > 2) {
            return splitString[2];
        }
        return "0";
    }

    public static String getCubzynUsername() {
        if (splitString != null && splitString.length > 3) {
            return splitString[3];
        }
        return "username";
    }

    public static void startWebServer() throws Exception {
        CompletableFuture<String> dataFuture = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();

        server.createContext("/", exchange -> {
            if ("GET".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getQuery() != null) {
                String query = exchange.getRequestURI().getQuery();
                if (query.startsWith("data=")) {
                    String data = query.substring(5);
                    dataFuture.complete(data);

                    String response = """
                            <!DOCTYPE html>
                            <html lang="en">
                            <head>
                                <meta charset="UTF-8">
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

                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                    server.stop(0);
                    return;
                }
            }
            String response = "Invalid request";
            exchange.sendResponseHeaders(400, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        server.setExecutor(executor);
        server.start();

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
        String receivedData = dataFuture.get();
        handleData(receivedData);

        executor.shutdown();
    }
}

