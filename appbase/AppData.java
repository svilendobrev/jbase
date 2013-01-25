package com.svilendobrev.appbase;

import com.svilendobrev.jbase.Log;
import com.svilendobrev.jbase.Error;
import com.svilendobrev.pref.Prefs;

import android.content.Context;

public abstract class AppData {

    public static boolean inited = false;

    private int refcounter = 0;

    public Context context;
    public Prefs prefs;

    public AppData( Context context) {
        this.context = context.getApplicationContext();
        prefs = new Prefs( this.context);
    }

    public void open() {
        if (refcounter==0) _open();
        refcounter++;
        Log.d("===========appdata open: " + refcounter);
    }
    public void close() {
        Log.d("===========appdata close: " + refcounter);
        refcounter--;
        if (refcounter==0) _close();
    }

    public void factoryReset() {
        refcounter = 0;
    }

    //public abstract Error init();
    protected void _open() {}
    protected void _close() {}
}

// vim:ts=4:sw=4:expandtab
