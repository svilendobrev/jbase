package com.svilendobrev.appbase;

import com.svilendobrev.jbase.funk;
import com.svilendobrev.jbase.Log;
import com.svilendobrev.jbase.Error;
import com.svilendobrev.jbase.datetime;
//import com.svilendobrev.appbase.StateBase;
//import com.svilendobrev.appbase.Flow;
//import com.svilendobrev.ui.MenuDescr;
//import com.svilendobrev.ui.ManagedDialog;
import com.svilendobrev.appbase.MenuDescr2.ResultCallback;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.preference.PreferenceManager;

import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.Configuration;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.MenuInflater;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/* These are the possible ways to save/load activity state:
- Starting a new activity - no state passed
    save: -
    load: onCreate creates a new state

- Programmatic restart of the activity - through activity2state
    save: onDestroy puts the state in activity2state
    load: onCreate gets the state from activity2state

- Starting activity which has been destroyed by the system to free memory - through activity2state
    save: onSaveInstanceState puts the state in activity2state
    load: onCreate gets the state from activity2state

*** disabled cause it overlaps with onSaveInstanceState
- Restart caused by a configuration change - e.g. because of orientation change
    save: onRetainNonConfigurationInstance - _hopefully_ called by the system on the old activity
    load: onCreate - gets the state using getLastNonConfigurationInstance
*/

public abstract class ActivityBase<S extends StateBase> extends Activity {

    public void debug( String s) { Log.d( getClass(), s); }

    public ProgressDialog progressDlg;
    boolean needRestart, needSaveState;

    static Hashtable<String, StateBase> activity2state = new Hashtable();

    public MenuDescr optionsMenu;
    private HashMap< View, MenuDescr> contextMenus = new HashMap();

    void _updatetime() {
        datetime.updateTimeZone();
        datetime.fixAllDateFormats();
        datetime.dtfm.fixAllDateFormats( getApplicationContext() );
    }
    protected void setPreferredTheme() {
        Resources res = getResources();
        String pkg = getPackageName();

        int prefId = res.getIdentifier( "pref_theme", "string", pkg);
        if (prefId == 0) return;

        int defaultId = res.getIdentifier( "pref_theme_default", "string", pkg);
        if (defaultId == 0) return;

        String theme = PreferenceManager.getDefaultSharedPreferences( this)
                        .getString( getString( prefId), getString( defaultId));
        int themeId = res.getIdentifier( theme, "style", pkg);
        if (themeId == 0) return;
        setTheme( themeId);
    }

    static public interface StateFactory<T> {
        public T make();
    }

    public S s;
    public StateFactory getStateFactory() {
        return new StateFactory<StateBase>() {
            public StateBase make() { return new StateBase(); }
        };
    }
    void makeState( StateFactory<S> factory) {
        s = factory.make();
    }

    private boolean calledLoadInitState, calledNoState, calledWithState; //make sure super is called

    @Override public void onCreate( Bundle savedInstanceState) {
        debug("onCreate");
        setPreferredTheme();
        super.onCreate(savedInstanceState);
        _updatetime();
        setupMenu();

        s = (S) activity2state.remove( getClass().getName());
        onCreate_NoState(); funk.assertTrue( calledNoState);
        if (s == null || needNewState()) {
            makeState( getStateFactory());
            onInitState();
        } else
            onLoadState();
        funk.assertTrue( calledLoadInitState);
        onCreate_WithState(); funk.assertTrue( calledWithState);
    }
    protected boolean needNewState() { return false; }

    public void onCreate_NoState() { calledNoState = true; }//does not depend on state data
    public void onCreate_WithState() {
        calledWithState = true;
        debug("onCreate_WithState " + s);
    }
    public void onInitState() { //executed once only at first activity startup
        debug("onInitState");
        calledLoadInitState = true;
    }
    public void onLoadState() {
        debug("onLoadState");
        calledLoadInitState = true;
        for (Flow t : s.runningTasks)
            t.attach( this);
    }
    public void onSaveState() {
        debug("onSaveState");
        for (Flow t : s.runningTasks)
            t.detach();
    }

    //lifecycle: http://developer.android.com/reference/android/app/Activity.html
    //TODO: use onSaveInstanceState(Bundle) to save session (then use it in onCreate)
    //TODO: save persistent stuff in onPause
    private boolean _paused = false;
    boolean _resumedFromChild = false;

