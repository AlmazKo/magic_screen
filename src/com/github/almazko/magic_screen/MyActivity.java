package com.github.almazko.magic_screen;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyActivity extends Activity implements View.OnTouchListener {


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        timeLastAction = System.currentTimeMillis();
        if (isSleep) {
            isSleep = false;
            restoreBrightness();

        }
        return false;
    }

    enum Stage {DISPOSAL, GAME, PAUSE}

    Stage currentStage;


    static boolean isSleep = false;


    public static final String PREFS_NAME = "mtg_preferences";

    public static boolean manageBrightness = false;
    static boolean fullScreen = false;

    // rename player1 -> opponent
    private static String KEY_PLAYER_1 = "player_1";
    private static String KEY_PLAYER_2 = "player_2";
    private static String KEY_STAGE = "stage";
    private static String KEY_TIME = "time";

    private static final float BRIGHTNESS_WAITING = 0.02f;
    private static final int WAITING = 5000;

    long timeLastAction = 0;

    private MediaPlayer mpTick;

    final static long WAIT_SERIES = 1500;
    long lastSeriesPlayer1 = 0;
    long lastSeriesPlayer2 = 0;

    long seriesPlayer1 = 0;
    long seriesPlayer2 = 0;

    Player player1;
    Player player2;
    Timer timer;
    java.util.Timer totalTimer;

    final static int MAX_SCREENS = 7;

    final Timer.Callback gameTimerCallback = new Timer.Callback() {
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

    final Handler totalTimerHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            long time = System.currentTimeMillis();


            if (seriesPlayer1 != 0 && time - lastSeriesPlayer1 > WAIT_SERIES) {
                seriesPlayer1 = 0;
                hideSeries((TextView) findViewById(R.id.player_1_series), 1000);
            }

            if (seriesPlayer2 != 0 && time - lastSeriesPlayer2 > WAIT_SERIES) {
                seriesPlayer2 = 0;
                hideSeries((TextView) findViewById(R.id.player_2_series), 1000);
            }

            if (manageBrightness && time - timeLastAction > WAITING) {
                isSleep = true;
                setBrightness(BRIGHTNESS_WAITING);
            }

            Log.i("MAGIC", "test");

            return false;
        }
    });


    int getHeightStatusBar() {
        int resId = getResources().getIdentifier("status_bar_height",
                "dimen",
                "android");
        if (resId > 0) {
            return getResources().getDimensionPixelSize(resId);
        }

        return 0;
    }

    int getHeightNavBar() {
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }

        return 0;
    }

    Point getSize() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);

        return size;
    }

    public void onResume() {
        super.onResume();

        timeLastAction = System.currentTimeMillis();
        isSleep = false;
        restoreBrightness();
        runTotalTimer();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("manage_brightness", manageBrightness);

        // Commit the edits!
        editor.commit();
    }


    public void onPause() {
        super.onPause();

    }

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

        mpTick = MediaPlayer.create(this, R.raw.sound_tick1);
        mpTick.setVolume(0.05f, 0.05f);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!isLandscape()) {
            rotatePlayer1Screen();
        }

        runTotalTimer();

        isSleep = false;
        timeLastAction = System.currentTimeMillis();
        findViewById(R.id.main_view).setOnTouchListener(this);

        setFullscreenMode();
        positioning();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        manageBrightness = settings.getBoolean("manage_brightness", false);
    }

    void setFullscreenMode() {
        View decorView = getWindow().getDecorView();
        int uiOptions = 0;
        if (fullScreen) {

            uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        decorView.setSystemUiVisibility(uiOptions);
    }


    void positioning() {

        int barSize = getHeightStatusBar();
        Point size = getSize();

        int width = size.x;
        int height = size.y;


        RelativeLayout.LayoutParams screen1sizing;
        RelativeLayout.LayoutParams screen2sizing;
        if (isLandscape()) {
            if (fullScreen) {
                width += getHeightNavBar();
            } else {
                height -= barSize;
            }

            screen1sizing = new RelativeLayout.LayoutParams(width / 2, height);
            screen2sizing = new RelativeLayout.LayoutParams(width / 2, height);
            screen2sizing.setMargins(width / 2, 0, 0, 0);
        } else {

            if (fullScreen) {
                height += getHeightNavBar();
            } else {
                height -= barSize;
            }

            screen1sizing = new RelativeLayout.LayoutParams(width, height / 2);
            screen2sizing = new RelativeLayout.LayoutParams(width, height / 2);
            screen2sizing.setMargins(0, height / 2, 0, 0);
        }

        findViewById(R.id.player_1_screen).setLayoutParams(screen1sizing);
        findViewById(R.id.player_2_screen).setLayoutParams(screen2sizing);
    }

    private void stageGame() {

        currentStage = Stage.GAME;

        removeStateDisposal();
        eventsGameStage();

        showGameViews();

        if (timer == null) {
            timer = new Timer(gameTimerCallback);
            timer.start(0, 500);

        } else if (!timer.isStarted) {
            timer.start(0, 500);
        }

    }

    void setBrightness(float value) {
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = value;
        getWindow().setAttributes(layout);
    }

    void restoreBrightness() {
        if (!manageBrightness) {
            return;
        }

        int curBrightnessValue;
        try {
            curBrightnessValue = android.provider.Settings.System.getInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);

        } catch (Settings.SettingNotFoundException e) {
            return;
        }

        if (curBrightnessValue == 0) {
            return;
        }

        setBrightness(curBrightnessValue);
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

        if (totalTimer != null) {
            totalTimer.cancel();
            totalTimer = null;
        }
    }

    void changeScreenToRight(final Player player) {
        if (player.screenId >= MAX_SCREENS) {
            player.screenId = 1;
        } else {
            player.screenId++;
        }

        slideToRight(player.fragmentId, player.screenId);
    }

    void changeScreenToLeft(final Player player) {
        if (player.screenId <= 1) {
            player.screenId = MAX_SCREENS;
        } else {
            player.screenId--;
        }

        slideToLeft(player.fragmentId, player.screenId);
    }

    void slideToRight(final int screenId, final int slideId) {

        SlideFragment fg = (SlideFragment) getFragmentManager().findFragmentById(screenId);
        if (fg == null || fg.getIndex() != slideId) {

            fg = SlideFragment.newInstance(slideId);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.animator.slide_to_left_in, R.animator.slide_to_left_out);
            ft.replace(screenId, fg);
            ft.commit();
        }
    }

    void slideToLeft(int screenId, int slideId) {

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

    void notice(final View v, final int bgId) {
        blink(v, bgId, 0.15f, 150);
    }

    void blink(final View v, final int bgId, final float blinkOpacity, final int ms) {
        v.setBackgroundResource(bgId);

        ValueAnimator appear = ValueAnimator.ofFloat(0, blinkOpacity);
        appear.setDuration(ms);
        appear.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                v.setAlpha((Float) valueAnimator.getAnimatedValue());
            }
        });

        ValueAnimator disappear = ValueAnimator.ofFloat(blinkOpacity, 0);
        disappear.setDuration(ms);
        disappear.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                v.setAlpha((Float) valueAnimator.getAnimatedValue());
            }
        });

        AnimatorSet anim = new AnimatorSet();
        anim.play(appear).before(disappear);
        anim.start();
    }

    void hideSeries(final TextView v, final int msDuration) {

        final int lastTime = 300;
        final float textSize = 20;

        ValueAnimator firstHiding = ValueAnimator.ofFloat(1, 0.8f);
        firstHiding.setDuration(msDuration - lastTime);
        firstHiding.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                v.setAlpha((Float) valueAnimator.getAnimatedValue());
            }
        });


        final int top = v.getTop();

        ValueAnimator greater = ValueAnimator.ofFloat(textSize, textSize * 3);
        greater.setDuration(msDuration - lastTime);
        greater.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final int offset = (int) ((Float) valueAnimator.getAnimatedValue() - textSize);
                v.setTextSize((Float) valueAnimator.getAnimatedValue());
                v.setPadding(0, -offset, 0, 0);
            }
        });

        ValueAnimator secondHiding = ValueAnimator.ofFloat(0.8f, 0);
        secondHiding.setDuration(lastTime);
        secondHiding.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                v.setAlpha((Float) valueAnimator.getAnimatedValue());
            }
        });

        AnimatorSet anim = new AnimatorSet();
        anim.play(firstHiding).with(greater);
        anim.play(greater).before(secondHiding);
        anim.start();
    }

    private void eventsGameStage() {

        findViewById(R.id.player_1_score).setOnTouchListener(new View.OnTouchListener() {
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

                View playerScreen = findViewById(R.id.player_effect_1_screen);

                if ((isLandscape() && isUp(v, e)) || (!isLandscape() && isRight(v, e))) {
                    player1.add(1);
                    seriesPlayer1++;
                    notice(playerScreen, R.color.blink_notice);
                } else {
                    player1.damage(1);
                    seriesPlayer1--;
                    if (player1.life > 0) {
                        notice(playerScreen, R.color.blink_notice);
                    } else {
                        blink(playerScreen, R.color.blink_warning, 0.3f, 150);
                    }
                }

                TextView tv = (TextView) findViewById(R.id.player_1_series);

                if (seriesPlayer1 > 2 || seriesPlayer1 < -2) {
                    tv.setAlpha(1);
                    tv.setTextSize(20);
                    tv.setPadding(0, 0, 0, 0);
                }

                tv.setText(String.valueOf(seriesPlayer1));

                playClick();
                showPlayer(player1);

                return false;
            }
        });

        findViewById(R.id.player_2_score).setOnTouchListener(new View.OnTouchListener() {
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


                View playerScreen = findViewById(R.id.player_effect_2_screen);

                if ((isLandscape() && isUp(v, e)) || (!isLandscape() && isRight(v, e))) {
                    player2.add(1);
                    seriesPlayer2++;
                    notice(playerScreen, R.color.blink_notice);
                } else {
                    player2.damage(1);
                    seriesPlayer2--;
                    if (player2.life > 0) {
                        notice(playerScreen, R.color.blink_notice);
                    } else {
                        blink(playerScreen, R.color.blink_warning, 0.3f, 150);
                    }
                }


                TextView tv = (TextView) findViewById(R.id.player_2_series);

                if (seriesPlayer2 > 2 || seriesPlayer2 < -2) {
                    tv.setAlpha(1);
                    tv.setTextSize(20);
                    tv.setPadding(0, 0, 0, 0);
                }

                tv.setText(String.valueOf(seriesPlayer2));

                playClick();
                showPlayer(player2);

                return false;
            }
        });
    }

    private void removeEventsGameStage() {
        findViewById(R.id.player_1_score).setOnTouchListener(null);
        findViewById(R.id.player_2_score).setOnTouchListener(null);
    }

    private void playClick() {
        if (mpTick.isPlaying()) {
            mpTick.seekTo(0);
        } else {
            mpTick.start();
        }
    }

    private void eventsChoiceStage() {
        View v;

        v = findViewById(R.id.player_1_screen);

        v.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                changeScreenToLeft(player1);
            }

            @Override
            public void onSwipeRight() {
                changeScreenToRight(player1);
            }
        });

        v = findViewById(R.id.player_2_screen);
        v.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                changeScreenToLeft(player2);
            }

            @Override
            public void onSwipeRight() {
                changeScreenToRight(player2);
            }
        });
    }

    private void removeStateDisposal() {
        findViewById(R.id.player_1_screen).setOnTouchListener(null);
        findViewById(R.id.player_2_screen).setOnTouchListener(null);

        findViewById(R.id.player_1_description).setVisibility(View.GONE);
        findViewById(R.id.player_2_description).setVisibility(View.GONE);
    }

    private void stageDisposal() {
        currentStage = Stage.DISPOSAL;

        hideGameViews();
        removeEventsGameStage();
        eventsChoiceStage();
    }

    void start() {
        stageGame();
    }

    void resume() {
        timer = new Timer(gameTimerCallback, timer.stop());
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

        if (timer == null) {
            timer = new Timer(gameTimerCallback, state.getLong(KEY_TIME));
        }

        runTotalTimer();
    }

    void runTotalTimer() {
        if (totalTimer == null) {

            totalTimer = new java.util.Timer();
            totalTimer.scheduleAtFixedRate(new java.util.TimerTask() {
                @Override
                public void run() {
                    totalTimerHandler.sendEmptyMessage(0);
                }
            }, 0, 500L);
        }
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

        gameOver(android.R.color.background_dark, 300);
    }

    void gameOver(final int bgId, final int duration) {

        final View shadower = findViewById(R.id.full_screen);
        shadower.setVisibility(View.VISIBLE);
        shadower.setBackgroundResource(bgId);

        ValueAnimator appear = ValueAnimator.ofFloat(0, 1f);
        appear.setDuration(duration);
        appear.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                shadower.setAlpha((Float) valueAnimator.getAnimatedValue());
            }
        });
        ValueAnimator caller = AnimatorCallback.get(new AnimatorCallback.Callback() {
            @Override
            public void call() {
                stageDisposal();
                showPlayers();
            }
        });

        ValueAnimator disappear = ValueAnimator.ofFloat(1f, 0);
        disappear.setDuration(duration);
        disappear.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                shadower.setAlpha((Float) valueAnimator.getAnimatedValue());
            }
        });

        AnimatorSet animSet = new AnimatorSet();

        animSet.play(appear).before(caller);
        animSet.play(caller).before(disappear);
        animSet.start();
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
        slideToRight(player1.fragmentId, player1.screenId);
        slideToLeft(player2.fragmentId, player2.screenId);
    }

    private void showPlayer(Player player) {
        TextView tw = (TextView) findViewById(player.lifeViewId);
        String life = String.valueOf(player.life);
        if (player.life == 6 || player.life == 9) {
            life += '.';
        }

        tw.setText(life);
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

