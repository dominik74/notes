package com.nickstudio.notes;

import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.LinkedList;
import java.util.List;

public class SoftKeyboardStateWatcher implements ViewTreeObserver.OnGlobalLayoutListener {

    public interface SoftKeyboardStateListener {
        void onSoftKeyboardOpened();
        void onSoftKeyboardClosed();
    }

    private final List<SoftKeyboardStateListener> listeners = new LinkedList<SoftKeyboardStateListener>();
    private final Context context;
    private final View activityRootView;
    private boolean    isSoftKeyboardOpened;

    public SoftKeyboardStateWatcher(Context context, View activityRootView) {
        this(context, activityRootView, false);
    }

    public SoftKeyboardStateWatcher(Context context, View activityRootView, boolean isSoftKeyboardOpened) {
        this.activityRootView     = activityRootView;
        this.isSoftKeyboardOpened = isSoftKeyboardOpened;
        this.context = context;

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();

        if (!isSoftKeyboardOpened && heightDiff > dpToPx(context, 200)) {
            isSoftKeyboardOpened = true;
            notifyOnSoftKeyboardOpened();
        } else if (isSoftKeyboardOpened && heightDiff <= dpToPx(context, 200)) {
            isSoftKeyboardOpened = false;
            notifyOnSoftKeyboardClosed();
        }
    }

    public boolean isSoftKeyboardOpened() {
        return isSoftKeyboardOpened;
    }

    public void addSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.add(listener);
    }

    public void removeSoftKeyboardStateListener(SoftKeyboardStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyOnSoftKeyboardOpened() {
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardOpened();
            }
        }
    }

    private void notifyOnSoftKeyboardClosed() {
        for (SoftKeyboardStateListener listener : listeners) {
            if (listener != null) {
                listener.onSoftKeyboardClosed();
            }
        }
    }

    private float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
}