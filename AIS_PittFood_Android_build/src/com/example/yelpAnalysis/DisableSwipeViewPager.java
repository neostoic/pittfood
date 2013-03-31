package com.example.yelpAnalysis;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class DisableSwipeViewPager extends ViewPager{

	private boolean enabled;
	
	public DisableSwipeViewPager(Context context) {
		super(context);
	}
	
	public DisableSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return false;
    }
}
