package com.github.almazko.magic_screen;

import android.os.Handler;
import android.os.Message;

/**
 * @author Almazko
 */
public class Timer extends java.util.Timer {

    long time = 0;
    long startTime = 0;

    boolean isStarted = false;

    private Callback callback;

    public static interface Callback {
        public void handle(long passedTimeMs);
    }

    public Timer(Callback callback) {
        this.callback = callback;
    }

    public Timer(Callback callback, long time) {

        this.callback = callback;
        this.time = time;
    }

    final Handler h = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }

            callback.handle(time + System.currentTimeMillis() - startTime);

            return false;
        }
    });

    long stop() {
        if (startTime == 0) {
            return 0;
        }

        time += System.currentTimeMillis() - startTime;
        this.cancel();

        return time;
    }

    public void start(long delay, long period) {

        scheduleAtFixedRate(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        h.sendEmptyMessage(0);
                    }
                },
                delay, period
        );
        isStarted = true;
    }
}
