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
import java.util.ArrayList;

public
class MenuDescr {

public static
class D {
    public Class<? extends Activity> activityClass;  // the child activity to start

    public MenuHandler menuHandler;  //for executing simple routine (no child activity involved)
    public MenuUpdater updater;

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
    public void update( MenuItem item) {
        if (updater!=null) updater.update( item);
    }

    public void start( ActivityBase context, MenuItem item) {
        if (menuHandler != null)
            menuHandler.handle( item);
        //if (checkable)
        //    item.setChecked( !item.isChecked());
        if (activityClass != null) {
            context.startActivity( new Intent( context, activityClass));
        }
    }
} //D

static public
interface MenuUpdater {
    public void update( MenuItem item);
}
public static
interface MenuHandler {
    public void handle( MenuItem item);
}

public static
interface DynamicTitle{
    public String getTitle( ContextMenu.ContextMenuInfo info);
}



    public HashMap< Integer, D> descr = new HashMap< Integer, D>();
    public int resId;
    public Integer titleId;
    public String title;
    public DynamicTitle dynamicTitle;

    public MenuDescr( int resId) { this.resId = resId; }
    public MenuDescr( int resId, int titleId) {
        this(resId);
        this.titleId = titleId;
    }
    public MenuDescr( int resId, String title) {
        this(resId);
        this.title = title;
    }
    public MenuDescr( int resId, DynamicTitle dynamicTitle) {
        this(resId);
        this.dynamicTitle = dynamicTitle;
    }

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

    public boolean start( ActivityBase context, MenuItem item) {
        if (item.hasSubMenu())
            return true;
        D d = descr.get( item.getItemId());
        if (d==null) return false;
        d.start( context, item);
        return true;
    }

    public void update( ContextMenu menu, ContextMenu.ContextMenuInfo info) {
        if (menu instanceof ContextMenu) {
            ContextMenu m = (ContextMenu) menu;
            if (dynamicTitle != null)
                m.setHeaderTitle( dynamicTitle.getTitle( info));
            else
            if ( funk.any(title))
                m.setHeaderTitle( title);
            else
            if ( titleId != null)
                m.setHeaderTitle( titleId);
        }
        update( menu);
    }

    public void update( Menu menu) {
        for (int id : descr.keySet()) {
            D d = descr.get(id);
            MenuItem item = menu.findItem( id);
            d.update( item);
        }
    }
} //MenuDescr

// vim:ts=4:sw=4:expandtab
