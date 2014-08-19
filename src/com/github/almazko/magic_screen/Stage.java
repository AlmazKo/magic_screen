package com.github.almazko.magic_screen;

import android.os.Bundle;
import org.jetbrains.annotations.*;

/**
 * @author Almazko
 */
abstract class Stage {

    protected MyActivity context;
    protected Player player1;
    protected Player player2;

    public Stage(@NotNull MyActivity context) {
        this.context = context;
        this.setContext(context);
    }


    void onSave(Bundle outState) {

    }

    public void setContext(@NotNull MyActivity context) {
        this.context = context;
        player1 = context.player1;
        player2 = context.player2;
    }

    abstract public void begin(@Nullable Stage prev);

    public void end(@Nullable Stage next) {
//        context = null;
//        player1 = null;
//        player2 = null;
    }
}
