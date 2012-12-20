package com.svilendobrev.pref;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/* new Preference types:
usage: actual thing-inheriting-Preference should get the default value
and use it in ctor / onCreateDialogView / onAttachedToHierarchy e.g.

    String defaults() { return ((PrefActivity) getContext()).defaultStr( getKey() ); }

    protected void onAttachedToHierarchy( PreferenceManager preferenceManager) {
        super.onAttachedToHierarchy( preferenceManager);
        setText( getPersistedString(  defaults() )); }

    do not use onSetInitialValue / onGetDefaultValue - xml-only, and not always called
*/

public abstract
class PrefActivity extends PreferenceActivity {

    //overload these
    abstract public    Prefs prefs();   // { return AppData.z.prefs; }
    abstract protected int resource();  // { return R.xml.preferences; }
    /////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource( resource() );
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( prefs() );
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( prefs() );
    }

    public String  defaultStr(  String key) { return prefs().getStr( key); }
    public Integer defaultInt(  String key) { return prefs().getInt( key); }
    public Boolean defaultBool( String key) { return prefs().getBool( key); }
}
// vim:ts=4:sw=4:expandtab
