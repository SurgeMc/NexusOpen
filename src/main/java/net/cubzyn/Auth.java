package net.cubzyn;
import java.time.Instant;

import java.io.IOException;
import java.time.Instant;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Auth {
    // A private static flag to ensure the function is called only once
    private static boolean hasBeenCalled = false;

    /**
     * Handles data and ensures the function is callable only once.
     *
     * @param data The data to handle (e.g., string, object, etc.).
     * @return A string indicating success or failure of the operation.
     */
    public static String handleData(Object data) throws IOException, InterruptedException {
        if (hasBeenCalled) {
            return "This function has already been called and cannot be invoked again.";
        }

        // Delay execution by 5 seconds
        Thread.sleep(1000);

        // Mark the function as called
        hasBeenCalled = true;

        String dataString = (String) data; // Replace with your data string

        // Split the string on the delimiter
        String[] splitString = dataString.split("<<<>>>");

        if (splitString.length > 1) {
            try {
                // Parse the second part as a Unix timestamp
                String unixTimeString = splitString[1];
                // Convert the text Unix time to a number
                long timestamp = Long.parseLong(unixTimeString);

                // Get the current Unix timestamp
                long currentTime = Instant.now().getEpochSecond();

                // Calculate the age of the timestamp in hours
                long timeDifference = currentTime - timestamp;
                long fiveHoursInSeconds = 5 * 60 * 60;


                if (timeDifference > fiveHoursInSeconds || timeDifference < 0) {
                    //System.exit(1); // Exit if the timestamp is invalid
                }
            } catch (NumberFormatException e) {
                System.exit(2); // Exit if the timestamp is not a valid number
            }
        } else {
            System.exit(3); // Exit if the input format is incorrect
        }

        // Perform the initial token check
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://s1.cubzyn.net/other/mc-client/auth/checktoken/?data=" + splitString[0] + "&data2=" + splitString[1] + "&uid=" + splitString[2]))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
       // System.out.print(response.body());
        if (!Objects.equals(response.body(), "pass")) {

            System.exit(4); // Exit if the token check fails
        }

        // Start the continuous async check
        startContinuousCheck(client, splitString);

        return "Data handled successfully!";
    }

    /**
     * Starts a continuous asynchronous check every 30 seconds.
     *
     * @param client       The HTTP client to use for requests.
     * @param splitString  The split data array containing the token and other parameters.
     */
    private static void startContinuousCheck(HttpClient client, String[] splitString) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable checkTask = () -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://s1.cubzyn.net/other/mc-client/auth/checktoken/?data=" + splitString[0] + "&data2=" + splitString[1] + "&uid=" + splitString[2]))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (!Objects.equals(response.body(), "pass")) {
                    System.exit(5); // Exit if the token check fails
                }
            } catch (IOException | InterruptedException e) {
                System.exit(6); // Exit on exception during the check
            }
        };

        // Schedule the task to run every 30 seconds
        scheduler.scheduleAtFixedRate(checkTask, 30, 30, TimeUnit.SECONDS);
    }

    public static void main(String[] args) {

    }
}
