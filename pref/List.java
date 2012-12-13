package com.svilendobrev.pref;

import android.content.Context;
import android.util.AttributeSet;
import android.preference.PreferenceManager;

public
class List extends android.preference.ListPreference {
    public List( Context context, AttributeSet attrs) { super( context, attrs); }
    public List( Context context) { super( context); }

    String defaults() { return ((PrefActivity) getContext()).defaultStr( getKey() ); }

    @Override
    protected void onAttachedToHierarchy( PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy( preferenceManager);
        setValue( getPersistedString( defaults() ));
    }
}
// vim:ts=4:sw=4:expandtab
