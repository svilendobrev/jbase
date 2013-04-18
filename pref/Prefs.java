package com.svilendobrev.pref;

import com.svilendobrev.jbase.funk;
import com.svilendobrev.jbase.Log;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 Prefs is a middleman between App and Editors( Preferences) and default-values stored in values.xml, balancing on a thin line:
 App:
    int  may be edited via SeekBar, ListInt, TextInt-or-whatever
    str  may be edited via Text*, List
    bool may be edited via CheckBox
 Editors:
    List  saves String and uses Strings as entries+values
    ListInt saves Integer and uses Strings as entries+values
    Text  saves String
    CheckBox saves Boolean
    TextInt??? saves Integer
    SeekBar saves Integer
 values.xml: (all is xml text actualy)
    int  can be stored as integer or string
    str  can be stored string (or integer??)
    bool can be stored as bool
    choices4lists can be string-array, integer-array
*/

public
class Prefs implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context context;
    private SharedPreferences prefs;
    private HashMap< Integer, Pref> _defs = new HashMap();
    private HashMap< String,  Pref> _defs4key = new HashMap();

    public
    ArrayList< Pref> changes = new ArrayList();

    public
    Prefs( Context c ) {
        context = c;
        prefs = PreferenceManager.getDefaultSharedPreferences( context);
    }

    public Object get( int id)          { return _defs.get( id).value(); }
    public Object get( String key)      { return _defs4key.get( key).value(); }
    public Pref getPref( int id)        { return _defs.get( id); }
    public Pref getPref( String key)    { return _defs4key.get( key); }

    public void set( int id, Object v) {
        v = _defs.get( id ).set( v);
        String key = context.getString( id);
        _setDirect( key, v);
    }
    private void _setDirect( String key, Object v) {
        SharedPreferences.Editor e = prefs.edit();
        if (v == null) e.remove( key);
        else
        if (v instanceof Boolean) e.putBoolean( key, (Boolean) v);
        else
        if (v instanceof Integer) e.putInt( key, (Integer) v);
        else
        if (v instanceof String) e.putString( key, (String) v);
        else
            funk.fail( "setPreference: unknown type " + v.getClass() );
        Log.d("Set preference:" + key + " value: " + v);
        e.commit();
    }

    public void del( int id) {
        SharedPreferences.Editor e = prefs.edit();
        e.remove( context.getString( id ));
        e.commit();
    }

    public void delAll() {
        SharedPreferences.Editor e = prefs.edit();
        e.clear();
        e.commit();
    }

    //@Override ...implements SharedPreferences.OnSharedPreferenceChangeListener {
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key) {
        Pref p = _defs4key.get( key);
        if (p == null) {
            Log.d("set unknown preference:" + key );
            return;
        }
        Integer i = p.id;
        Object old = p.value();
        Object val = sharedPreferences.getAll().get(key);
        val = p.set( val);
        if (!val.equals( old))
            if (!changes.contains( p)) {
                changes.add( p);
                p.onChange();
            }
    }
    public void changed( int id) {
        Pref p = _defs.get( id);
        funk.assertTrue( p != null);
        changes.add( p);
    }

    public String  getStr(  int idkey)  { return (String)  get( idkey); }
    public Integer getInt(  int idkey)  { return (Integer) get( idkey); }
    public Boolean getBool( int idkey)  { return (Boolean) get( idkey); }
    public String  getStr(  String key) { return (String)  get( key); }
    public Integer getInt(  String key) { return (Integer) get( key); }
    public Boolean getBool( String key) { return (Boolean) get( key); }

    public void clear_defs_caches() {
        changes.clear();
        _defs.clear();
        _defs4key.clear();
    }

    public void load() {
        //use after definitions
        for (Pref p: _defs.values())
            _defs4key.put( context.getString( p.id), p);
        //XXX all preferences must have defaults - i.e. via _defs4key
        Map<String, ?> saved = prefs.getAll();
        for (String key : saved.keySet()) {
            Pref p = _defs4key.get( key);
            if (p == null)
                Log.d("load unknown preference:" + key );
            else
                p.set( saved.get(key) );
        }
    }

    public String  getStrDirect(  String key, String defval)     { return prefs.getString( key, defval); }
    public Integer getIntDirect(  String key, int    defval)     { return prefs.getInt( key, defval); }
    public Boolean getBoolDirect( String key, boolean defval)    { return prefs.getBoolean( key, defval); }
    public void setDirect( String key, Object v) {
        funk.assertTrue( !_defs.containsKey( key) );
        _setDirect( key, v);
    }

//////////

public
class Pref {
    public Pref common_action( String act) { _common_actions.add( act); return this; }
    public boolean needs_common_action( String act)     { return _common_actions.contains( act); }
    //overrload these
    public void onChange() {}
    public Object value()  { return _value; }               //convert or whatever
    public Object default_value()  { return _default_value; }  //convert or whatever
    public Object set( Object v) { _value =v; return v; }  //convert or whatever

    protected
    Pref( int id) {
        this.id = id;
        _defs.put( id, this);
    }

    protected
    Pref( int id, Object v) {
        this( id);
        _set_value_and_default( v);
    }
    protected void _set_value_and_default( Object v) { _default_value = set(v); }


    //////
    public    int id;
    protected Object _value, _default_value;

    protected
    ArrayList< String > _common_actions = new ArrayList();

    public
    boolean equals( Object o) { return o instanceof Pref && ((Pref)o).id == id; }
}
public
class Null extends Pref {
    public Null( int id) { super( id); }
}
public
class Str extends Pref {
    public Str( int id, String value) { super( id, value); }
    public Str( int id, int idvalue)  { super( id, context.getResources().getString( idvalue)); }
}
public
class Int extends Pref {
    public Int( int id, Integer value, boolean ignored) { super( id, value); }
    public Int( int id, int idvalue) {
        super( id);
        _set_value_and_default( id2value( idvalue));
    }

    protected
    Object id2value( int idvalue) {
        Resources r = context.getResources();
        String type = r.getResourceTypeName( idvalue);
        Object value;
        if ("string".equals( type)) value = r.getString( idvalue);
        else value = r.getInteger( idvalue);
        return value;
    }

    protected
    Integer asint( Object v) {
        if (v instanceof String) v = Integer.parseInt( (String) v);
        //funk.assertTrue( v instanceof Integer);
        return (Integer)v;
    }

    public Object set( Object v) { return super.set( asint(v) ); }

    public
    Integer min_value, max_value, step;
    public Int  min_value_v( int v )    { min_value = v; return this; }
    public Int  max_value_v( int v )    { max_value = v; return this; }
    public Int  step_v( int v )         { step = v; return this; }
    public Int  min_value_id( int id )  { return min_value_v( asint( id2value( id)) );  }
    public Int  max_value_id( int id )  { return max_value_v( asint( id2value( id)) );  }
    public Int  step_id( int id )       { return step_v( asint( id2value( id)) );  }
}
public
class Bool extends Pref {
    public Bool( int id, Boolean value) { super( id, value); }
    public Bool( int id, int idvalue)   { super( id, context.getResources().getBoolean( idvalue)); }
}

} //Prefs
// vim:ts=4:sw=4:expandtab
