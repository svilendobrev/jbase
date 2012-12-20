package com.svilendobrev.appbase;

import com.svilendobrev.pref.Prefs;
import com.svilendobrev.jbase.funk;
//import com.svilendobrev.appbase.ActivityBase;
//import com.svilendobrev.appbase.MenuDescr.ResultCallback;

import android.content.Intent;
import android.os.Message;
import android.os.Bundle;
import android.os.SystemClock;

public abstract class AppActivity<S extends StateApp> extends ActivityBase<S> {
    @Override public void onCreate( Bundle bundle) {
        if (getAppData() == null)
            createAppData();

        if (getLastNonConfigurationInstance() == null) //no onRetainNonConfigurationInstance
            getAppData().open();

        super.onCreate( bundle);
        if (AppData.inited) {
            if (s.needRefresh) refresh();
            if (funk.not( s.runningTasks))
                updateUI();
        }
    }
    //@Override protected boolean needNewState() {
    //    return super.needNewState() || !AppData.inited;
    //}

    @Override public void onInitState() {
        super.onInitState();
        if (!AppData.inited) init();
    }

    protected abstract void createAppData();
    public abstract AppData getAppData();
    public void factoryReset() {}

    @Override public Object onRetainNonConfigurationInstance() {
        getAppData().open(); //prevent closing in onDestroy
        return new Boolean(true);//just return something to prove we have been here
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        getAppData().close();
        if (factory_reset) factoryReset();
    }

    public void init() {
        debug("init");
    }
    public void refresh() {
        debug("refresh");
        s.needRefresh = false;
    }

    @Override public void updateUI() {
        super.updateUI();
        if (!s.needRefresh && AppData.inited)
            _updateUI();
    }
    public abstract void _updateUI();

    boolean factory_reset;

    static public MenuDescr2.ResultCallback<AppActivity> preferencesCallback = new MenuDescr2.ResultCallback<AppActivity>() { public void resultCancel( Intent data) {
        boolean init, update, restart;
        this.context.factory_reset = init = update = restart = false;
        for (Prefs.Pref p : this.context.getAppData().prefs.changes) {
            if (p.needs_common_action( "factory_reset")) this.context.factory_reset = true;
            else if (p.needs_common_action( "restart")) restart = true;
            else if (p.needs_common_action( "init")) init = true;
            else if (p.needs_common_action( "update")) update = true;
            //else updateUI is called in super.onResumedFromChild
        }
        this.context.getAppData().prefs.changes.clear();

        if (this.context.factory_reset) this.context.restart(false);
        else if (restart) new Restart().start( this.context);
        else if (init) this.context.init();
        else if (update) this.context.refresh();
    }};

    static public class Restart extends FlowApp<AppActivity> {
        // keep AppData open until the new activity instance starts
        AppData z;
        boolean running = false;
        @Override protected void _start( ActivityBase activity) {
            z = ((AppActivity) activity).getAppData();
            super._start( activity);
        }
        @Override public AppData getAppData() { return z; }

        @Override public void attach( AppActivity a) {
            super.attach( a);
            if (running) finish();
        }
        @Override public void init() {
            running = true;
            activity.restart();
        }
    }
}


// vim:ts=4:sw=4:expandtab
