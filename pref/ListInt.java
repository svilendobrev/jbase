package com.svilendobrev.pref;

import android.content.Context;
import android.util.AttributeSet;
import android.preference.PreferenceManager;

public
class ListInt extends android.preference.ListPreference {
    public ListInt( Context context, AttributeSet attrs) { super( context, attrs); }
    public ListInt( Context context) { super( context); }

    Integer defaults() { return ((PrefActivity) getContext()).defaultInt( getKey() ); }

    @Override
    protected void onAttachedToHierarchy( PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy( preferenceManager);
        super.setValue( ""+getPersistedInt( defaults() ));
    }

    @Override
    public void setValue( String value) {
        super.setValue( value);
        persistInt( Integer.parseInt( value) );
    }
    //XXX hack this off as super.setValue will call it
    @Override
    protected boolean persistString( String value) { return true; }
    //XXX hack this off just in case to avoid getPersistedString
    @Override
    protected void onSetInitialValue( boolean restoreValue, Object defaultValue) { }
}
// vim:ts=4:sw=4:expandtab
