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
import android.app.AlertDialog.Builder;

import android.graphics.drawable.Drawable;
import java.net.URL;
import java.io.InputStream;
import android.util.Log;

import com.svilendobrev.appbase.MenuDescr;
import android.view.MenuItem;
import android.view.ContextMenu;
import java.util.HashMap;

import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.util.TypedValue;

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

/////// textable/clickable

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

static public
void setText( View context, int id, CharSequence text) {
    funk.assertTrue( id !=0);
    View v = context.findViewById( id);
    ((TextView) v).setText( text);
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
    public
    interface ok {
        void ok() ;
    }
    public
    interface ok2 {
        void ok( String input_text, View input) ;
    }
    static public
    void _dlgEdit( final Context a, String title, String message, final ok2 okker, String text, Integer input_type) {
        final EditText input = new EditText( a);
        input.setText( text);
        if (input_type!=null) input.setInputType( input_type);
        Builder b = new Builder( a)
        .setTitle( title)
        .setMessage( message)
        .setView( input)
        .setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int which) {
                //hideKeyboard( a, input);
                okker.ok( input.getText().toString(), input ); } })
        //.setOnCancelListener( new DialogInterface.OnCancelListener() {
        //    @Override public void onCancel( DialogInterface dialog) { // Canceled
        //    } })
        .setNegativeButton( "Cancel", null )
        ;
        b.create().show();
    }
    //static public
    //void z_dlgEdit( Context a, String title, final ok okker, String text) {
    //    _dlgEdit( a, title, okker, text, "Enter name:") }
    static public
    void dlgAdd( Context a, String kind, final ok2 okker ) {
        _dlgEdit( a, "Add new " + kind, "Enter name:", okker, "", null); }
    static public
    void dlgRename( Context a, String kind, String oldname, final ok2 okker ) {
        _dlgEdit( a, "Rename " + kind, "Enter name:", okker, oldname, null); }
    static public
    Builder _dlgOkCancel( Context a, String title, String message, final ok okker ) {
        return new Builder( a)
        .setTitle( title)
        .setMessage( message)
        .setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int which) {
                okker.ok(); }})
        .setNegativeButton( "Cancel", null);
    }
    static public
    void dlgOkCancel( Context a, String title, final ok okker ) { dlgOkCancel( a, title, null, okker ); }
    static public
    void dlgOkCancel( Context a, String title, String message, final ok okker ) {
        _dlgOkCancel( a, title, message, okker ) .create().show(); }

    public
    interface choice {
        Object choice( int which ) ;
        String name( Object choice ) ;
        void ok(     Object choice ) ;
    }
    static public
    void _dlgChoose( final Context a, final String title, final CharSequence[] items, final choice okker, final boolean ask) {
        new Builder( a)
        .setTitle( title)
        .setItems( items,
            new DialogInterface.OnClickListener() {
                @Override public void onClick( DialogInterface dialog, int which) {
                    final Object item2del = okker.choice( which);
                    if (!ask) return;
                    dlgOkCancel( a, title+ " " + okker.name( item2del) + " ?",
                        new ok() { @Override public void ok() {
                            okker.ok( item2del);
                        }});
            }})
        //.setOnCancelListener( new DialogInterface.OnCancelListener() { @Override public void onCancel( DialogInterface dialog) {
        //        // Canceled
        //    }})
        .setNegativeButton( "Cancel", null )
        .create().show();
    }
    static public
    void dlgDel( final Context a, final String kind, final CharSequence[] items, final choice okker ) {
        String title = "Delete "+kind;
        _dlgChoose( a, title, items, okker, true);
    }
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
    protected View _currentContextView; //fragile?
    //@Override
    public void onCreateContextMenu( Activity activity, ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        _currentContextView = v;
        MenuDescr d = _contextMenus.get( v);
        //MenuInflater inflater =
        activity.getMenuInflater().inflate( d.resId, menu);
        d.update( menu, menuInfo);
    }
    //@Override
    public boolean onContextItemSelected( Activity activity, MenuItem item) {
        MenuDescr d = _contextMenus.get( _currentContextView);
        d.start( activity, item);
        return true;
    }

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

} // Common
// vim:ts=4:sw=4:expandtab
