package com.svilendobrev.pref;

import android.content.Context;
import android.util.AttributeSet;
import android.preference.PreferenceManager;

public
class CheckBox extends android.preference.CheckBoxPreference {
    public CheckBox( Context context, AttributeSet attrs, int defStyle) { super( context, attrs, defStyle); }
    public CheckBox( Context context, AttributeSet attrs) { super( context, attrs); }
    public CheckBox( Context context) { super( context); }

    Boolean defaults() { return ((PrefActivity) getContext()).defaultBool( getKey() ); }

    @Override
    protected void onAttachedToHierarchy( PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy( preferenceManager);
        setChecked( getPersistedBoolean( defaults() ));
    }
}
// vim:ts=4:sw=4:expandtab
