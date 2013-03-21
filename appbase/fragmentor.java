package com.svilendobrev.appbase;

import com.svilendobrev.jbase.funk;

import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;

//import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.svilendobrev.appbase.MenuDescr;
import android.view.Menu;
import android.view.MenuItem;

import com.svilendobrev.ui.Common;
import android.view.ContextMenu;
import android.view.View;

public class fragmentor {
    static public
    FragmentActivity replace( FragmentActivity host, int ui_id, Fragment frag) {
        host.getSupportFragmentManager()
            .beginTransaction()
            .replace( ui_id, frag) //, name)
            .addToBackStack( null)
            .commit();
        return host;
    }
    static public
    FragmentActivity replaceIfNotSame( FragmentActivity host, int ui_id, Fragment frag) {
        if (frag != host.getSupportFragmentManager().findFragmentById( ui_id))
            replace( host, ui_id, frag);
        return host;
    }


static public
class aFragment extends Fragment {
    protected void debug( String s) { Log.d( funk.rsplit_last( getClass().getName(), "[.]"), s); }

    /*
    @Override public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ...
         _loadState()
        //XXX never happens: if (savedInstanceState != null) savedInstanceState.getwhatever.. ;
    */

    @Override public void onResume() {
        //db.open();
        super.onResume();
        debug( "onResume");
    }
    @Override public void onPause() {
        //_saveState();
        //db.close();
        super.onPause();
        debug( "onPause");
    }
    @Override public void onCreate( Bundle savedInstanceState) {
        super.onCreate( savedInstanceState);
        debug("onCreate");
    }
/*
    @Override
    public void onSaveInstanceState( Bundle outState) {
        super.onSaveInstanceState(outState);
        debug( "onSaveInstanceState");
        // never happens ; use onPause/ onResume/onCreateView
    }
    @Override public void onStart() { super.onStart(); debug("onStart"); }
    //@Override public void onRestart() { super.onRestart(); debug("onRestart"); }
    @Override public void onStop() { super.onStop(); debug("onStop"); }
    @Override public void onDestroy() { super.onDestroy(); debug("onDestroy"); }
    @Override public void onDestroyView() { super.onDestroyView(); debug("onDestroyView"); }
    @Override public void onActivityCreated( Bundle b) { super.onActivityCreated( b); debug("onActivityCreated "+b); }
    @Override public void onAttach( Activity a) { super.onAttach( a); debug("onAttach"); }
    @Override public void onDetach() { super.onDetach(); debug("onDetach"); }
*/
} //aFragment

static public
class aFragmentActivity extends FragmentActivity {
    protected void debug( String s) { Log.d( funk.rsplit_last( getClass().getName(), "[.]"), s); }

    //optionsMenu: see jbase/appbase/ActivityBase ; generic
    protected MenuDescr optionsMenu;
    @Override public boolean onCreateOptionsMenu( Menu menu) {
        debug( "onCreateOptionsMenu");
        if (optionsMenu == null) return false;
        getMenuInflater().inflate( optionsMenu.resId, menu);
        return true;
    }
    @Override public boolean onPrepareOptionsMenu( Menu menu) {
        debug( "onPrepareOptionsMenu");
        optionsMenu.update( menu);
        return super.onPrepareOptionsMenu( menu);
    }
    @Override public boolean onOptionsItemSelected( MenuItem item) {
        debug( "onOptionsItemSelected");
        optionsMenu.start( this, item);
        return true;
    }

////////////////
/*  needed
    @Override public void onCreate( Bundle savedInstanceState) {
        super.onCreate( savedInstanceState);
        setupPrefs();
        contextMenus = new Common.contextMenus();
        setupMenu();
    }


//  MenuDescr.MenuHandler preferencesHandler = new MenuDescr.MenuHandler() { public void handle( MenuItem item) {
//      //startChild( Preferences.class, AppActivity.preferencesCallback);
//      debug( "preferencesHandler");
//      //startActivityForResult( new Intent( LifeOrganizerActivity.this, Preferences.class), 123); //childKey
//      startActivity( new Intent( LifeOrganizerActivity.this, Preferences.class));
//  }};

    protected void setupMenu() {
        debug( "setupMenu");
        optionsMenu = new MenuDescr( R.menu.main_options);
        optionsMenu.put( R.id.menu_preferences  , Preferences.class );//preferencesHandler);
        //optionsMenu.put( R.id.menu_kill, dieHandler);
        //optionsMenu.put( R.id.menu_information  , informationHandler);
    }

    //prefs: see jbase/appbase/AppData
    static
    public class Preferences extends PrefActivity {
        @Override public    Prefs prefs()     { return Common.prefs; }
        @Override protected int resource()    { return R.xml.preferences; }
    }

    protected void setupPrefs() {
        debug( "setupPrefs");
        prefs = new Prefs( getApplicationContext() );//this.context);

        prefs.new Str( R.string.pref_server_url, R.string.pref_server_url_default ) { @Override public void onChange() {
                debug("pref_server_url changed "+value()); //also init below
                onRemoteSyncURLChanged();
            }}
            .common_action( "init");
        prefs.new Str( R.string.pref_usr_psw,    R.string.pref_usr_psw_default    ) { @Override public void onChange() {
                debug("pref_usr_psw changed "+value()); //also init below
                onRemoteSyncURLChanged();
            }}
            .common_action( "init");
        prefs.new Bool( R.string.pref_use_default_location, false );
        prefs.new Str( R.string.pref_default_location, R.string.pref_default_location_default );

        prefs.load();
    }
    void onRemoteSyncURLChanged() {}
*/

    //////////
    //contextMenus: see jbase/appbase/ActivityBase ; generic
    //static
    public Common.contextMenus contextMenus ; //= new contextMenus(); XXX initialize this to get going
    @Override public void onCreateContextMenu( ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu( menu, v, menuInfo);
        debug( "onCreateOptionsMenu: "+ contextMenus);
        if (contextMenus !=null)
            contextMenus.onCreateContextMenu( this, menu, v, menuInfo);
    }
    @Override public boolean onContextItemSelected( MenuItem item) {
        return contextMenus.onContextItemSelected( this, item);
    }
    public void setContextMenu( View v, MenuDescr m) {
        contextMenus.setContextMenu( this, v, m);
    }

} //aFragmentActivity


} //Common
// vim:ts=4:sw=4:expandtab
