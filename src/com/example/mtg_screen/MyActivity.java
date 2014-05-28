package com.example.mtg_screen;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;

public class MyActivity extends Activity {

    public static byte player1Life = 20;
    public static byte player2Life = 20;
    private TextView scr1Score;
    private TextView scr2Score;
    private TextView scrTimer;
    private long startTime;


    final Handler h = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int ms = (int) millis - seconds * 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;

            scrTimer.setText(String.format("%02d:%02d", minutes, seconds));
            return false;
        }
    });


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        scr1Score = (TextView) findViewById(R.id.scr1_score);
        scr2Score = (TextView) findViewById(R.id.scr2_score);

        init();
        touchEvents();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        Timer timer = new Timer();

        timer.scheduleAtFixedRate(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        h.sendEmptyMessage(0);
                    }
                },
                0, 500
        );
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

        findViewById(R.id.reload).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                init();
            }
        });
    }

    void init() {
        startTime = System.currentTimeMillis();
        scrTimer = (TextView) findViewById(R.id.scr_timer);
        scrTimer.setText("00:00");
        player1Life = 20;
        player2Life = 20;
        setPlayer1Score(player1Life);
        setPlayer2Score(player2Life);
    }

    private void setPlayer1Score(byte score) {
        scr1Score.setText(String.valueOf(score));
    }

    private void setPlayer2Score(byte score) {
        scr2Score.setText(String.valueOf(score));
    }
}
