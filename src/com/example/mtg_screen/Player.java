package com.example.mtg_screen;

import android.widget.TextView;

/**
 * @author Almazko
 */
public class Player {
    final static int DEFAULT_LIFE = 20;

    int life = DEFAULT_LIFE;
    int screenId;
    int fragmentId;
    TextView lifeView;

    public Player(TextView lifeView) {
        this.lifeView = lifeView;
    }

    public void damage(int value) {

        life -= value;
    }

    public void add(int value) {
        life += value;
    }

}
