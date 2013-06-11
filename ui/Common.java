package com.svilendobrev.ui;

import com.svilendobrev.jbase.funk;

import android.view.ViewGroup;
import android.view.View;

import android.content.res.Resources;
import android.content.Context;
import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.EditText;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.graphics.drawable.Drawable;
import java.net.URL;
import java.io.InputStream;
import android.util.Log;

import com.svilendobrev.appbase.MenuDescr;
import android.view.MenuItem;
import android.view.Menu;
import android.view.ContextMenu;
import java.util.HashMap;
import java.util.List;

import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.util.TypedValue;

import android.location.LocationManager;
import android.location.Location;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

// Common ui functionalities
public
class Common {

/////// visibility

static public void setVisible( View v, boolean show) {
    if (v!=null) v.setVisibility( show ? View.VISIBLE : View.GONE);
}

static public void setInvisible( View v, boolean hide) {
    if (v!=null) v.setVisibility( hide ? View.INVISIBLE : View.VISIBLE );
}

static public void show(        View v, boolean shown)  { setVisible( v,shown); }
static public void invisible(   View v, boolean unseen) { setInvisible( v,unseen); }


static public
void setVisible( View[] vs, boolean show) {
    for (View v: vs)
        v.setVisibility( show ? View.VISIBLE : View.GONE);
}

static public void setVisible( View v)    { if (v!=null) v.setVisibility( View.VISIBLE); }
static public void setInvisible( View v)  { if (v!=null) v.setVisibility( View.INVISIBLE); }
static public void setHidden( View v)     { if (v!=null) v.setVisibility( View.GONE); }
static public void show( View v)        { setVisible(v); }
static public void hide( View v)        { setHidden(v); }
static public void gone( View v)        { setHidden(v); }
static public void invisible( View v)   { setInvisible(v); }

/////// hierarchies

static public
int getIdentifierInOtherNamespace( Resources res, int id, String namespace ) {
    String idname = res.getResourceEntryName( id);
    String pkg = res.getResourcePackageName( id);
    return res.getIdentifier( idname, namespace, pkg);
}

static public
View[] getSubViews( View[] vs, int id) {
    View[] nv = new View[ funk.len( vs) ];
    for (int i= funk.len( nv); --i>=0; )
        nv[i] = vs[i].findViewById( id );
    return nv;
}

/////// clickable

// use like   Common.setOnClick( this, R.id.butt, new View.OnClickListener() { @Override public void onClick( View v) {
static public
View setOnClick( Activity context, int id, View.OnClickListener l ) {
    funk.assertTrue( id !=0);
    View v = context.findViewById( id);
    v.setOnClickListener( l ); return v;
}
static public
View setOnClick( View context, int id, View.OnClickListener l ) {
    funk.assertTrue( id !=0);
    View v = context.findViewById( id);
    v.setOnClickListener( l ); return v;
}

// use like   new Common.OnClick( this, R.id.butt ) { @Override public void onClick( View v) {
abstract static public
class OnClick implements View.OnClickListener {
//usage: new Common.OnClick( ..) { @Override public void onClick(View v) { .. }};
    public OnClick( View v ) { v.setOnClickListener( this); }
    public OnClick( Activity context, int id) {
        funk.assertTrue( id !=0);
        View v = context.findViewById( id);
        v.setOnClickListener( this);
    }
    public OnClick( View context, int id) {
        funk.assertTrue( id !=0);
        View v = context.findViewById( id);
        v.setOnClickListener( this);
    }
}

abstract static public
class OnLongClick implements View.OnLongClickListener {
//usage: new Common.OnLongClick( ...) { @Override public boolean onLongClick( View v) { .. }};
    public OnLongClick( View v ) { v.setOnLongClickListener( this); }
    public OnLongClick( Activity context, int id) {
        funk.assertTrue( id !=0);
        View v = context.findViewById( id);
        v.setOnLongClickListener( this);
    }
    public OnLongClick( View context, int id) {
        funk.assertTrue( id !=0);
        View v = context.findViewById( id);
        v.setOnLongClickListener( this);
    }
}

/////// textable

static public
void setText( View context, int id, CharSequence text) {
    funk.assertTrue( id !=0);
    View v = context.findViewById( id);
    ((TextView) v).setText( text);
}
static public
void setText( View context, int id, int text) {
    funk.assertTrue( id !=0);
    View v = context.findViewById( id);
    ((TextView) v).setText( text);
}

static public
String getText( EditText ev) {
    funk.assertTrue( ev != null);
    return ev.getText().toString();
}
static public
String getText( View context, int edit_text_id) {
    funk.assertTrue( edit_text_id !=0);
    View v = context.findViewById( edit_text_id);
    return getText( (EditText) v);
}

/////// toasts
static public
void toastShort( Context context, String text) { Toast.makeText( context, text, Toast.LENGTH_SHORT ).show(); }
static public
void toastLong(  Context context, String text) { Toast.makeText( context, text, Toast.LENGTH_LONG  ).show(); }
static public
void toastShort( Context context, int text) { Toast.makeText( context, text, Toast.LENGTH_SHORT ).show(); }
static public
void toastLong(  Context context, int text) { Toast.makeText( context, text, Toast.LENGTH_LONG  ).show(); }

/////// dialoging

static public
class dlg {
/* AlertDialog always will be closed after ok().
To re-show (e.g. because validate), store it and send a message:
    AlertDialog _dlg;
    Handler _showHandler = new Handler() { @Override public void handleMessage( Message msg) {
        _dlg.show();
    }};
    ...
        _dlg = new Common.dlg.OkCancel(..) { ok() {
                ...
                _showHandler.sendEmptyMessage( 1234 );
  /////////////
    or, use ManagedDialog
*/
    //override these
    static public String but_Ok = "Ok";
    static public String but_Cancel = "Cancel";

