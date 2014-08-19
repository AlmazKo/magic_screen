package com.github.almazko.magic_screen;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Almazko
 */
public class TimerHandler extends Handler {

    Collection<Callback> callbacks = new LinkedList<>();


    @Override
    public void handleMessage(Message msg) {

        long time = System.currentTimeMillis();

        msg.s

        if (manageBrightness && time - timeLastAction > WAITING) {
            isSleep = true;
            setBrightness(BRIGHTNESS_WAITING);
        }

        for (Callback callback : callbacks) {
            callback.handleMessage(msg);
        }

        Log.i("MAGIC", "test");

        return;
    }

    void addCallback(Callback callback) {
        callbacks.add(callback);
    }
}
