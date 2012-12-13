package com.svilendobrev.ui;

import com.svilendobrev.jbase.funk;

import android.view.ViewGroup;
import android.view.View;

import android.content.res.Resources;

// Common functionalities
public
class Common {

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

} // Common
// vim:ts=4:sw=4:expandtab