    static public abstract
    class Edit extends Builder {
        //override these
        abstract public void ok( String input_text, View input) ;
        public          void cancel( View input) {}
        public          void init() {}  //extra init stuff, e.g. input.setSomething


        public Edit( final Context a, String text, Integer input_type) { super(a);
            input = new EditText( a);
            input.setText( text);
            if (input_type!=null) input.setInputType( input_type);
            setView( input);
            setPositiveButton( but_Ok, new DialogInterface.OnClickListener() { @Override public void onClick( DialogInterface dialog, int which) {
                hideKeyboard( a, input);
                ok( input.getText().toString().trim(), input );
                }});
            setNegativeButton( but_Cancel, new DialogInterface.OnClickListener() { @Override public void onClick( DialogInterface dialog, int which) {
                hideKeyboard( a, input);
                cancel( input );
                }});
        //.setOnCancelListener( new DialogInterface.OnCancelListener() { @Override public void onCancel( DialogInterface dialog) { ..
        }
        public Edit( final Context a, String text)          { this( a, text, null); }
        public Edit( final Context a, Integer input_type)   { this( a, null, input_type); }
        public Edit( final Context a)                       { this( a, null, null); }

        public Edit singleLine() { input.setSingleLine(); return this; }
        // input_type= android.text.InputType.TYPE_CLASS_TEXT also makes it single-line

        protected EditText input;
    }
    /*new Edit( .. ) { public void ok( String input_text, View input) {
        }}
        .setTitle( title)
        .setMessage( message)
        .show();
    */

    static public
    class Ok extends Builder implements DialogInterface.OnClickListener {
        public Ok( Context a)               { super(a); _init(); }
        public Ok( Context a, String title) { super(a); _init(); setTitle( title); }
        public Ok( Context a, int title)    { super(a); _init(); setTitle( title); }
        public void ok() {} //override this

        @Override public void onClick( DialogInterface dialog, int which) { ok(); }
        private void _init() { setPositiveButton( but_Ok, this); }
    }
    /*new Ok( .. ) { public void ok() {
        }}
        .setTitle( title)
        .setMessage( message)
        .show();
    */
    static public
    class OkCancel extends Ok {
        public OkCancel( Context a)                 { super(a); _init(); }
        public OkCancel( Context a, String title)   { super(a, title); _init(); }
        public OkCancel( Context a, int title)      { super(a, title); _init(); }
        private void _init() { setNegativeButton( but_Cancel, null); }
    }

