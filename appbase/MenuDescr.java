package com.svilendobrev.appbase;

import com.svilendobrev.jbase.funk;
//import com.svilendobrev.appbase.ActivityBase;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.SubMenu;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;

import java.util.HashMap;

public
class MenuDescr {

public static
class D {
    public Class<? extends Activity> activityClass;  // the child activity to start

    public MenuHandler menuHandler;  //for executing simple routine (no child activity involved)
    public MenuUpdater updater;
    public int requestCode = -1;

    /** constructors with child activity */
    public D( Class<? extends Activity> activityClass) {
        this.activityClass = activityClass;
    }

    /** constructors with simple handler */
    public D( MenuHandler menuHandler) {
        this.menuHandler = menuHandler;
    }
    public D updater( MenuUpdater updater) {
        this.updater = updater;
        return this;
    }
    public D( MenuHandler menuHandler, MenuUpdater updater) {
        this( menuHandler);
        this.updater = updater;
    }
    public D requestCode( int rcode) {
        this.requestCode = rcode;
        return this;
    }
    public void update( MenuItemInfo ii) {
        if (updater!=null) updater.update( ii);
    }

//    public void start( Activity context, MenuItem item) { start( context, item, null); }
    public void start( Activity context, MenuItem item, ContextMenu.ContextMenuInfo info) {
        if (menuHandler != null)
            menuHandler.handle( new MenuItemInfo( item, info));
        //if (checkable)
        //    item.setChecked( !item.isChecked());
        if (activityClass != null)
            context.startActivityForResult( new Intent( context, activityClass), requestCode);
    }
} //D

static public
class MenuItemInfo {
    public MenuItem item;
    public ContextMenu.ContextMenuInfo info;    //null for non-ContextMenu i.e. options ; maybe null for ContextMenu too

    public MenuItemInfo( MenuItem item, ContextMenu.ContextMenuInfo info) {
        this.item = item;
        this.info = info;
    }
}

static public
interface MenuUpdater {
    public void update( MenuItemInfo item);
}
public static
interface MenuHandler {
    public void handle( MenuItemInfo item);
}


    public HashMap< Integer, D> descr = new HashMap();
    public int resId;
    public Integer titleId;
    public String title;

    public MenuDescr( int resId) { this.resId = resId; }
    public MenuDescr( int resId, int titleId) {
        this(resId);
        this.titleId = titleId;
    }
    public MenuDescr( int resId, String title) {
        this(resId);
        this.title = title;
    }

    //do override
    public String dynamicTitle( MenuItemInfo mii) { return null; }


    public D put( int id, D d) {
        descr.put( id, d);
        return d;
    }
    public D put( int id, Class<? extends Activity> activityClass) {
        return put( id, new D( activityClass));
    }
    public D put( int id, MenuHandler menuHandler) {
        return put( id, new D( menuHandler));
    }
    public D put( int id, MenuHandler menuHandler, MenuUpdater updater) {
        return put( id, new D( menuHandler, updater));
    }

    public boolean start( Activity context, MenuItem item) { return start( context, item, null); }
    public boolean start( Activity context, MenuItem item, ContextMenu.ContextMenuInfo info) {
        if (item.hasSubMenu())
            return true;
        D d = descr.get( item.getItemId());
        if (d==null) return false;
        d.start( context, item, info);
        return true;
    }

    public void update( ContextMenu menu, ContextMenu.ContextMenuInfo info) {
        MenuItemInfo mii = new MenuItemInfo( null, info);
        if (menu instanceof ContextMenu) {
            ContextMenu m = (ContextMenu) menu;
            String t = dynamicTitle( mii);
            if (t != null)
                m.setHeaderTitle( t);
            else
            if (funk.any(title))
                m.setHeaderTitle( title);
            else
            if (titleId != null)
                m.setHeaderTitle( titleId);
        }
        _update( menu, mii);
    }

    public void update( Menu menu) { _update( menu, null); }

    private void _update( Menu menu, MenuItemInfo mii) {
        for (int id : descr.keySet()) {
            D d = descr.get(id);
            mii.item = menu.findItem( id);
            d.update( mii);
        }
    }
} //MenuDescr

// vim:ts=4:sw=4:expandtab
