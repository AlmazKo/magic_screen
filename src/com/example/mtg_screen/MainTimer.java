package com.example.mtg_screen;

import java.util.TimerTask;

/**
 * @author Almazko
 */
public class MainTimer  extends TimerTask {

    public static long START_TIME = System.currentTimeMillis();

    public void run() {
        START_TIME = System.currentTimeMillis();
    }
}
