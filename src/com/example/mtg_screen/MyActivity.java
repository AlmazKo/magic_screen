package com.example.mtg_screen;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MyActivity extends Activity {

    Player player1;
    Player player2;

    final static int MAX_SCREENS = 4;

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

        init();
        touchEvents();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


//
//        Timer timer = new Timer();
//
//        timer.scheduleAtFixedRate(
//                new java.util.TimerTask() {
//                    @Override
//                    public void run() {
//                        h.sendEmptyMessage(0);
//                    }
//                },
//                0, 500
//        );

        showActions();

    }

    void changeRightScreen(Player player) {
        if (player.screenId >= MAX_SCREENS) {
            player.screenId = 1;
        } else {
            player.screenId++;
        }

        showDetails(player.fragmentId, player.screenId);
    }

    void changeLeftScreen(Player player) {
        if (player.screenId <= 1) {
            player.screenId = MAX_SCREENS;
        } else {
            player.screenId--;
        }

        showDetails2(player.fragmentId, player.screenId);
    }

    void showDetails(int screenId, int slideId) {

        SlideFragment fg = (SlideFragment) getFragmentManager().findFragmentById(screenId);
        if (fg == null || fg.getIndex() != slideId) {

            fg = SlideFragment.newInstance(slideId);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_to_left_in, R.animator.slide_to_left_out);
            ft.replace(screenId, fg);
            ft.commit();
        }
    }

    void showDetails2(int screenId, int slideId) {

        SlideFragment fg = (SlideFragment) getFragmentManager().findFragmentById(screenId);
        if (fg == null || fg.getIndex() != slideId) {

            fg = SlideFragment.newInstance(slideId);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_to_right_in, R.animator.slide_to_right_out);
            ft.replace(screenId, fg);
            ft.commit();
        }
    }

    void showActions() {

        ActionsFragment fg = (ActionsFragment) getFragmentManager().findFragmentById(R.id.f_actions);
        if (fg == null) {

            fg = ActionsFragment.newInstance(ActionsFragment.Stage.DISPOSAL);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.replace(R.id.f_actions, fg);
            ft.commit();
        }
    }

    void ani() {
        ValueAnimator anim = ValueAnimator.ofInt(10, 200);
        anim.setDuration(5000);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                scr1Score.setLeft(value);
            }
        });

        anim.start();
    }

    private void touchEvents() {

//        findViewById(R.id.scr_start).setOnClickListener(new Button.OnClickListener() {
//            public void onClick(View v) {
//                showDetails(R.id.player_2_screen, 2);
////                   ani();
//            }
//        });
        findViewById(R.id.scr1_plus).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                player1.add(1);
                showPlayer(player1);
            }
        });
        findViewById(R.id.scr1_minus).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                player1.damage(1);
                showPlayer(player1);
            }
        });
        findViewById(R.id.scr2_plus).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                player2.add(1);
                showPlayer(player2);
            }
        });
        findViewById(R.id.scr2_minus).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                player2.damage(1);
                showPlayer(player2);
            }
        });

//        findViewById(R.id.f_actions).setOnClickListener(new Button.OnClickListener() {
//            public void onClick(View v) {
//                start();
//            }
//
//
//        });

//        findViewById(R.id.reload).setOnClickListener(new Button.OnClickListener() {
//            public void onClick(View v) {
//                init();
//            }
//        });
    }

    void start() {

        findViewById(R.id.scr1_plus).setVisibility(View.VISIBLE);
        findViewById(R.id.scr1_minus).setVisibility(View.VISIBLE);
        findViewById(R.id.scr2_plus).setVisibility(View.VISIBLE);
        findViewById(R.id.scr2_minus).setVisibility(View.VISIBLE);

        player1.life = 20;
        player2.life = 20;
    }


    void init() {
        // TODO add destroy
        player1 = new Player((TextView) findViewById(R.id.scr1_score));
        player1.fragmentId = R.id.player_1_screen;
        player1.screenId = 3;


        player2 = new Player((TextView) findViewById(R.id.scr2_score));
        player2.fragmentId = R.id.player_2_screen;
        player2.screenId = 3;

        startTime = System.currentTimeMillis();
//        scrTimer = (TextView) findViewById(R.id.scr_timer);
//        scrTimer.setText("00:00");

        showPlayer(player1);
        showPlayer(player2);
        showDetails(R.id.player_1_screen, 1);
        showDetails(R.id.player_2_screen, 2);

        View v;


        v = findViewById(R.id.player_1_screen);

        v.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
//                changeScreen(player2);
                changeLeftScreen(player1);
            }

            @Override
            public void onSwipeRight() {
                changeRightScreen(player1);
            }
        });

        v = findViewById(R.id.player_2_screen);
        v.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
//                changeScreen(player2);
                changeLeftScreen(player2);
            }

            @Override
            public void onSwipeRight() {
                changeRightScreen(player2);
            }
        });


    }

    private void showPlayer(Player player) {
        player.lifeView.setText(String.valueOf(player.life));
    }

//    private void setPlayer1Score(byte score) {
//        scr1Score.setText(String.valueOf(score));
//    }
//
//    private void setPlayer2Score(byte score) {
//        //  scr2Score.setText(String.valueOf(score));
//    }


}
