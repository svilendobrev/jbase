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
    View v = context.findViewById( id);
    v.setOnClickListener( l ); return v;
}
static public
View setOnClick( View context, int id, View.OnClickListener l ) {
    View v = context.findViewById( id);
    v.setOnClickListener( l ); return v;
}

static public
void setText( View context, int id, String text) {
    ((TextView) context.findViewById( id)).setText( text);
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
        void ok( String input_text) ;
    }
    static public
    void _dlgEdit( Context a, String title, final ok okker, String text) {
        final EditText input = new EditText( a);
        input.setText( text);
        new Builder( a)
        .setTitle( title)
        .setMessage( "Enter name:")
        .setView( input)
        .setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int which) {
                okker.ok( input.getText().toString() ); } })
        //.setOnCancelListener( new DialogInterface.OnCancelListener() {
        //    @Override public void onCancel( DialogInterface dialog) { // Canceled
        //    } })
        .setNegativeButton( "Cancel", null )
        .create().show();
    }
    static public
    void dlgAdd( Context a, String kind, final ok okker ) {
        _dlgEdit( a, "Add new " + kind, okker, ""); }
    static public
    void dlgRename( Context a, String kind, String oldname, final ok okker ) {
        _dlgEdit( a, "Rename " + kind, okker, oldname); }
    static public
    Builder _dlgOkCancel( Context a, String title, String message, final ok okker ) {
        return new Builder( a)
        .setTitle( title)
        .setMessage( message)
        .setPositiveButton( "Ok", new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int which) {
                okker.ok( null ); }})
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
    void dlgDel( final Context a, final String kind, final CharSequence[] items, final choice okker ) {
        new Builder( a)
        .setTitle( "Delete "+kind)
        .setItems( items,
            new DialogInterface.OnClickListener() {
                @Override public void onClick( DialogInterface dialog, int which) {
                    final Object item2del = okker.choice( which);
                    dlgOkCancel( a, "Delete " + kind + " " + okker.name( item2del) + " ?",
                        new ok() { @Override public void ok( String ignore) {
                            okker.ok( item2del);
                        }});
            }})
        //.setOnCancelListener( new DialogInterface.OnCancelListener() { @Override public void onCancel( DialogInterface dialog) {
        //        // Canceled
        //    }})
        .setNegativeButton( "Cancel", null )
        .create().show();
    }
} //dlg


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


} // Common
// vim:ts=4:sw=4:expandtab
