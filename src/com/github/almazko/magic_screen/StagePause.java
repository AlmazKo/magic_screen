package com.github.almazko.magic_screen;

import org.jetbrains.annotations.NotNull;

/**
 * @author Almazko
 */
public class StagePause extends Stage {

    public StagePause(@NotNull MyActivity context) {
        super(context);
    }

    @Override
    public void begin(Stage prev) {

    }

    @Override
    public void end(Stage next) {
        super.end(next);
    }
}
