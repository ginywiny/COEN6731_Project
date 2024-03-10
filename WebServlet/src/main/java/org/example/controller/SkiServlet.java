package org.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;


@WebServlet(value = "skiers")
public class SkiServlet extends HttpServlet {
    // Full endpoint /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getOutputStream().println("Get " + System.currentTimeMillis() + " This is doGet for skiers");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Get incoming request
        String URL = request.getRequestURI();
        String[] URLsplit = URL.split("/");
        System.out.println("URL: " + URL);

        // Initialize response fields
        String resortID, seasonID, dayID, skierID, time = new String();

        // Parse incoming POST request to retrieve all values 
        // Start at 1 to skip the context /skiresortApp and skip 2 to 
        for (int i = 1; i < URLsplit.length; i+=2) {
            String currPath = URLsplit[i];

            if (currPath.equals("skiers")) {
                if (i+1 < URLsplit.length) {
                    resortID = URLsplit[i+1];
                }
                else {
                    response.getOutputStream().println("Invalid API URL request");
                }
            }
            else if (currPath.equals("seasons")) {
                if (i+1 < URLsplit.length) {
                    seasonID = URLsplit[i+1];
                }
                else {
                    response.getOutputStream().println("Invalid API URL request");
                }
            }
            else if (currPath.equals("days")) {
                if (i+1 < URLsplit.length) {
                    dayID = URLsplit[i+1];
                }
                else {
                    response.getOutputStream().println("Invalid API URL request");
                }
            }
            else if (currPath.equals("skiers")) {
                if (i+1 < URLsplit.length) {
                    skierID = URLsplit[i+1];
                }
                else {
                    response.getOutputStream().println("Invalid API URL request");
                }
            }
        }

        // Dummy data return
        response.getOutputStream().println("Post " + System.currentTimeMillis() + " This is doPost for skiers");
    }
}
