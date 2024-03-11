package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ClientRunnable extends Thread {

    private static final String ENTRYURL = "http://127.0.0.1:8080"; // Constant IP:Port
    private static final String CONTEXTURL = "/skiresortApp"; // Constant IP:Port
    private static final AtomicInteger atomicSuccessCount = new AtomicInteger(0);
    private static Gson gson = new Gson();
    private static ExecutorService executorService;

    private final int threadCount;
    private final int threadRequests;
    private final int wantedUploads;
    private final int requestRetryCount;

    
    // Initialize clients with threads and number of requests per thread    
    public ClientRunnable(int threadCount, int threadRequests, int wantedUploads, int requestRetryCount) {
        executorService = Executors.newFixedThreadPool(threadCount);
        this.threadCount = threadCount;
        this.threadRequests = threadRequests;
        this.wantedUploads = wantedUploads;
        this.requestRetryCount = requestRetryCount;

        System.out.println("----------------------------------------------------");
        System.out.println("Starting client...");
        System.out.println("----------------------------------------------------");
    }

    // Perform API connection test
    public static boolean testConnectivity(HttpClient client) {

        // Specify entry point
        // String endpoint = ENTRYURL + CONTEXTURL + "/skiers/1/seasons/2022/days/1/skiers/4";
        String endpoint = ENTRYURL + CONTEXTURL + "/skiers";

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
            System.out.println("Initial test invocation: " + endpoint);
            System.out.println("Response code: " + response.statusCode());
            System.out.print("Response body: " + response.body());
            System.out.println("----------------------------------------------------");
            System.out.println();


            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("Client connected to server...");
                System.out.println("----------------------------------------------------");
                System.out.println();
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

        // Step 1: Create client and test initial server connection (not done for multiple threads)
		HttpClient client = HttpClient.newHttpClient();
        // End program if no initial server connection
        if (!testConnectivity(client)) {
            return;
        }

        // 2. Perform requests to server concurrently using ExecutorService
        for (int i = 0; i < threadCount; i++) {

            // Create ExecutorService RequestTask in lambda
            executorService.submit(()-> {
                SkiLiftRideEventGenerator eventGenerator = new SkiLiftRideEventGenerator();
                
                for (int j = 0; j < threadRequests; j++) {
                    // Generate random skier lift ride ticket
                    SkiLiftRideEvent lift = eventGenerator.generateRandomSkiLiftRide();

                    // Format /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
                    String parametersURL = String.format("/skiers/%d/seasons/%d/days/%d/skiers/%d", 
                                            lift.getResortID(), lift.getSeasonID(), lift.getDayID(), lift.getSkierID());
                    String endpoint = ENTRYURL + CONTEXTURL + parametersURL;
                    
                    // Convert SkiLiftRideEvent lift ricket to json
                    String requestBody = gson.toJson(lift);

                    // Create request to send generated data
                    System.out.println("Invoking: " + endpoint);
                    HttpRequest httpRequest = HttpRequest.newBuilder()
                            .uri(URI.create(endpoint))
                            .header("Content-Type", "application/json")
                            .POST(BodyPublishers.ofString(requestBody))
                            .build();
            
                    // Send the request and check whether it succeeded or failed
                    try {
                        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                        // Handing 5xx and 4xx (webserver or servlet error)
                        if (response.statusCode() >= 400 || response.statusCode() >= 500) {
                            System.out.println("Failed initial request: Response code " + response.statusCode());
                            System.out.println("Retrying request 5 times...");
                            // Retry 5 more times
                            for (int k = 0; k < requestRetryCount; k++) {
                                response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                                if (response.statusCode() == 200 || response.statusCode() == 201) {
                                    break;
                                }
                            }
                        }

                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            atomicSuccessCount.incrementAndGet(); // Increment count of successful POSTs
                            if (atomicSuccessCount.get() >= wantedUploads) {
                                System.out.println("----------------------------------------------------");
                                System.out.println("Total uploads reached!");
                                System.out.println("----------------------------------------------------");
                                executorService.shutdown();
                                break;
                            }
                        }
                        else {
                            System.out.println("Failed request: Response code " + response.statusCode());
                        }

                    } catch (Exception e) {
                        System.err.println("Error request failed: " + e.getMessage());
                    }


                }
            });
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
