package com.github.almazko.magic_screen;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.View;
import android.widget.TextView;

/**
 * @author Almazko
 */
public class Effect {


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



    void gameOver(final View shadower, final int bgId, AnimatorCallback.Callback middle, final int duration) {
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

        ValueAnimator caller = AnimatorCallback.get(middle);

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
}
