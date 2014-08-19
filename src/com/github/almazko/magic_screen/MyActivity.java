package com.github.almazko.magic_screen;

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
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;

public class MyActivity extends Activity implements View.OnTouchListener {

    private static final String BUNDLE_KEY_PLAYER_1 = "player_1";
    private static final String BUNDLE_KEY_PLAYER_2 = "player_2";
    private static final String BUNDLE_KEY_STAGE = "stage";
    private static final String PREFS_NAME = "mtg_preferences";
    private static final String PREF_KEY_BRIGHTNESS = "manage_brightness";

    private static final float BRIGHTNESS_WAITING = 0.02f;
    private static final int WAITING = 5000;


    static long timeLastAction = 0;

    Handler.Callback totalTimerCallback = new Handler.Callback() {
        Collection<Handler.Callback> callbacks = new LinkedList<>();

        @Override
        public boolean handleMessage(Message msg) {

            long time = System.currentTimeMillis();

            if (manageBrightness && time - timeLastAction > WAITING) {
                isSleep = true;
                setBrightness(BRIGHTNESS_WAITING);
            }

            for (Handler.Callback callback : callbacks) {
                callback.handleMessage(msg);
            }

            Log.i("MAGIC", "test");

            return false;
        }
    };

    enum SStage {DISPOSAL, GAME, PAUSE}

    Stage stage;
    SStage currentStage;

    static boolean isSleep = false;
    static boolean manageBrightness = false;
    static boolean fullScreen = false;


    MediaPlayer playerTick;
    Player player1;
    Player player2;
    java.util.Timer totalTimer;

    final Handler totalTimerHandler = new Handler(totalTimerCallback);
    final Effect effect = new Effect();

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        timeLastAction = System.currentTimeMillis();
        if (isSleep) {
            isSleep = false;
            restoreBrightness();

        }
        return false;
    }

    @Override
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

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(PREF_KEY_BRIGHTNESS, manageBrightness);
        editor.commit();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.main);
        findViewById(R.id.main_view).setOnTouchListener(this);

        if (state != null) {
            currentStage = restoreState(state);
        } else {
            currentStage = SStage.DISPOSAL;
            init(2, 3);
        }

        com.github.almazko.magic_screen.Stage prevStage = stage;
        switch (currentStage) {
            case DISPOSAL:
                stage = new StageDisposal(this);
                break;
            case GAME:
                stage = new StageGame(this);
                break;
            case PAUSE:
                stage = new StagePause(this);
                break;
        }


        showActionsPanel();


        playerTick = MediaPlayer.create(this, R.raw.sound_tick1);
        playerTick.setVolume(0.05f, 0.05f);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        timeLastAction = System.currentTimeMillis();


        if (!isLandscape()) {
            rotatePlayer2Screen();
        }
        runTotalTimer();
        setFullscreenMode();
        positioning();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        manageBrightness = settings.getBoolean("manage_brightness", false);


        stage.begin(prevStage);
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
            screen1sizing.setMargins(0, height / 2, 0, 0);
            screen2sizing = new RelativeLayout.LayoutParams(width, height / 2);
        }

        findViewById(R.id.player_1_screen).setLayoutParams(screen1sizing);
        findViewById(R.id.player_2_screen).setLayoutParams(screen2sizing);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(BUNDLE_KEY_PLAYER_1, player1);
        outState.putSerializable(BUNDLE_KEY_PLAYER_2, player2);
        outState.putString(BUNDLE_KEY_STAGE, currentStage.toString());

        stage.onSave(outState);

        if (totalTimer != null) {
            totalTimer.cancel();
            totalTimer = null;
        }
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

    void showActionsPanel() {

        ActionsFragment fg = (ActionsFragment) getFragmentManager().findFragmentById(R.id.f_actions);
        if (fg == null) {

            fg = ActionsFragment.newInstance(SStage.DISPOSAL);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.replace(R.id.f_actions, fg);
            ft.commit();
        }
    }

    void notice(final View v, final int bgId) {
        effect.blink(v, bgId, 0.15f, 150);
    }

    void playClick() {
        if (playerTick.isPlaying()) {
            playerTick.seekTo(0);
        } else {
            playerTick.start();
        }
    }


    void onStageStart() {
        stage = new StageGame(this);
        stage.begin(stage);
    }

    void onStagePauseToGame() {

        StageGame gameStage = new StageGame(this);
        stage.end(gameStage);

        gameStage.begin(stage);
//
//        gameStage.start
//        timer = new Timer(gameTimerCallback, timer.stop());
//        timer.start(0, 500);
//
//        stageGame();
    }

//    public void onStagePauseToGame() {
//        if (timer != null) {
//            timer.stop();
//        }
//
//    }

    private SStage restoreState(Bundle state) {
        player1 = (Player) state.getSerializable(BUNDLE_KEY_PLAYER_1);
        player2 = (Player) state.getSerializable(BUNDLE_KEY_PLAYER_2);
        return SStage.valueOf(state.getString(BUNDLE_KEY_STAGE));


//
//        if (timer == null) {
//            timer = new Timer(gameTimerCallback, state.getLong(KEY_TIME));
//        }

//        runTotalTimer();
    }

    void runTotalTimer() {


        totalTimerHandler.call
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

    void onStagePauseToDisposal() {
        init(player1.screenId, player2.screenId);


        View shadower = findViewById(R.id.full_screen);

        final MyActivity context = this;
        AnimatorCallback.Callback middleCallback = new AnimatorCallback.Callback() {
            @Override
            public void call() {
                stage.end(null);

                stage = new StageDisposal(context);
                stage.begin(null);
            }
        };

        effect.gameOver(shadower, android.R.color.background_dark, middleCallback, 300);
    }


    private void init(int player1ScreenId, int player2ScreenId) {

        player1 = new Player(R.id.scr1_score);
        player1.fragmentId = R.id.player_1_screen;
        player1.screenId = player1ScreenId;

        player2 = new Player(R.id.scr2_score);
        player2.fragmentId = R.id.player_2_screen;
        player2.screenId = player2ScreenId;
    }

    void showPlayerLife(@NotNull final Player player) {
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

    void rotatePlayer2Screen() {
        findViewById(R.id.player_2_score).setRotation(180);
        findViewById(R.id.player_2_screen).setRotation(180);
    }

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

    static boolean isRight(@NotNull final View v, @NotNull final MotionEvent e) {
        return e.getX() > v.getWidth() / 2;
    }

    static boolean isUp(@NotNull final View v, @NotNull final MotionEvent e) {
        return e.getY() < v.getHeight() / 2;
    }
}

