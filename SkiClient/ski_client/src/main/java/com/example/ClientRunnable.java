package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.gson.Gson;

public class ClientRunnable extends Thread {

    private static final String ENTRYURL = "http://127.0.0.1:8080"; // Constant IP:Port
    private static final String CONTEXTURL = "/skiresortApp"; // Constant IP:Port
    private static final AtomicInteger atomicSuccessCount = new AtomicInteger(0);
    private static final AtomicInteger atomicRequestCount = new AtomicInteger(0);
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

        List<Long> startTimeList = Collections.synchronizedList(new ArrayList<Long>());
        List<Long> endTimeList = Collections.synchronizedList(new ArrayList<Long>());

        // Pre total time
        Long startTime = System.currentTimeMillis();

        // Step 2. Perform requests to server concurrently using ExecutorService
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
                        // Pre-POST timestamp
                        long start = System.currentTimeMillis();
                        startTimeList.add(start);

                        // Send POST
                        atomicRequestCount.getAndIncrement();
                        HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                        // Post-PORT timestamp
                        long end = System.currentTimeMillis();
                        endTimeList.add(end);

                        // Handing 5xx and 4xx (webserver or servlet error)
                        if (response.statusCode() >= 400 || response.statusCode() >= 500) {
                            System.out.println("Failed initial request: Response code " + response.statusCode());
                            System.out.println("Retrying request 5 times...");
                            // Retry 5 more times
                            for (int k = 0; k < requestRetryCount; k++) {
                                response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                                atomicRequestCount.getAndIncrement();
                                if (response.statusCode() == 200 || response.statusCode() == 201) {
                                    break;
                                }
                            }
                        }

                        if (response.statusCode() == 200 || response.statusCode() == 201) {
                            System.out.print("Status code: " + response.statusCode() + " Response body: " + response.body());
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

        // Post total time
        Long endTime = System.currentTimeMillis();

        // Shut down executor threads gracefully
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("----------------------------------------------------");
        System.out.println("Client Requesting Complete!");
        System.out.println("----------------------------------------------------");
        System.out.println();


        // Step 3: Perform performance calculations
        // Total Response Time, ie. Walltime
        Long totalTime = endTime - startTime;

        // POST Latency List
        List<Long> latencyTimeList = new ArrayList<>();
        for (int i = 0; i < startTimeList.size(); i++) {
            long latency = endTimeList.get(i) - startTimeList.get(i);
            latencyTimeList.add(latency);
        }

        // Median response time
        Long medianResponseTime = 0L;
        Collections.sort(latencyTimeList);
        if (latencyTimeList.size() >=2 && latencyTimeList.size() % 2 == 0) {
            int left = (latencyTimeList.size() / 2) - 1;
            int right = latencyTimeList.size() / 2;
            medianResponseTime = latencyTimeList.get((right - left) / 2);
        }
        else {
            int middle = (int) Math.ceil(latencyTimeList.size() / 2);
            medianResponseTime = latencyTimeList.get(middle);
        }

        // Mean Response Time
        Long meanResponseTime = 0L;
        for (int i = 0; i < latencyTimeList.size(); i++) {
            meanResponseTime += latencyTimeList.get(i);
        }
        meanResponseTime /= latencyTimeList.size();

        // Min Response Time
        Long minResponseTime = latencyTimeList.get(0);

        // Max Response Time
        Long maxResponseTime = latencyTimeList.get(latencyTimeList.size()-1);

        // Throughput
        int requests = atomicRequestCount.get();
        double totalTimeSeconds = (totalTime.doubleValue() / 1000);
        int throughput = (int)(requests / totalTimeSeconds); // Must be in SECONDS not MILLISECONDS!

        // 99th Percentile
        Long percentile99 = 0L;
        // Calculation source https://goodcalculators.com/percentile-calculator/
        double percentileIndex = Math.ceil((latencyTimeList.size() - 1) * 0.99);
        percentile99 = latencyTimeList.get((int)percentileIndex);

        System.out.println("----------------Profiling Performance---------------");
        System.out.println("Total Requests: " + requests);
        System.out.println("Start Time (ms): " + startTime);
        System.out.println("End Time (ms): " + endTime);
        System.out.println("Wall Time (ms): " + totalTime);
        System.out.println("Mean Response Time (ms): " + meanResponseTime);
        System.out.println("Median Response Time (ms): " + medianResponseTime);
        System.out.println("Throughput (req/s): " + throughput);
        System.out.println("99th Percentile (ms): " + percentile99);
        System.out.println("Minimum Response Time (ms): " + minResponseTime);
        System.out.println("Maximum Response Time (ms): " + maxResponseTime);
        System.out.println("----------------------------------------------------");

    }
}