    @Override public void onPause() {
        super.onPause();
        debug("onPause");
        _paused = true;
    }
    @Override public void onResume() {
        super.onResume();
        debug("onResume: _paused=" + _paused + " ");
        _updatetime();
        if (_paused) {
            _paused = false;
            if (_resumedFromChild)
                onResumedFromChild();
            else
                updateOnResume();
            _resumedFromChild = false;
        }
    }
    @Override protected void onStart() {
        super.onStart();
        debug("onStart");
    }
    @Override protected void onRestart() {
        super.onRestart();
        debug("onRestart");
    }
    @Override protected void onStop() {
        super.onStop();
        debug("onStop");
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        debug("onDestroy");

        if (needSaveState) {
            onSaveState();
            activity2state.put( getClass().getName(), s);
        }

        if (needRestart) {
            Intent i = new Intent( this, getClass());
            startActivity( i);
        }
    }

    protected void updateOnResume() {
        debug("updateOnResume");
        updateUI();
    }

    protected void onResumedFromChild() {
        debug("onResumedFromChild");
        updateUI();
    }

    protected void restart() { restart( true); }
    protected void restart( boolean saveState) {
        needRestart = true;
        needSaveState = saveState;
        finish();
    }

    protected void setupMenu() {}

    public void updateUI() {
        debug("updateUI");
    }

    public View currentContextView;

    public Handler handler = new Handler();

    public void showProgress() { showDialog( ManagedDialog.Progress.KEY); }
    public void hideProgress() {
        if (progressDlg != null)
            dismissDialog( ManagedDialog.Progress.KEY);
        progressDlg = null;
    }
    public void updateProgress( ManagedDialog.Progress setup) {
        if (progressDlg == null) {
            loadDialog( setup.key, setup);
        } else if (setup.horizontal() == progressDlg.isIndeterminate()) {
            hideProgress();
            setup.show( this);
        } else
            setup.update( this, progressDlg);
    }

    public void showDataError( Error err)   { handleError( err); }
    public void showDataError( int msgId )  { handleError( new Error( msgId)); }
    public void showDataError( String txt)  { handleError( txt); }

    private Integer get_text_id( String name) {
        Resources res = getResources();
        String pkg = getPackageName();
        int id = res.getIdentifier( name, "string", pkg);
        if (id == 0)
            return null;
        return id;
    }
    public int title4error()    { return get_text_id("title_error"  ); }
    public int btn_ok     ()    { return get_text_id("btn_ok"       ); }
    public int btn_cancel ()    { return get_text_id("btn_cancel"   ); }
    public int btn_yes    ()    { return get_text_id("btn_yes"      ); }
    public int btn_no     ()    { return get_text_id("btn_no"       ); }
    public int title4progress() { return get_text_id("title_progress4loading"); }

    public void handleError( Object e) {
        String t;
        if (e instanceof Error) {
            //show error for err.msgId or else err.msg
            Error err = (Error) e;
            Integer msgId = err.msgId;
            if (msgId!=null) {
                if (handleErrorById( err))
                    return;  //shown
                showInfoDlg( title4error(), msgId);
                return;
            } else
                t = err.msg;
        } else
            t = (String) e;
        showInfoDlg( title4error(), t);
    }

    //put+override this in application-specific ActivityBase
    protected boolean handleErrorById( Error err) { return false; }

    @Override public void onConfigurationChanged( Configuration newConfig) {
        debug("onConfigurationChanged: " + newConfig.orientation);
        super.onConfigurationChanged( newConfig);
    }

    @Override protected void onSaveInstanceState( Bundle outState) {
        debug("onSaveInstanceState");
        super.onSaveInstanceState( outState);
        needSaveState = true;
    }
    @Override protected void onRestoreInstanceState( Bundle state) {
        debug("onRestoreInstanceState");
        super.onRestoreInstanceState( state);
        needSaveState = false; //onSaveInstanceState can happen without onDestroy
    }

    ////////////// Perform UI changes only when readyForUiChanges()
    private boolean progressFocused = false;
    private ArrayList<Runnable> runnablesUI = new ArrayList();

    public void setProgressFocused( boolean hasFocus) {
        debug("Progress focus changed: " + hasFocus);
        progressFocused = hasFocus;
        if (!progressFocused) return; //only the first time we get focused is important for runnablesUI
        if (readyForUiChanges())
            onReadyForUiChanges();
    }
    public boolean readyForUiChanges() {
        return hasWindowFocus() || progressFocused;
    }
    protected void onReadyForUiChanges() {
        debug("onReadyForUiChanges");
        for (Runnable r : runnablesUI)
            r.run();
        runnablesUI.clear();
    }
    //run ui runnable when hasWindowFocus
    public void runUIupdater( Runnable updater) {
        if (!readyForUiChanges())
            runnablesUI.add( updater);
        else
            updater.run();
        debug("runUIupdater: " + readyForUiChanges() + " " + runnablesUI.size());
    }


