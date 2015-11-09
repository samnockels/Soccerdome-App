package com.example.sam.soccerdomematchapp;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Sam on 28/10/15.
 */
public class updateScore extends AsyncTask<String,String,String> {

    @Override
    protected String doInBackground(String... params)
    {

        //retrieves the parameters
        String requestURL = params[0];
        String operation_key = params[1];
        String operation_value = params[2];
        String match_id_key = params[3];
        String match_id_value = params[4];
        String score_a_key = params[5];
        String score_a_value = params[6];
        String score_b_key = params[7];
        String score_b_value = params[8];

        //Putting the columnName and data into a hashmap
        HashMap hashmap = new HashMap();
        hashmap.put(operation_key, operation_value);
        hashmap.put(match_id_key, match_id_value);
        hashmap.put(score_a_key, score_a_value);
        hashmap.put(score_b_key, score_b_value);

        performPostCall(requestURL, hashmap);

        return "";
    }


    public String performPostCall(String requestURL, HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {


            url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {

                response="";

            }
        } catch (Exception e) {


            e.printStackTrace();

        }

        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        System.out.println(result.toString());
        return result.toString();
    }

}
