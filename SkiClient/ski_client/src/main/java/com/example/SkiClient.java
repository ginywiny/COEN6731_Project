package com.example;

public class SkiClient {

    public static void main(String[] args) {

        ClientRunnable client = new ClientRunnable(1, 1);
        // ClientRunnable client = new ClientRunnable(32, 1000);
        client.run();

    }
}