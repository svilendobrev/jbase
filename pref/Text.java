package com.svilendobrev.pref;

import android.content.Context;
import android.util.AttributeSet;
import android.preference.PreferenceManager;

public
class Text extends android.preference.EditTextPreference {
    public Text( Context context, AttributeSet attrs, int defStyle) { super( context, attrs, defStyle); }
    public Text( Context context, AttributeSet attrs) { super( context, attrs); }
    public Text( Context context) { super( context); }

    String defaults() { return ((PrefActivity) getContext()).defaultStr( getKey() ); }

    @Override
    protected void onAttachedToHierarchy( PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy( preferenceManager);
        setText( getPersistedString( defaults() ));
    }
}
// vim:ts=4:sw=4:expandtab