    public static abstract
    class Choice extends Builder implements DialogInterface.OnClickListener {
        abstract public Object  choice( int which ) ;
        public          String  ask_title( Object choice ) { return null; }
        public          void    ask_ok(    Object choice ) {}
        //public          void    cancel() {}

        public Choice( Context a, CharSequence[] items )    { this( a); setItems( items, this); }
        public Choice( Context a, List< String> items)      { this( a, toArray( items)); }

        protected Choice( Context a) { super(a);
            _ctx = a;
            setNegativeButton( but_Cancel, null );
            //setNegativeButton( but_Cancel, new DialogInterface.OnClickListener() { @Override public void onClick( DialogInterface dialog, int which) {
            //  cancel(); }});
        }

        protected Context _ctx;
        @Override public void onClick( DialogInterface dialog, int which) {
            final Object item2del = choice( which);
            String askt = ask_title( item2del);
            if (funk.not( askt)) return;
            new OkCancel( _ctx) { public void ok() {
                    ask_ok( item2del);
                }}
                .setTitle( askt )
                .show();
        }
    }
    public static abstract
    class Choice1 extends Choice {
        public Choice1( Context a, CharSequence[] items) { this(a,items,-1,true); }
        public Choice1( Context a, CharSequence[] items, int choosen) { this(a,items,choosen,true); }
        public Choice1( Context a, CharSequence[] items, int choosen, boolean autoclose_at_click ) { super(a);
            //XXX to close just in ask_ok, set autoclose=false and use _dlg.dismiss in ask_ok
            _autoclose = autoclose_at_click;
            setSingleChoiceItems( items, choosen, this);
        }
        public AlertDialog _dlg;
        protected boolean _autoclose = false;
        @Override public AlertDialog show() { return _dlg = super.show(); }
        @Override public void onClick( DialogInterface dialog, int which) {
            if (_autoclose) _dlg.dismiss();
            super.onClick( dialog, which);
        }
    }

        //final CharSequence[] items = new CharSequence[ funk.len( id_anchors) ];
        //int i = 0; for (int id: id_anchors) items[i++] = getString( id);
    static public CharSequence[] toArrayCS( List< CharSequence> items)  { return (CharSequence[]) items.toArray( new CharSequence[0]); }
    static public CharSequence[] toArray( List< String> items)       { return (CharSequence[]) items.toArray( new CharSequence[0]); }
} //dlg

/////// contextMenus: see jbase/appbase/ActivityBase ; generic
/* make a (static??) instance then override activity's onCreateContextMenu/onContextItemSelected e.g. in fragmentor
    public Common.contextMenus contextMenus = new contextMenus();
    @Override public void onCreateContextMenu( ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu( menu, v, menuInfo);
        if (contextMenus !=null)
            contextMenus.onCreateContextMenu( this, menu, v, menuInfo);
    }
    @Override public boolean onContextItemSelected( MenuItem item) {
        return contextMenus.onContextItemSelected( this, item);
    }
    public void setContextMenu( View v, MenuDescr m) {
        contextMenus.setContextMenu( this, v, m);
    }
*/
static public
class contextMenus {
    public contextMenus() {}

    protected HashMap< View, MenuDescr> _contextMenus = new HashMap();

    //fragile? set onCreateContextMenu , unset .. never (dont use onClosed)
    protected View _currentContextView;
    public    ContextMenu.ContextMenuInfo _menuInfo; //fragile? but submenus dont get it otherwise

