package com.svilendobrev.ui;

import android.widget.EditText;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;

public class EditText4focus extends EditText {
    public interface Focuser {
        void focus( boolean on);
    }
    public Focuser focuser;
    public EditText4focus( Context context) { super(context); }
    public EditText4focus( Context context, AttributeSet attrs) { super(context, attrs); }
    public EditText4focus( Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    //at plain lose/gain focus - can be done via void setOnFocusChangeListener(OnFocusChangeListener l) {
    @Override
    protected void onFocusChanged( boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged( focused, direction, previouslyFocusedRect);
        if (focuser!=null) focuser.focus( focused);
    }

    //at keyboard hide.. tricky!
    @Override
    public boolean dispatchKeyEventPreIme( KeyEvent event) {
        //Log.d( "TAGGGGGGGGGGGGG", "dispatchKeyEventPreIme " + event );
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
            if (event.getAction() == KeyEvent.ACTION_UP)
                if (focuser!=null) focuser.focus( false);
        return super.dispatchKeyEventPreIme( event);
    }
}
// vim:ts=4:sw=4:expandtab
