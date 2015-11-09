package com.example.sam.soccerdomematchapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class SelectTeams extends Activity {


    private String response;
    private ListView mainListView;
    private ArrayAdapter listAdapter;
    private int selectedItem = 0;
    private String teamA[] = new String[20];
    private String teamB[] = new String[20];
    private String match_id[] = new String[20];

    private boolean fixturesPresent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //--Pre written code:
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_teams);
        //--

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //
        fixturesPresent = populateList();

        if(fixturesPresent){
            mainListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            mainListView.setItemChecked(0, true);
            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    selectedItem = position;
                }
            });
        }

    }

    //Automatically Generated Method
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_teams, menu);
        return true;
    }

    //Automatically Generated Method
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    //populateList will
    public boolean populateList() {

        String operation = "get_fixtures";
        String date = getDate();

        response = sendGet(operation, date);

        if (response == "Connection Error") {

            ((TextView) findViewById(R.id.heading)).setText("There was an error accessing the database, please try again later.");
            ((TextView) findViewById(R.id.heading)).setTextColor(Color.RED);
            return false;

        } else if (response == "No Results") {

            ((TextView) findViewById(R.id.heading)).setText("No Matches Today.");
            ((TextView) findViewById(R.id.heading)).setTextColor(Color.RED);
            return false;

        } else {

            ((TextView) findViewById(R.id.heading)).setText("Matches happening today:");
            ((TextView) findViewById(R.id.heading)).setTextColor(Color.parseColor("#C1E1A6"));


            // Find the ListView in the UI.
            mainListView = (ListView) findViewById(R.id.listView);

            //Matches are send in one long string, and are seperated by the = sign.
            //This splits the string up and puts it into an array.
            String[] match = response.split("=", -1);


            int length = match.length;

            String string;

            String[] formattedMatches = new String[length];

            for (int i = 0; i <= length - 1; i++) {

                string = match[i];

                String[] temp = string.split("/", -1);

                match_id[i] = temp[1];

                temp = temp[0].split("\\+", -1);

                teamA[i] = temp[0].toUpperCase();
                teamB[i] = temp[1].toUpperCase();

                formattedMatches[i] = teamA[i] + "    VS    " + teamB[i];

            }


            ArrayList<String> fixtures = new ArrayList<String>();
            fixtures.addAll(Arrays.asList(formattedMatches));


            // Create ArrayAdapter using the planet list.
            listAdapter = new ArrayAdapter<String>(this, R.layout.list_view_style, fixtures);


            // Set the ArrayAdapter as the ListView's adapter.
            mainListView.setAdapter(listAdapter);

            return true;

        }
    }

    //Creates a new GET request, which will return:
    // - The fixtures for the current day if they are present in the format:  teamA+teamB=teamC+teamD
    // - - - The '=' seperates the fixtures and the + seperates teams in each fixture

    // - "No Results" if there are not fixtures for the current day
    // - "Connection Error" if there was a problem connecting to the database


    private String sendGet(String operation, String date){


        String address = "http://192.168.0.6/Computing/app/get.php?operation=" + operation + "&date=" + date;
        //String address = "http://4d07b1c2.ngrok.io/Computing/app/get.php?operation=" + operation + "&date=" + date;

        try{
            URL url = new URL(address);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            StringBuilder response = new StringBuilder();

            StringBuilder noResults = new StringBuilder();
            noResults.append("No Results");


            while((line = in.readLine()) != null) {
                response.append(line);
            }

            in.close();

            if (response.toString().equals(noResults.toString())){

                return "No Results";

            }else {

                return response.toString();

            }


        }catch(Exception e){

            return "Connection Error";

        }
    }


    //When the Start Match button is pressed:
    public void matchStart(View view){

        if (fixturesPresent){

            //End the current activity
            finish();

            //Set up the new activity Main Activity.java
            Intent i = new Intent(getApplicationContext(), MainActivity.class);

            //Pass some variables to the new activity.
            i.putExtra("team_a", teamA[selectedItem]);
            i.putExtra("team_b", teamB[selectedItem]);
            i.putExtra("match_id", match_id[selectedItem]);

            //Start the activity.
            startActivity(i);

        }else{

            //Quick display message
            Toast.makeText(SelectTeams.this, "No Fixture Selected", Toast.LENGTH_SHORT).show();
        }

    }


    //When the Refresh button is pressed
    public void refreshList(View view){

        //Clear the list
        mainListView.setAdapter(null);

        //Submit the query again, and fill the list with the results
        populateList();

    }

    //Quick method which returns the current date in YY/MM/DD format.
    public String getDate(){

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        //get current date time with Date()
        Date date = new Date();
        return dateFormat.format(date);

    }



}
