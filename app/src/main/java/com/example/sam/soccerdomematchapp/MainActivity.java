package com.example.sam.soccerdomematchapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Chronometer;
import android.widget.Toast;
import android.app.AlertDialog;
import android.os.Vibrator;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements Chronometer.OnChronometerTickListener {


    //Declaring View Variables
    private Chronometer timer; //The timer
    private TextView teamAscore, teamBscore; //The scoreboard
    private Vibrator v; //Access the the devices vibrate functionality
    private int scoreA = 0;
    private int scoreB = 0;
    private boolean stopwatchOn = false;
    private long timeWhenStopped = 0;
    private Toast errorMessage;
    private boolean quarterEnd = false;
    private String timerTime = "00:00";

    private String team_a;
    private String team_b;
    private String match_id;

    private TextView matchStage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Initialising View Variables
        timer = ((Chronometer)findViewById(R.id.chronometer));
        teamAscore = ((TextView)findViewById(R.id.teamAscore));
        teamBscore = ((TextView)findViewById(R.id.teamBscore));
        matchStage = ((TextView)findViewById(R.id.stageText));

        //getSystemService with this parameter will allow the access to the vibrate functionality.
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        timer.setOnChronometerTickListener(this);

        //When the activity moves from select teams to main activity, the two teams are passed
        //as variables team_a and team_b
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            team_a = extras.getString("team_a");
            team_b = extras.getString("team_b");
            match_id = extras.getString("match_id");
        }

        ((TextView) findViewById(R.id.teamAname)).setText(team_a);
        ((TextView) findViewById(R.id.teamBname)).setText(team_b);

        Toast.makeText(MainActivity.this, match_id, Toast.LENGTH_SHORT).show();

        //Makes sure the score is set to 0-0 at the start
        updateScore update_s = new updateScore();
        update_s.execute("http://192.168.0.6/app/update.php", "operation", "updateMatchScore", "match_id", match_id, "team_a_score", String.valueOf(scoreA), "team_b_score", String.valueOf(scoreB));
        //update_s.execute("http://4d07b1c2.ngrok.io/Computing/app/update.php", "operation", "updateMatchScore", "match_id", match_id, "team_a_score", String.valueOf(scoreA), "team_b_score", String.valueOf(scoreB));


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void toggle(View view) {

        if(stopwatchOn){

            timeWhenStopped = timer.getBase() - SystemClock.elapsedRealtime();
            timer.stop();
            stopwatchOn = false;
            timer.setTextColor(Color.RED);

        }
        else{

            if (quarterEnd)
                Toast.makeText(MainActivity.this, timerTime, Toast.LENGTH_SHORT).show();
                timer.setText(timerTime);
                quarterEnd = false;

            timer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            timer.start();
            stopwatchOn = true;
            timer.setTextColor(Color.GREEN);

        }
    }

    public void onChronometerTick(Chronometer chronometer) {

        //Setting up local variables
        boolean stop = false;
        boolean halfTime = false;
        boolean fullTime = false;

        timerTime = (timer.getText()).toString();  //Gets the string value of the timer

        updateTime update = new updateTime();
        update.execute("http://192.168.0.6/app/update.php", "operation", "updateTime", "match_id", match_id, "time", "00:" + timerTime);

        //Using a switch statement for readability
        switch (timerTime){

            case "00:00":
                matchStage.setText("Quarter 1");
                break;

            case "00:10":
                stop = true;
                matchStage.setText("End of Quarter 1");
                quarterEnd = true;
                break;

            case "00:11":
                matchStage.setText("Quarter 2");
                break;

            case "00:20":
                stop = true;
                halfTime = true;
                matchStage.setText("Half Time");
                quarterEnd = true;
                break;

            case "00:21":
                matchStage.setText("Quarter 3");
                break;

            case "00:30":
                stop = true;
                matchStage.setText("End of Quarter 3");
                quarterEnd = true;
                break;

            case "00:31":
                matchStage.setText("Quarter 4");
                break;

            case "00:40":
                stop = true;
                fullTime = true;
                matchStage.setText("Full Time");
                break;
        }

        if (stop){

            //Stop the timer
            timeWhenStopped = timer.getBase() - SystemClock.elapsedRealtime();
            timer.stop();
            stopwatchOn = false;
            timer.setTextColor(Color.RED);

            //if the device has vibrate hardware
            if(v.hasVibrator()){

                if(halfTime || fullTime){

                    //Setting a new array for the vibrate pattern
                    long[] pattern = {0,500,500,500};

                    //1. Wait 0s
                    //2. Vibrate for 500 milliseconds
                    //3. Wait for 500 milliseconds
                    //4. Vibrate for 500 milliseconds

                    //The -1 stops the pattern from repeating
                    v.vibrate(pattern, -1);

                }else{
                    //Vibrate for 1 second.
                    v.vibrate(1000);
                }
            }
            if (fullTime) {
                //startActivity(new Intent(MainActivity.this, MatchUploadedScreen.class));
                Toast.makeText(MainActivity.this, "Full Time!", Toast.LENGTH_SHORT).show();
            }

        }

    }
    public void teamAgoal(View view) {
        if (stopwatchOn)
            scoreA = changeGoalTally("A", true,scoreA, scoreB);
        else
            errorMsg(1);
    }

    public void teamBgoal(View view){
        if(stopwatchOn)
            scoreB = changeGoalTally("B", true, scoreA, scoreB);
        else
            errorMsg(1);
    }

    public void reduceA(View view){
        if(stopwatchOn)
            scoreA = changeGoalTally("A", false, scoreA, scoreB);
        else
            errorMsg(1);
    }

    public void reduceB(View view){
        if(stopwatchOn)
            scoreB = changeGoalTally("B", false, scoreA, scoreB);
        else
            errorMsg(1);
    }

    public int changeGoalTally(String team, boolean increment, int scoreA, int scoreB){

        switch (team) {

            case "A":

                if (increment)
                    scoreA += 1;
                else
                    scoreA -= 1;

                teamAscore.setText(String.valueOf(scoreA));
                toggle(null);

                break;

            case "B":

                if (increment)
                    scoreB += 1;
                else
                    scoreB -= 1;

                teamBscore.setText(String.valueOf(scoreB));
                toggle(null);

                break;

        }

        updateScore update = new updateScore();
        update.execute("http://192.168.0.6/app/update.php", "operation", "updateMatchScore", "match_id", match_id, "team_a_score", String.valueOf(scoreA) , "team_b_score", String.valueOf(scoreB));
        //update.execute("http://4d07b1c2.ngrok.io/Computing/app/update.php", "operation", "updateMatchScore", "match_id", match_id, "team_a_score", String.valueOf(scoreA) , "team_b_score", String.valueOf(scoreB));
        if (team == "A")
            return scoreA;
        else
            return scoreB;

    }

    public void errorMsg(int errorCode){

        switch (errorCode){

            case 1:
                errorMessage.makeText(MainActivity.this, "Start Stopwatch!", Toast.LENGTH_SHORT).show();

        }
    }

    public void onBackPressed(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Quit")
                .setMessage("Are you sure you want to quit? All unsaved changes will be lost.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        Intent i = new Intent(getApplicationContext(), SelectTeams.class);
                        startActivity(i);
                    }
                })
                .setNegativeButton("No", null)
                .show();


    }

    public void penalty(View view){




    }



}
