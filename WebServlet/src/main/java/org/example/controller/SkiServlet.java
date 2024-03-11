package org.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@WebServlet(value = "skiers/*")
public class SkiServlet extends HttpServlet {
    // Full endpoint /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}

    public static boolean verifyRequest(String resortID, String seasonID, String dayID, String skierID, HttpServletResponse response) throws IOException {
        int valResortID = Integer.parseInt(resortID);
        int valSeasonID = Integer.parseInt(seasonID);
        int valdayID = Integer.parseInt(dayID);
        int valskierID = Integer.parseInt(skierID);
        boolean flag = true;

        try {
            if (valResortID < 1 || valResortID > 10) {
                response.getOutputStream().println("Invalid resortID, must be between [1, 10]");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                flag = false;
            }
            if (valSeasonID != 2022 ) {
                response.getOutputStream().println("Invalid seasonID, must be 2022");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                flag = false;
            }
            if (valdayID != 1 ) {
                response.getOutputStream().println("Invalid dayID, must be 1");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                flag = false;
            }
            if (valskierID < 1 || valskierID > 100000) {
                response.getOutputStream().println("Invalid skierID, must be between [1, 100000]");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                flag = false;
            }
        } catch (IOException e) {
            throw new IOException("Invalid API request.", e);
        }
        
        return flag;
    }

    public static boolean verifyLiftTicket(SkiLiftRideEvent lift, HttpServletResponse response) throws IOException {

        boolean flag = true;

        if (lift.getResortID() < 1 || lift.getResortID() > 10) {
            response.getOutputStream().println("Invalid resortID, must be between [1, 10]");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            flag = false;
        }
        if (lift.getSeasonID() != 2022 ) {
            response.getOutputStream().println("Invalid seasonID, must be 2022");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            flag = false;
        }
        if (lift.getDayID() != 1 ) {
            response.getOutputStream().println("Invalid dayID, must be 1");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            flag = false;
        }
        if (lift.getSkierID() < 1 || lift.getSkierID() > 100000) {
            response.getOutputStream().println("Invalid skierID, must be between [1, 100000]");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            flag = false;
        }
        if (lift.getLiftID() < 1 || lift.getLiftID() > 40) {
            response.getOutputStream().println("Invalid liftID, must be between [1, 40]");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            flag = false;
        }
        if (lift.getTime() < 1 || lift.getTime() > 360) {
            response.getOutputStream().println("Invalid time, must be between [1, 360]");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            flag = false;
        }

        return flag;
    }

    // Convert the request Json body to a SkiLiftRideEvent object
    public static SkiLiftRideEvent fromJsonToLift(HttpServletRequest request, HttpServletResponse response) throws IOException{
        
        // Read request body
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        SkiLiftRideEvent lift = new SkiLiftRideEvent(-1, -1, -1, -1, -1, -1);
        Gson gson = new Gson();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("Full string before JSON: " + sb.toString());
        } catch (Exception e) {
            response.getOutputStream().println("Invalid request JSON body.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        // Convert request body from json to SkiLiftRideEvent
        try {
            lift = gson.fromJson(sb.toString(), SkiLiftRideEvent.class);
            return lift;
        } catch (JsonSyntaxException e) {
            // Handle JSON parsing error
            System.out.println("Failed to parse and convert JSON.");
            e.printStackTrace();
        }
        return lift;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getOutputStream().println("Get " + System.currentTimeMillis() + " This is doGet for skiers");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Get incoming request
        String URL = request.getRequestURI();
        String[] URLsplit = URL.split("/");
        System.out.println("URL: " + URL + " length: " + URLsplit.length);

        // Server connection test return
        if (URLsplit.length == 3 && URLsplit[2].equals("skiers")) {
            response.getOutputStream().println("Server connected!");
            response.setStatus(HttpServletResponse.SC_CREATED); // Return 201
            return;
        }

        // If incorrect number of parameters for request
        if (URLsplit.length != 10) {
            response.getOutputStream().println("Invalid API request parameters");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // /skiresortApp/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        // Note: empty section before /skiresortApp is element 0, so there are 10 elements, not 9
        String[] cleanURLsplit = new String[URLsplit.length - 2];
        System.arraycopy(URLsplit, 2, cleanURLsplit, 0, URLsplit.length - 2);

        // Initialize response fields
        String resortID = new String();
        String seasonID = new String();
        String dayID = new String();
        String skierID = new String();

        // Parse incoming request for API request parameters
        if (cleanURLsplit.length == 8) {
            resortID = cleanURLsplit[1];
            seasonID = cleanURLsplit[3];
            dayID = cleanURLsplit[5];
            skierID = cleanURLsplit[7];
        }
        else {
            response.getOutputStream().println("Invalid API URL request");
            return;
        }

        // Verify if valid parameters
        if (!verifyRequest(resortID, seasonID, dayID, skierID, response)) {
            response.getOutputStream().println("Invalid API URL request");
            return;
        }

        // Step 2: Perform JSON body checks
        SkiLiftRideEvent lift = fromJsonToLift(request, response);
        // /skiresortApp/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
        if (lift != null && verifyLiftTicket(lift, response)) {
            response.getOutputStream().println("Dummy response body.");
            response.setStatus(HttpServletResponse.SC_CREATED); // Set to 201 CREATED successful status code
        }
        else {
            response.getOutputStream().println("Invalid API URL request");
            return;
        }
    }
}
