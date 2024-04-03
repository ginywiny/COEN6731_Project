package com.example;

public class SkiClient {

    public static void main(String[] args) {

        // ClientRunnable client = new ClientRunnable(1, 1, 1, 5);
        // ClientRunnable client = new ClientRunnable(10, 10, 10, 5);
        ClientRunnable client = new ClientRunnable(32, 1000, 
        10000, 5);
        client.run();

    }
}