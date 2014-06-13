package com.github.almazko.magic_screen;

import java.io.Serializable;

/**
 * @author Almazko
 */
public class Player implements Serializable {
    final static int DEFAULT_LIFE = 20;

    int life = DEFAULT_LIFE;
    int screenId;
    int fragmentId;
    int lifeViewId;

    public Player(int lifeViewId) {
        this.lifeViewId = lifeViewId;
    }

    public void damage(int value) {

        life -= value;
    }

    public void add(int value) {
        life += value;
    }

}
