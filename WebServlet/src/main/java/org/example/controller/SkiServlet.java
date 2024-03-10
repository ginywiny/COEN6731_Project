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


// @WebServlet(value = "skiers")
@WebServlet(value = "skiers/*")
public class SkiServlet extends HttpServlet {
    // Full endpoint /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}

    public static boolean verifyRequest(String resortID, String seasonID, String dayID, String skierID, HttpServletResponse response) throws IOException {

        System.out.println(resortID);
        System.out.println(seasonID);
        System.out.println(dayID);
        System.out.println(skierID);

        int valResortID = Integer.parseInt(resortID);
        int valSeasonID = Integer.parseInt(seasonID);
        int valdayID = Integer.parseInt(dayID);
        int valskierID = Integer.parseInt(skierID);

        try {
            if (valResortID < 1 || valResortID > 10) {
                response.getOutputStream().println("Invalid resortID, must be between [1, 10]");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            if (valSeasonID != 2022 ) {
                response.getOutputStream().println("Invalid seasonID, must be 2022");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            if (valdayID != 1 ) {
                response.getOutputStream().println("Invalid dayID, must be 1");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            if (valskierID < 1 || valskierID > 100000) {
                response.getOutputStream().println("Invalid skierID, must be between [1, 100000]");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else {
                return false;
            }
        } catch (IOException e) {
            throw new IOException("Invalid API request.", e);
        }
        
        return true;
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

        

        // Dummy data return for valid response
        // response.getOutputStream().println("Post " + System.currentTimeMillis() + " This is doPost for skiers");
    }
}
