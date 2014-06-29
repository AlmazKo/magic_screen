package com.github.almazko.magic_screen;

import android.animation.ValueAnimator;

/**
 * @author Almazko
 */
abstract class AnimatorCallback {

    public static interface Callback {
        public void call();
    }

    static ValueAnimator get(final Callback callback) {
        ValueAnimator animator = ValueAnimator.ofInt(0, 0);
        animator.setDuration(0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            boolean isCall = false;

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!isCall) {
                    isCall = true;
                    callback.call();
                }
            }
        });


        return animator;
    }
}
