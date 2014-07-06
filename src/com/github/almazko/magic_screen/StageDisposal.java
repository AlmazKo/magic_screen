package com.github.almazko.magic_screen;

import android.view.View;
import android.widget.TextView;
import org.jetbrains.annotations.*;

/**
 * @author Almazko
 */
public class StageDisposal extends Stage {

    final static int MAX_SCREENS = 7;


    public StageDisposal(@NotNull MyActivity context) {
        super(context);
    }

    @Override
    public void begin(@Nullable Stage prev) {
        addListeners();
        showPlayers();
    }

    @Override
    public void end(@Nullable Stage next) {
        context.findViewById(R.id.player_1_screen).setOnTouchListener(null);
        context.findViewById(R.id.player_2_screen).setOnTouchListener(null);

        context.findViewById(R.id.player_1_description).setVisibility(View.GONE);
        context.findViewById(R.id.player_2_description).setVisibility(View.GONE);


        super.end(next);
    }


    void showPlayers() {
        showPlayer(player1);
        showPlayer(player2);
        context.slideToRight(player1.fragmentId, player1.screenId);
        context.slideToLeft(player2.fragmentId, player2.screenId);
    }


    private void showPlayer(@NotNull Player player) {
        TextView tw = (TextView) context.findViewById(player.lifeViewId);
        String life = String.valueOf(player.life);
        if (player.life == 6 || player.life == 9) {
            life += '.';
        }

        tw.setText(life);
    }


    private void addListeners() {
        View v;

        v = context.findViewById(R.id.player_1_screen);

        v.setOnTouchListener(new OnSwipeTouchListener(context) {
            @Override
            public void onSwipeLeft() {
                changeScreenToLeft(player1);
            }

            @Override
            public void onSwipeRight() {
                changeScreenToRight(player1);
            }
        });

        v = context.findViewById(R.id.player_2_screen);
        v.setOnTouchListener(new OnSwipeTouchListener(context) {
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

    void changeScreenToRight(final Player player) {
        if (player.screenId >= MAX_SCREENS) {
            player.screenId = 1;
        } else {
            player.screenId++;
        }

        context.slideToRight(player.fragmentId, player.screenId);
    }

    void changeScreenToLeft(final Player player) {
        if (player.screenId <= 1) {
            player.screenId = MAX_SCREENS;
        } else {
            player.screenId--;
        }

        context.slideToLeft(player.fragmentId, player.screenId);
    }
}
