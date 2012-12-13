package com.svilendobrev.ui;

import com.svilendobrev.jbase.Log;
import com.svilendobrev.jbase.funk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SlidingDrawer;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.graphics.Rect;
import android.content.res.TypedArray;

public class ExpandArea extends SlidingDrawer {
    private final Rect tmpRect = new Rect();

    View longClickableView;
    public boolean isVertical;
    public ExpandArea( Context context, AttributeSet attrs) {
        super(context, attrs);
        //HACK no other way - SlidingDrawer.mVertical is inaccessible, android.R.styleable is inaccessible
        int orie = attrs.getAttributeIntValue( "http://schemas.android.com/apk/res/android", "orientation", ORIENTATION_VERTICAL); //what namespace to use?
        /*
        //above namespace is way too specific here...
        for (int n=attrs.getAttributeCount(), i=0; i<n; i++) {
            //Log.d("55555555555555 :" + i + " "+attrs.getAttributeName(i) );
            if (funk.eq( attrs.getAttributeName(i), "orientation")) {
                orie = attrs.getAttributeIntValue( i, ORIENTATION_VERTICAL);
                break;
            }
        }
        */

        isVertical = (ORIENTATION_VERTICAL == orie);
        Log.d("isVertical:" + isVertical  + "/"+orie);
    }

    boolean pressedOnHandle = false;

    @Override
    public boolean onInterceptTouchEvent( MotionEvent event) {
        longClickableView = null;
        int x = (int) event.getX();
        int y = (int) event.getY();
        getHandle().getHitRect(tmpRect);

        pressedOnHandle = tmpRect.contains( x,y);
        //Log.d( "1111111 pressedOnHandle"+ pressedOnHandle + " " + x + " " +y + " " +tmpRect);

        if (pressedOnHandle) {
            View child = findChild4Event( x, y);
            //Log.d( "1111111 child: " + child);
            //Log.d( "eofindLongClickable "+child);
            if (child != null) {
                if (child.isClickable()) {
                    //Log.d( "1111111 found clickable");
                    return false; //give control to the child
                }
                if (child.isLongClickable()) {
                    //Log.d( "1111111 found LONG clickable");
                    longClickableView = child;
                }
            }
        }
        //Log.d( "longClickableView " +longClickableView);
        return super.onInterceptTouchEvent( event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d( "22222"+ pressedOnHandle + " " +longClickableView );
        if (pressedOnHandle && (longClickableView != null))
            //Log.d("onTouchEvent " + event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_CANCEL:
                    //Log.d("zzzzz hasPerformedLongClick:" + hasPerformedLongClick);
                    setPressed( false);
                    //refreshDrawableState();
                    if (!hasPerformedLongClick)
                        removeCallbacks( pendingCheck4LongPress);
                    else
                        return true;
                    break;

                case MotionEvent.ACTION_DOWN:
                    //Log.d("zzzzz postCheckForLongClick");
                    setPressed( true);
                    //refreshDrawableState();
                    postCheckForLongClick();
                    break;
            }
        return super.onTouchEvent( event);
    }

    boolean hasPerformedLongClick = false;

    class CheckForLongPress implements Runnable {
        private int origWindowAttachCount;
        public void run() {
            if (isPressed()
                    && (getParent()!= null) && hasWindowFocus()
                    && origWindowAttachCount == getWindowAttachCount()) {

                funk.assertTrue( longClickableView != null);
                hasPerformedLongClick = longClickableView.performLongClick();

                /*
                if (performLongClick()) {
                    //Log.d("longClick");
                    hasPerformedLongClick = true;
                }
                */
            }
        }
        public void rememberWindowAttachCount() {
            origWindowAttachCount = getWindowAttachCount();
        }
    }

    CheckForLongPress pendingCheck4LongPress = new CheckForLongPress();

    private void postCheckForLongClick() {
        hasPerformedLongClick = false;
        pendingCheck4LongPress.rememberWindowAttachCount();
        postDelayed( pendingCheck4LongPress, ViewConfiguration.getLongPressTimeout());
    }

    /*
    public boolean onLongClick(View v) {
        boolean res = false;
        //Log.d("onLongClick "+longClickableView);
        if (longClickableView != null)
            res = longClickableView.performLongClick();
        return res;
    }
    */

    ///////// FIXME findChild4Event copy/pasted from dnd.java
    private View findChild4Event( int x, int y) {
        int[] childCoords = new int[2]; //unused
        return findChild4Event( this, x, y, childCoords);
    }
    private View findChild4Event( ViewGroup container, int x, int y, int[] childCoords) {
        final Rect r = tmpRect;
        final int count = container.getChildCount();
        final int scrolledX = x + container.getScrollX();
        final int scrolledY = y + container.getScrollY();
        //Log.d( "findChild4Event "+container +  " " + x + ","+y);
        for (int i = count; --i >= 0; ) {
            final View child = container.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            child.getHitRect(r);
            if ( r.contains( scrolledX, scrolledY)) {
                if (!(child instanceof ViewGroup)) {
                    childCoords[0] = x;
                    childCoords[1] = y;
                    return child;
                }
                x = scrolledX - child.getLeft();
                y = scrolledY - child.getTop();
                return findChild4Event((ViewGroup) child, x, y, childCoords);
            }
        }
        return null;
    }

}

// vim:ts=4:sw=4:expandtab

