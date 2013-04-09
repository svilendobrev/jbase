package com.svilendobrev.appbase;

import com.svilendobrev.jbase.funk;

import android.view.MenuItem;
import android.content.Intent;
import android.app.Activity;

public
class MenuDescr2 extends MenuDescr {

public static
class DA extends MenuDescr.D {
    public ResultCallback<? extends ActivityBase> callback;
    public DA( Class<? extends Activity> activityClass, ResultCallback<? extends ActivityBase> callback) {
        super( activityClass);
        this.callback = callback;
    }
    public void start( ActivityBase context, MenuItem item) {
        if (callback != null && activityClass != null)
            context.startChild( activityClass, callback);
        else
            super.start( context, item);
    }
}

public static
class ResultCallback<T> {
    public T context;

    public void setContext( ActivityBase context) {
        this.context = (T) context;
    }
    public void resultOk( Intent data) {}
    public void resultCancel( Intent data) {}
}

    public D put( int id, Class<? extends Activity> activityClass, ResultCallback<? extends ActivityBase> callback) {
        return put( id, new DA( activityClass, callback));
    }
    //same ctors
    public MenuDescr2( int resId)               { super( resId); }
    public MenuDescr2( int resId, int titleId)  { super( resId, titleId); }
    public MenuDescr2( int resId, String title) { super( resId, title); }

} //MenuDescr2

// vim:ts=4:sw=4:expandtab
