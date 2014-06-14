package com.github.almazko.magic_screen;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class MyActivity extends Activity {

    enum Stage {DISPOSAL, GAME, PAUSE}

    Stage currentStage;

    private static String KEY_PLAYER_1 = "player_1";
    private static String KEY_PLAYER_2 = "player_2";
    private static String KEY_STAGE = "stage";
    private static String KEY_TIME = "time";


    private MediaPlayer playerClick;

    Player player1;
    Player player2;
    Timer timer;

    final static int MAX_SCREENS = 4;

    final Timer.Callback call = new Timer.Callback() {
        @Override
        public void handle(long passedTimeMs) {

            TextView scrTimer = (TextView) findViewById(R.id.scr_timer);
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


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main);

        if (state != null) {
            restoreState(state);
        } else {
            init();
        }

        switch (currentStage) {
            case DISPOSAL:
                stageDisposal();
                break;
            case GAME:
                stageGame();
        }

        showPlayers();
        showActions();

        playerClick = MediaPlayer.create(this, R.raw.sound_quick1);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!isLandscape()) {
            rotatePlayer1Screen();
        }
    }

    private void stageGame() {

        currentStage = Stage.GAME;

        removeChoiceStageEvents();
        gameStageEvents();

        showGameViews();

        if (timer == null) {
            timer = new Timer(call);
            timer.start(1500, 500);

        } else if (!timer.isStarted) {
            timer.start(0, 500);
        }
    }

    private void showGameViews() {
        findViewById(R.id.scr1_plus).setVisibility(View.VISIBLE);
        findViewById(R.id.scr1_minus).setVisibility(View.VISIBLE);
        findViewById(R.id.scr2_plus).setVisibility(View.VISIBLE);
        findViewById(R.id.scr2_minus).setVisibility(View.VISIBLE);
    }

    private void hideGameViews() {
        findViewById(R.id.scr1_plus).setVisibility(View.INVISIBLE);
        findViewById(R.id.scr1_minus).setVisibility(View.INVISIBLE);
        findViewById(R.id.scr2_plus).setVisibility(View.INVISIBLE);
        findViewById(R.id.scr2_minus).setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_PLAYER_1, player1);
        outState.putSerializable(KEY_PLAYER_2, player2);
        outState.putString(KEY_STAGE, currentStage.toString());

        if (timer != null) {
            outState.putLong(KEY_TIME, timer.stop());
        } else {
            outState.putLong(KEY_TIME, 0);
        }

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

            fg = ActionsFragment.newInstance(Stage.DISPOSAL);
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
                //  int value = (int) valueAnimator.getAnimatedValue();
                //scr1Score.setLeft(value);
            }
        });

        anim.start();
    }

    private void gameStageEvents() {

        findViewById(R.id.player_1_score).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {

                if ((isLandscape() && isUp(v, e)) || (!isLandscape() && isRight(v, e))) {
                    player1.add(1);
                } else {
                    player1.damage(1);
                }

                playClick();
                showPlayer(player1);

                return false;
            }
        });

        findViewById(R.id.player_2_score).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if ((isLandscape() && isUp(v, e)) || (!isLandscape() && isRight(v, e))) {
                    player2.add(1);
                } else {
                    player2.damage(1);
                }

                playClick();
                showPlayer(player2);

                return false;
            }
        });
    }

    private void removeGameStageEvents() {
        findViewById(R.id.player_1_score).setOnTouchListener(null);
        findViewById(R.id.player_2_score).setOnTouchListener(null);
    }

    private void playClick() {
        if (playerClick.isPlaying()) {
            playerClick.seekTo(0);
        } else {
            playerClick.start();
        }
    }

    private void choiceStageEvents() {
        View v;

        v = findViewById(R.id.player_1_screen);

        v.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
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
                changeLeftScreen(player2);
            }

            @Override
            public void onSwipeRight() {
                changeRightScreen(player2);
            }
        });
    }

    private void removeChoiceStageEvents() {
        View v;

        v = findViewById(R.id.player_1_screen);
        v.setOnTouchListener(null);

        v = findViewById(R.id.player_2_screen);
        v.setOnTouchListener(null);
    }

    private void stageDisposal() {
        currentStage = Stage.DISPOSAL;

        hideGameViews();
        removeGameStageEvents();
        choiceStageEvents();
    }

    void start() {
        stageGame();
    }

    void resume() {

        timer = new Timer(call, timer.time);
        timer.start(0, 500);

        stageGame();
    }

    public void pause() {
        if (timer != null) {
            timer.stop();
        }

    }

    private void restoreState(Bundle state) {
        player1 = (Player) state.getSerializable(KEY_PLAYER_1);
        player2 = (Player) state.getSerializable(KEY_PLAYER_2);
        currentStage = Stage.valueOf(state.getString(KEY_STAGE));
        timer = new Timer(call, state.getLong(KEY_TIME));
    }

    void reset() {

        final int prevScreenIdPlayer1 = player1.screenId;
        final int prevScreenIdPlayer2 = player2.screenId;

        player1 = new Player(R.id.scr1_score);
        player1.fragmentId = R.id.player_1_screen;
        player1.screenId = prevScreenIdPlayer1;

        player2 = new Player(R.id.scr2_score);
        player2.fragmentId = R.id.player_2_screen;
        player2.screenId = prevScreenIdPlayer2;

        timer.stop();
        timer = null;
        stageDisposal();
        showPlayers();
    }

    private void init() {

        currentStage = Stage.DISPOSAL;
        // TODO add destroy
        player1 = new Player(R.id.scr1_score);
        player1.fragmentId = R.id.player_1_screen;
        player1.screenId = 2;


        player2 = new Player(R.id.scr2_score);
        player2.fragmentId = R.id.player_2_screen;
        player2.screenId = 3;
    }


    void showPlayers() {

        showPlayer(player1);
        showPlayer(player2);
        showDetails(player1.fragmentId, player1.screenId);
        showDetails(player2.fragmentId, player2.screenId);
    }

    private void showPlayer(Player player) {
        TextView tw = (TextView) findViewById(player.lifeViewId);
        tw.setText(String.valueOf(player.life));
    }


    boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }


    void rotatePlayer1Screen() {
        findViewById(R.id.player_1_score).setRotation(180);
    }

    static boolean isRight(final View v, final MotionEvent e) {
        return e.getX() > v.getWidth() / 2;
    }

    static boolean isUp(final View v, final MotionEvent e) {
        return e.getY() < v.getHeight() / 2;
    }
}

