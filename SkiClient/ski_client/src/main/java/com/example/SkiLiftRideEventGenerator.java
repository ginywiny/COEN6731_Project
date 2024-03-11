package com.example;

import java.util.Random;

public class SkiLiftRideEventGenerator implements Runnable{

    private SkiLiftRideEvent lift;
    public SkiLiftRideEventGenerator() {}

    public SkiLiftRideEvent generateRandomSkiLiftRide() {
        Random rand = new Random();
        int skierID = 1 + rand.nextInt(100000); // [1, 100000]
        int resortID = 1 + rand.nextInt(10); // [1, 10]
        int liftID = 1 + rand.nextInt(40); // [1, 40]
        int seasonID = 2022; // 2022
        int dayID = 1; // 1
        int time = 1 + rand.nextInt(360); // [1, 360] 
        SkiLiftRideEvent lift = new SkiLiftRideEvent(skierID, resortID, liftID, seasonID, dayID, time);
        return lift;
    }

    @Override
    public void run() {
        this.lift = generateRandomSkiLiftRide();
    }
}
