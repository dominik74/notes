package com.nickstudio.notes;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.Toolbar;

public class ToolbarController {
    private Toolbar toolbar;
    private Window window;

    public ToolbarController(Toolbar toolbar, Window window) {
        this.toolbar = toolbar;
        this.window = window;
    }

    public void setColor(@ColorInt int color) {
        toolbar.setBackground(new ColorDrawable(color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    public void animateColor(@ColorInt int color) {
        int colorFrom = window.getStatusBarColor();

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, color);
        colorAnimation.setDuration(100); // milliseconds (150)
        colorAnimation.addUpdateListener(animator -> setColor((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }
}
