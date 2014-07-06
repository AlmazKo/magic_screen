package com.github.almazko.magic_screen;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Almazko
 */
public class StageGame extends Stage {
    private static String KEY_TIME = "time";

    final static long WAIT_SERIES = 1500;
    long lastSeriesPlayer1 = 0;
    long lastSeriesPlayer2 = 0;

    long seriesPlayer1 = 0;
    long seriesPlayer2 = 0;

    Timer timer;


    final Timer.Callback gameTimerCallback = new Timer.Callback() {
        @Override
        public void handle(long passedTimeMs) {

            TextView scrTimer = (TextView) context.findViewById(R.id.scr_timer);
            if (scrTimer == null) {
                return;
            }

            int seconds = (int) (passedTimeMs / 1000);
            final int minutes = seconds / 60;
            seconds = seconds % 60;

            if (passedTimeMs == 0) {
                scrTimer.setText("00:00");
            } else {
                scrTimer.setText(String.format("%02d:%02d", minutes, seconds));
            }
        }
    };


    final Handler.Callback totalTimerHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            long time = System.currentTimeMillis();


            if (seriesPlayer1 != 0 && time - lastSeriesPlayer1 > WAIT_SERIES) {
                seriesPlayer1 = 0;
                context.effect.hideSeries((TextView) context.findViewById(R.id.player_1_series), 1000);
            }

            if (seriesPlayer2 != 0 && time - lastSeriesPlayer2 > WAIT_SERIES) {
                seriesPlayer2 = 0;
                context.effect.hideSeries((TextView) context.findViewById(R.id.player_2_series), 1000);
            }

            return false;
        }
    };

    public StageGame(@NotNull MyActivity context) {
        super(context);
    }

    @Override
    void onSave(Bundle outState) {
        super.onSave(outState);

        if (timer != null) {
            outState.putLong(KEY_TIME, timer.stop());
        } else {
            outState.putLong(KEY_TIME, 0);
        }

    }

    @Override
    public void begin(@Nullable Stage prev) {

        if (timer == null) {
            timer = new Timer(gameTimerCallback);
            timer.start(0, 500);

        } else if (!timer.isStarted) {
            timer.start(0, 500);
        }

        show();
        addEvents();
    }

    @Override
    public void end(@Nullable Stage next) {
        removeEvents();
        hide();

        super.end(next);
    }


    private void show() {
        context.findViewById(R.id.scr1_plus).setVisibility(View.VISIBLE);
        context.findViewById(R.id.scr1_minus).setVisibility(View.VISIBLE);
        context.findViewById(R.id.scr2_plus).setVisibility(View.VISIBLE);
        context.findViewById(R.id.scr2_minus).setVisibility(View.VISIBLE);
    }

    private void hide() {
        context.findViewById(R.id.scr1_plus).setVisibility(View.INVISIBLE);
        context.findViewById(R.id.scr1_minus).setVisibility(View.INVISIBLE);
        context.findViewById(R.id.scr2_plus).setVisibility(View.INVISIBLE);
        context.findViewById(R.id.scr2_minus).setVisibility(View.INVISIBLE);
    }

    void addEvents() {

        context.findViewById(R.id.player_1_score).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {

                long time = System.currentTimeMillis();

                if (lastSeriesPlayer1 == 0) {
                    lastSeriesPlayer1 = time;
                } else {
                    if (time - lastSeriesPlayer1 > WAIT_SERIES) {
                        seriesPlayer1 = 0;
                    }
                    lastSeriesPlayer1 = time;
                }

                View playerScreen = context.findViewById(R.id.player_effect_1_screen);

                if ((context.isLandscape() && context.isUp(v, e)) || (!context.isLandscape() && context.isRight(v, e))) {
                    player1.add(1);
                    seriesPlayer1++;
                    context.notice(playerScreen, R.color.blink_notice);
                } else {
                    player1.damage(1);
                    seriesPlayer1--;
                    if (player1.life > 0) {
                        context.notice(playerScreen, R.color.blink_notice);
                    } else {
                        context.effect.blink(playerScreen, R.color.blink_warning, 0.3f, 150);
                    }
                }

                TextView tv = (TextView) context.findViewById(R.id.player_1_series);

                if (seriesPlayer1 > 2 || seriesPlayer1 < -2) {
                    tv.setAlpha(1);
                    tv.setTextSize(20);
                    tv.setPadding(0, 0, 0, 0);
                }

                tv.setText(String.valueOf(seriesPlayer1));

                context.playClick();
                context.showPlayerLife(player1);

                return false;
            }
        });


        context.findViewById(R.id.player_2_score).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {


                long time = System.currentTimeMillis();

                if (lastSeriesPlayer2 == 0) {
                    lastSeriesPlayer2 = time;
                } else {
                    if (time - lastSeriesPlayer2 > WAIT_SERIES) {
                        seriesPlayer2 = 0;
                    }
                    lastSeriesPlayer2 = time;
                }


                View playerScreen = context.findViewById(R.id.player_effect_2_screen);

                if ((context.isLandscape() && context.isUp(v, e)) || (!context.isLandscape() && context.isRight(v, e))) {
                    player2.add(1);
                    seriesPlayer2++;
                    context.notice(playerScreen, R.color.blink_notice);
                } else {
                    player2.damage(1);
                    seriesPlayer2--;
                    if (player2.life > 0) {
                        context.notice(playerScreen, R.color.blink_notice);
                    } else {
                        context.effect.blink(playerScreen, R.color.blink_warning, 0.3f, 150);
                    }
                }


                TextView tv = (TextView) context.findViewById(R.id.player_2_series);

                if (seriesPlayer2 > 2 || seriesPlayer2 < -2) {
                    tv.setAlpha(1);
                    tv.setTextSize(20);
                    tv.setPadding(0, 0, 0, 0);
                }

                tv.setText(String.valueOf(seriesPlayer2));

                context.playClick();
                context.showPlayerLife(player2);

                return false;
            }
        });
    }


    private void removeEvents() {
        context.findViewById(R.id.player_1_score).setOnTouchListener(null);
        context.findViewById(R.id.player_2_score).setOnTouchListener(null);
    }
}
