package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ClientRunnable extends Thread {

    private static final String ENTRYURL = "http://127.0.0.1:8080"; // Constant IP:Port
    private static final String CONTEXTURL = "/skiresortApp"; // Constant IP:Port
    private static ExecutorService executorService;

    private final int threadCount;
    private final int threadRequests;
    
    // Initialize clients with threads and number of requests per thread    
    public ClientRunnable(int threadCount, int threadRequests) {
        executorService = Executors.newFixedThreadPool(threadCount);
        this.threadCount = threadCount;
        this.threadRequests = threadRequests;

        System.out.println("----------------------------------------------------");
        System.out.println("Starting client...");
        System.out.println("----------------------------------------------------");
    }

    // Perform API connection test
    public static boolean testConnectivity(HttpClient client) {

        // Specify entry point
        String endpoint = ENTRYURL + CONTEXTURL + "/skiers";
        // String endpoint = ENTRYURL + CONTEXTURL + "/skiers/{1}/seasons/{2}/days/{3}/skiers/{4}";
        System.out.println("Invoking: " + endpoint);

        // Create request to ping for connection
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            // Send the request and get the response
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // Print the response status code and body
            System.out.println();
            System.out.println("-------------Performing Connection Test-------------");
            System.out.println("Response code: " + response.statusCode());
            System.out.print("Response body: " + response.body());
            System.out.println("----------------------------------------------------");
            System.out.println();


            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("Client API connected...");
                return true;
            }
            else {
                System.out.println("Error bad response, no API connectivity: " + response.statusCode());
                return false;
            }

        } catch (Exception e) {
            System.err.println("Error no connectivity: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void run() {

        // Step 1: Create client and test server connection
		HttpClient client = HttpClient.newHttpClient();
        if (testConnectivity(client)) {
            System.out.println("Client connected.");
        }
        else {
            return;
        }

        // Shut down executor threads gracefully
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
