package com.svilendobrev.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public
class AutoCompleteTextView4noreplace extends AutoCompleteTextView {
    public AutoCompleteTextView4noreplace( Context context) { super( context); }
    public AutoCompleteTextView4noreplace( Context context, AttributeSet attrs) { super( context, attrs); }
    public AutoCompleteTextView4noreplace( Context context, AttributeSet attrs, int defStyle) { super( context, attrs, defStyle); }

    //all this to allow clink-on-item to decide whether to replace text or not
    @Override
    protected void replaceText( CharSequence text) {    //dont do anything
        clearComposingText();
    }
    //then use this ; .setText fires autocomplete ??
    public void do_replaceText( CharSequence text) { super.replaceText( text); }
};

// vim:ts=4:sw=4:expandtab
