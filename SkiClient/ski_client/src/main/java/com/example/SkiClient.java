package com.example;

public class SkiClient {

    public static void main(String[] args) {

        ClientRunnable client = new ClientRunnable(32);
        client.run();

    }
}