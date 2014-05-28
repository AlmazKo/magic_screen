package com.example.mtg_screen;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MyActivity extends Activity {

    public static byte player1Life = 20;
    public static byte player2Life = 20;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        touchEvents();
    }

    private void touchEvents() {

        TextView screenPlayer1 = (TextView) findViewById(R.id.scr_player1);
        TextView screenPlayer2 = (TextView) findViewById(R.id.scr_player2);

        screenPlayer1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                clickPlayer1((TextView) v);
            }
        });

        screenPlayer2.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                clickPlayer2((TextView) v);
            }
        });
    }

    private void clickPlayer2(TextView v) {
        player1Life--;
        v.setText(String.valueOf(player1Life));
    }

    private void clickPlayer1(TextView v) {
        player2Life--;
        v.setText(String.valueOf(player2Life));

    }
}
