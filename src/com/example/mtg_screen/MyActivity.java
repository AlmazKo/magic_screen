package com.example.mtg_screen;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MyActivity extends Activity {

    public static byte player1Life = 20;
    public static byte player2Life = 20;
    private TextView scr1Score;
    private TextView scr2Score;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        touchEvents();
        scr1Score = (TextView) findViewById(R.id.scr1_score);
        scr2Score = (TextView) findViewById(R.id.scr2_score);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void touchEvents() {

        findViewById(R.id.scr1_plus).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setPlayer1Score(++player1Life);
            }
        });


        findViewById(R.id.scr1_minus).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setPlayer1Score(--player1Life);
            }
        });


        findViewById(R.id.scr2_plus).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setPlayer2Score(++player2Life);
            }
        });


        findViewById(R.id.scr2_minus).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                setPlayer2Score(--player2Life);
            }
        });


    }

    private void setPlayer1Score(byte score) {
        scr1Score.setText(String.valueOf(score));
    }
    private void setPlayer2Score(byte score) {
        scr2Score.setText(String.valueOf(score));
    }
}