    //@Override
    public void onCreateContextMenu( Activity activity, ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        _currentContextView = v;
        _menuInfo = menuInfo;
        MenuDescr d = _contextMenus.get( v);
        activity.getMenuInflater().inflate( d.resId, menu);
        Log.d( "contextMenus", "onCreateContextMenu "+menu + " " +menuInfo);
        d.update( menu, menuInfo);
    }
    //@Override
    public boolean onContextItemSelected( Activity activity, MenuItem item) {
        MenuDescr d = _contextMenus.get( _currentContextView);
        // MenuItem.getMenuInfo() == null for submenus..
        d.start( activity, item, _menuInfo);
        return true;
    }
    /* dont use: called before submenu opens
    //@Override
    public void onContextMenuClosed( Menu menu) {
        _currentContextView = null;
        _menuInfo = null;
    }
    */

    public void setContextMenu( Activity activity, View v, MenuDescr m) {
        activity.registerForContextMenu( v);
        _contextMenus.put( v, m);
    }
} //contextMenus

/////// misc
static public
Drawable loadImageFromURL( String url) {
    try {
        return Drawable.createFromStream( (InputStream)(new URL( url).getContent()), "blah");
    } catch ( Exception e) {
        Log.e( "loadImageFromURL", ""+url, e);
        return null;
    }
}

/*hideKeyboard
{ // kill keyboard when enter is pressed
textInput.setOnKeyListener( new OnKeyListener() {
    public boolean onKey(View arg0, int arg1, KeyEvent event) {
        if ( event.getAction() == KeyEvent.ACTION_DOWN && arg1 == KeyEvent.KEYCODE_ENTER) {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow( textInput.getWindowToken(), 0);
            return true;
        } return false;
}});
*/
static public
void hideKeyboard( Context a, View v) {    //edittext or tabhost or whatever
    ((InputMethodManager)a.getSystemService( Activity.INPUT_METHOD_SERVICE))
    .hideSoftInputFromWindow(
        //v.getWindowToken()
        v.getApplicationWindowToken()
        , 0);
}
static public
void showKeyboard( Activity a) {
    ((InputMethodManager)a.getSystemService( Activity.INPUT_METHOD_SERVICE))
    .toggleSoftInput( InputMethodManager.SHOW_FORCED, 0) ;
}

/*
static public
void copyButtonStyle( Button from, Button to) {
    //XXX does not really work. inflate-from-butt.xml
    //to.setText( ..)
    //to.setId( ..)
    to.setBackgroundDrawable( from.getBackground());
    to.setLayoutParams( from.getLayoutParams());
    to.setGravity( from.getGravity());
    to.setPadding( from.getPaddingLeft(), from.getPaddingTop(), from.getPaddingRight(), from.getPaddingBottom());
    to.setTextSize( TypedValue.COMPLEX_UNIT_PX, from.getTextSize());
    Drawable[] dd = from.getCompoundDrawables();
    to.setCompoundDrawables( dd[0], dd[1], dd[2], dd[3]);
    to.setTextColor( from.getTextColors());

    //to.setTextAppearance( this, R.style.buttLabel);
}
*/

//non ui

    static public
    Location getLastKnownLocation_any( Context c) {
        LocationManager lm = (LocationManager)c.getSystemService( Context.LOCATION_SERVICE);
        List< String> providers = lm.getProviders( true);
        /* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
        Location l = null;
        for (int i = providers.size(); --i>=0; ) {
            l = lm.getLastKnownLocation( providers.get(i));
            if (l != null) break;
        }
        return l;
    }

    static public boolean hasNetworkAccess( Context c) {
        ConnectivityManager network = (ConnectivityManager) c.getSystemService( Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = network.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    static public int getIntResource( Context c, int id) {
        return (Integer)c.getResources().getInteger( id);
    }
    static public String getRawTextResource( Context c, int id ) {
        try {
            //InputStream is = getAssets().open("disclaimer.txt");
            InputStream is = c.getResources().openRawResource( id); //dR.raw.disclaimer);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read( buffer);
            is.close();
            return new String( buffer);
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

//from 2.0 onwards: see http://stackoverflow.com/questions/2000102/android-override-back-button-to-act-like-home-button
// @Override public void Activity.onBackPressed() { ... }

} // Common
// vim:ts=4:sw=4:expandtab
