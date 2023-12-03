package org.smartregister.brac.hnpp.custom_view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class NonSwipeableViewPager extends ViewPager {

    public NonSwipeableViewPager(Context context) {
        super(context);
    }

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Disable touch events
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Disable touch events
        return true;
    }
}