    @Override public void onWindowFocusChanged( boolean hasFocus) {
        super.onWindowFocusChanged( hasFocus);
        debug("onWindowFocusChanged: " + hasFocus);
        if (readyForUiChanges())
            onReadyForUiChanges();
    }


    @Override protected Dialog onCreateDialog(final int id) {
        debug("onCreateDialog: " + id);
        ManagedDialog setup = s.dialog2setup.get( id);
        if (setup != null)
            return setup.create( this);
        Log.d("Missing managed dialog! Perhaps onRestoreInstanceState wants to restore it. WHY???");
        return null;
    }
    @Override protected void onPrepareDialog(int id, Dialog dialog) {
        debug("onPrepareDialog: " + id +" " +dialog);
    }

    @Override public boolean onCreateOptionsMenu( Menu menu) {
        if (optionsMenu != null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate( optionsMenu.resId, menu);
            return true;
        }
        return false;
    }

    @Override public boolean onPrepareOptionsMenu( Menu menu) {
        optionsMenu.update( menu);
        return super.onPrepareOptionsMenu( menu);
    }

    @Override public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu( menu, v, menuInfo);
        currentContextView = v;
        MenuDescr d = contextMenus.get( v);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate( d.resId, menu);
        d.update( menu, menuInfo);
    }
    @Override public boolean onOptionsItemSelected( MenuItem item) {
        optionsMenu.start( this, item);
        return true;
    }
    @Override public boolean onContextItemSelected( MenuItem item) {
        MenuDescr d = contextMenus.get( currentContextView);
        d.start(this, item);
        return true;
    }

    public void setContextMenu( View v, MenuDescr m) {
        registerForContextMenu( v);
        contextMenus.put( v, m);
    }

    static private int childKey = 0;
    static protected HashMap< Integer, ResultCallback<? extends ActivityBase>> callbacks = new HashMap();

    public void startChild( Class<? extends Activity> childClass, ResultCallback<? extends ActivityBase> callback) {
        Intent i = new Intent(this, childClass);
        startChild( i, callback);
    }

    @Override
    public void startActivity(Intent i) { // we don't support activities which live on their own
        startChild( i, null);
    }

    public void startChild( Intent i, ResultCallback<? extends ActivityBase> callback) {
        callbacks.put( childKey, callback);
        startActivityForResult( i, childKey);
        childKey++;
        if (childKey > 100) childKey = 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        debug("onActivityResult");
        _resumedFromChild = true;

        ResultCallback<? extends ActivityBase> callback = callbacks.get( requestCode);

        if (callback != null) {
            callback.setContext(this);
            if (resultCode == RESULT_CANCELED)
                callback.resultCancel( data);
            else if (resultCode == RESULT_OK)
                callback.resultOk( data);
        }
        callbacks.remove( requestCode);
    }


    public String title4progress = "Loading ...";

    public void showInfoDlg( Integer title, String message)  { new ManagedDialog.Info().title( title).message(message).show( this); }
    public void showInfoDlg( Integer title, Integer message) { new ManagedDialog.Info().title( title).message(message).show( this); }

    static Hashtable<String, Integer> dialog_keys = new Hashtable();

    public int loadDialog( String key, ManagedDialog setup) {
        Integer id = dialog_keys.get( key);
        if (id == null) {
            id = dialog_keys.size();
            dialog_keys.put( key, id);
        }
        s.dialog2setup.put( id, setup);
        return id;
    }
    public void showDialog( String key) {
        Integer id = dialog_keys.get( key);
        funk.assertTrue(id != null);
        super.showDialog( id);
    }
    public void showDialog( String key, ManagedDialog setup) {
        int id = loadDialog( key, setup);
        super.showDialog( id);
    }
    public void dismissDialog( String key) {
        debug("dismissDialog key=" + key);
        Integer id = dialog_keys.get( key); //or remove??
        funk.assertTrue(id != null);
        super.dismissDialog( id);
        s.dialog2setup.remove( id);
        removeDialog( id);
    }
}

// vim:ts=4:sw=4:expandtab
