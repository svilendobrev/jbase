package com.svilendobrev.ui;

import android.widget.EditText;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class EditText4focus extends EditText {
    /* allows extra views/buttons when there is input-keyboard.
        hide extras when focus=false ; show when touched
        focus=true happens without keyboard being shown.. maybe also show on it if hard-keyboard??
        onCreateInputConnection seems same as focus(true)
    */
    public interface Focuser {
        void focus( boolean on);
        void touched();
    }
    public Focuser focuser;
    public EditText4focus( Context context) { super(context); }
    public EditText4focus( Context context, AttributeSet attrs) { super(context, attrs); }
    public EditText4focus( Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    //at plain lose/gain focus - can be done via void setOnFocusChangeListener(OnFocusChangeListener l) {
    @Override protected void onFocusChanged( boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged( focused, direction, previouslyFocusedRect);
        //Log.d( "onFocusChanged", ""+direction + " "+previouslyFocusedRect );
        if (focuser!=null) focuser.focus( focused);
    }

    //at keyboard hide.. tricky!
    @Override public boolean dispatchKeyEventPreIme( KeyEvent event) {
        //Log.d( "TAGGGGGGGGGGGGG", "dispatchKeyEventPreIme " + event );
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            if (event.getAction() == KeyEvent.ACTION_UP)
                if (focuser!=null) focuser.focus( false);
        return super.dispatchKeyEventPreIme( event);
    }
    /*
    @Override public InputConnection     onCreateInputConnection( EditorInfo outAttrs) {
        Log.d( "onCreateInputConnection", ""+outAttrs);
        return super.onCreateInputConnection( outAttrs);
    }
    */
    @Override public boolean onTouchEvent(MotionEvent event) {
        //XXX very carefull! playing with onTouchEvent isn't much sure.. reacts on all sorts of wrong events
        final int action = event.getActionMasked();
        boolean r = super.onTouchEvent( event);
            if (r)
            //if (isTextEditable() )
            if (onCheckIsTextEditor() && isEnabled() )
            if (action == MotionEvent.ACTION_UP && isFocused() ) //&& !mScrolled) XXX
            { //Log.d( "onTouchEvent", ""+didTouchFocusSelect());
                if (focuser!=null) focuser.touched();
            }
        return r;
    }

}
// vim:ts=4:sw=4:expandtab
