package com.svilendobrev.appbase;

import com.svilendobrev.jbase.Log;
import com.svilendobrev.jbase.funk;
import com.svilendobrev.jbase.Model;
import com.svilendobrev.jbase.Error;
import com.svilendobrev.pref.Prefs;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class AppData {

    public interface DataLoader {
        public ArrayList<Model> loadData();
    }

    abstract public static class DataCache {
        boolean _dirty = true;
        protected ArrayList<Model> data;

        //public DataCache() {}
        public ArrayList<Model> getData( DataLoader loader) {
            if (_dirty) {
                data = loader!=null ? loader.loadData() : loadData();
                _clearmap();
                for (Model m : data)
                    _add2map( m);
                _dirty = false;
            }
            return data;
        }
        public ArrayList<Model> getData() { return getData(null); }

        public void update_now() {
            invalidate();
            getData();
        }
        public void invalidate() {
            _dirty = true;
        }

        public boolean dirty() { return _dirty; }

        public void add( Model m) {
            if (m == null) return;
            data.add( m);
            _add2map( m);
        }
        public void remove( Model m) {
            data.remove( data.indexOf( m));
            _del2map(m);
        }

        abstract protected void _clearmap();
        abstract protected void _add2map( Model m);
        abstract protected void _del2map( Model m);
        abstract public Model getById( long id);
        abstract public Model getById( String key);

        protected ArrayList<Model> loadData() { return null; } //either redefine this or use a DataLoader
    }

    public static class DataCacheInt extends DataCache {
        private HashMap< Long, Model> map = new HashMap();

        @Override protected void _clearmap() { map.clear(); }
        @Override protected void _add2map( Model m) { map.put( m.getId(), m); }
        @Override protected void _del2map( Model m) { map.remove( m.getId()); }
        @Override public Model getById( long id) { return map.get( id); }
        @Override public Model getById( String key) { funk.fail("only integer keys supported"); return null; }
    }
    public static class DataCacheStr extends DataCache {
        private HashMap< String, Model> map = new HashMap< String, Model>();

        @Override protected void _clearmap() { map.clear(); }
        @Override protected void _add2map( Model m) { map.put( m.getKey(), m); }
        @Override protected void _del2map( Model m) { map.remove( m.getKey()); }
        @Override public Model getById( String key) { return map.get( key); }
        @Override public Model getById( long id) { funk.fail("only string keys supported"); return null; }
    }


    public static boolean inited = false;

    private int refcounter = 0;

    public Context context;
    public Prefs prefs;

    public AppData( Context context) {
        this.context = context.getApplicationContext();
        prefs = new Prefs( this.context);
    }

    public void open() {
        if (refcounter==0) _open();
        refcounter++;
        Log.d("===========appdata open: " + refcounter);
    }
    public void close() {
        Log.d("===========appdata close: " + refcounter);
        refcounter--;
        if (refcounter==0) _close();
    }

    public void factoryReset() {
        refcounter = 0;
    }

    //public abstract Error init();
    protected void _open() {}
    protected void _close() {}
}

// vim:ts=4:sw=4:expandtab
