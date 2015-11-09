package com.example.sam.soccerdomematchapp;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Handler;

/**
 * Created by Sam on 29/10/15.
 */
public class getData extends AsyncTask<String,String,String> {



    @Override
    protected String doInBackground(String... params){

        String operation = params[0];
        String date = params[1];
        String parameter = "";
        String result = "";

        if (operation == "get_fixtures")
            parameter = "?operation=" + operation + "&date=" + date;
            try{
                return sendGet(parameter);
            }catch (Exception e){

            }

        return "error";

    }

    private String sendGet(String param) throws Exception {

        String url = "http://192.168.0.5/app/get.php" + param;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();

    }



}
