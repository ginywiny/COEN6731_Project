package com.example;

public class SkiLiftRideEvent {
    private int skierID;
    private int resortID;
    private int liftID;
    private int seasonID;
    private int dayID;
    private int time; 

    SkiLiftRideEvent(int skierID, int resortID, int liftID, int seasonID, int dayID, int time) {
        this.skierID = skierID;
        this.resortID = resortID;
        this.seasonID = seasonID;
        this.dayID = dayID;
        this.time = time;
    }
}