package com.svilendobrev.storage;

import com.svilendobrev.jbase.funk;
import com.svilendobrev.jbase.Log;
import com.svilendobrev.jbase.Model;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.database.sqlite.SQLiteCursor;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

public
class sqlite {
    protected SQLiteDatabase db;
    private DBhelper dbopener;

/*
    public
    static class Setting extends Models.KeyValue {
        static class db extends genSQLite.KeyValue {
            public Class modelClass()   { return Setting.class; }
            public String tablename()   { return "T4Setting"; }
        }
    }
*/

    static protected
    HashMap< Class, Base> modelklas2db = new HashMap();
    static {
        //genSQLite.setup();  //register generateds
//        new Setting.db();
    }
    //all handmade Base/db-model-clones like Setting above have to be registered (just create one)
    //BEFORE db is created; e.g. in some static block/method
    //beware that static stuff is called ONLY when owner class is used which may be too late
    //and the order inside a class is AS written

    public sqlite( DBhelper dbhelper) {
        //all handmade db-models have to be registered so far
        //Log.d( "ssssssssssssssssqlite open: " + modelklas2db.keySet() );
        dbopener = dbhelper;
        open();
        //shema( db);
    }
    public void open()  {
        if (db==null) db = dbopener.getWritableDatabase();
    }
    public void close() { db.close(); db = null; }

    private static ContentValues cv = new ContentValues();

    public int insert( Model x) {
        Base b = modelklas2db.get( x.getClass() );
        cv.clear();
        return db.insert( b.tablename(), null, b.save( x, cv))>0 ? 1 : 0;
    }
    public int update( Model x, Model old4key) {
        Base b = modelklas2db.get( x.getClass() );
        if (old4key==null) old4key = x;
        cv.clear();
        return db.update( b.tablename(), b.save(x, cv), b.whereBy(old4key), null);
    }
    public int update( Model x) { return update( x,x); }

    public void deleteAll( Class x) {
        Log.d( "deleteAll " + x );
        Base b = modelklas2db.get( x);
        db.delete( b.tablename(), null, null);
    }
    public int delete( Model x) {
        Base b = modelklas2db.get( x.getClass() );
        return db.delete( b.tablename(), b.whereBy(x), null);  //TODO whereargs instead of embedding in whereBy?
    }
    public int delete( Class klas, String where) {
        Base b = modelklas2db.get( klas);
        return db.delete( b.tablename(), where, null);
    }

    public int put( Model x, Model old4key) {
        //delete( old4key or x); insert( x);
        //or
        int r = update(x,old4key);
        return r>0 ? r : insert(x);
    }
    public int put( Model x) { return put( x,x); }

    //query( String table, String[] columns,
        //String selection, String[] selectionArgs,
        //String groupBy, String having, String orderBy,
        //String limit)

    static public
    Cursor query( SQLiteDatabase db, String tablename, String[] columns, String where, String orderBy, String limit, String groupBy) {
        if (0>0)
        Log.d( "QQQQQQQQQQuery: "+ tablename
            + (funk.any(columns)  ? " "+funk.join( columns, ",") : "")
            + (funk.any(where)    ? " WHERE "+where :"")
            + (funk.any(groupBy)  ? " GROUP BY "+groupBy :"")
            + (funk.any(orderBy)  ? " ORDER BY "+orderBy :"")
            + (funk.any(limit)    ? " LIMIT "+limit :"")
        );
        Cursor sc = db.query( tablename, columns,
                        where, null,
                        groupBy, null,
                        orderBy, limit);
        //((SQLiteCursor)sc).setLoadStyle( 50, 100);    //not available yet, as well as db.interrupt()
        return sc;
    }
    static public
    Cursor query( SQLiteDatabase db, String tablename, String[] columns, String where, String orderBy, String limit) {
        return query( db, tablename, columns, where, orderBy, limit, null); }
    static public
    Cursor query( SQLiteDatabase db, String tablename, String where, String orderBy, String limit) {
        return query( db, tablename, null, where, orderBy, limit); }
    static public
    int query1int( SQLiteDatabase db, String tablename, String column, String where, int valueOnNull ) {
        Cursor c = query( db, tablename, new String[] { column }, where, null, null);
        int r = c.moveToFirst() ? c.getInt( 0) : valueOnNull;
        c.close();
        return r;
    }
    static public
    int count( SQLiteDatabase db, String tablename, String where ) {
        return query1int( db, tablename, "count(1)", where, 0 );
    }

    static public
    ArrayList<String> query1column( SQLiteDatabase db, String tablename, String column, String where, String orderBy ) {
        ArrayList<String> r = new ArrayList();
        Cursor c = query( db, tablename, new String[] { column }, where, orderBy, null);
        if (c.moveToFirst()) do {
                r.add( c.getString(0));
            } while (c.moveToNext());
        c.close();
        return r;
    }

    public Cursor query( String tablename, String[] columns, String where, String orderBy, String limit, String groupBy) {
        return query( db, tablename, columns, where, orderBy, limit, groupBy); }
    public Cursor query( String tablename, String[] columns, String where, String orderBy, String limit) {
        return query( db, tablename, columns, where, orderBy, limit); }
    public Cursor query( String tablename, String where, String orderBy, String limit) {
        return query( db, tablename, where, orderBy, limit); }
    public Cursor query( String tablename, String where, String orderBy) {
        return query( db, tablename, where, orderBy, null); }

    public Cursor query( Class klas, String where, String orderBy, String limit) {
        Base b = modelklas2db.get( klas );
        return query( b.tablename(), where, orderBy, limit);
    }

    public int count( String tablename, String where ) { return count( db, tablename,where); }
    public int count( String tablename) { return count( tablename, null); }
    public
    ArrayList<String> query1column( String tablename, String column, String where, String orderBy ) { return query1column( db, tablename, column, where, orderBy); }
    public int query1int( String tablename, String column, String where, int valueOnNull ) { return query1int( db, tablename, column, where, valueOnNull ); }

    public int count( Class klas, String where ) { return count( db, modelklas2db.get( klas ).tablename(), where); }
    public int count( Class klas) { return count( klas, null); }

    public Model load( Model x, Cursor c) {
        Base b = modelklas2db.get( x.getClass() );
        return b.load( x, c);
    }
    public void load_from_cursor( Class klas, Cursor c, Collection< Model> result) {
        Base b = modelklas2db.get( klas );
        b.load_from_cursor( c, result);
    }

    public void getAll( Class klas, Collection<Model> result, String where, String orderBy, String limit) {
        Base b = modelklas2db.get( klas );
        b.load_from_cursor( query( b.tablename(), where, orderBy, limit), result);
    }
    public void getAll( Class klas, Collection<Model> result, String where, String orderBy) { getAll( klas, result, where, orderBy, null); }
    public void getAll( Class klas, Collection<Model> result) { getAll( klas, result, null, null, null); }

    public Model.Many getAll( Class klas, String where, String orderBy, String limit) {
        Model.Many result = Model.newCollection();
        getAll( klas, result, where, orderBy, limit);
        return result;
    }
    public Model.Many getAll( Class klas, String where, String orderBy) { return getAll( klas, where, orderBy, null); }
    public Model.Many getAll( Class klas) { return getAll( klas, null, null); }

/*
    public void getAll( Class klas, Map<Integer,Model> result, String where, String orderBy) {
        Base b = modelklas2db.get( klas );
        b.load_from_cursor( query( b.tablename(), where, orderBy), result);
    }
*/
/*
    public Model get1st( Class klas, String where) {
        Model.Many result = getAll( klas, where, null, "1");
        return funk.not(result) ? null : result.get(0);
    }
    public Model get1st( Model some4key) {
        Base b = modelklas2db.get( some4key.getClass() );
        return get1st( some4key.getClass(), b.whereBy( some4key) );
    }
*/
    public Model get1st( Model some4key, boolean overwrite) {
        Base b = modelklas2db.get( some4key.getClass() );
        Cursor c = query( b.tablename(), b.whereBy( some4key), null, "1");
        if (!overwrite) some4key = null;
        if (c.moveToFirst())
            some4key = b.load( some4key, c);
        else
            some4key = null;
        c.close();
        return some4key;
    }
    public Model get1stOverwrite( Model some4key) { return get1st( some4key, true); }
    public Model get1st(          Model some4key) { return get1st( some4key, false); }

    public void putAll( Collection<Model> data) {
        for (Model x: data) put(x);
    }

    static
    public String whereIn( Class klas, Collection<Model> cc) {
        Base b = modelklas2db.get( klas );
        return b.whereIn( cc);
    }

    public void tx_begin() { db.beginTransaction(); }
    public void tx_end()   { db.endTransaction(); }
    public void tx_ok()    { db.setTransactionSuccessful(); }

    ////////////// shema-management

    //"SELECT name FROM  (SELECT * FROM sqlite_master UNION ALL   SELECT * FROM sqlite_temp_master) WHERE type='table' ORDER BY name "
    //"SELECT name FROM sqlite_master WHERE type='table' and name like 'T4%' ORDER BY name;"

    static public
    void shema_tables( SQLiteDatabase db, ArrayList< String> r ) {
        Cursor c = query( db, "sqlite_master", "type='table' and name like 'T4%'", "name", null );
        if (c.moveToFirst()) do {
            r.add( c.getString( c.getColumnIndex( "name" ) ));
        } while (c.moveToNext());
        c.close();
    }
    static public
    void shema_columns( SQLiteDatabase db, String tablename, HashMap< String,String> r) {
        Cursor c = db.rawQuery( "PRAGMA table_info( '"+tablename +"')", null);
        if (c.moveToFirst()) do {
            r.put( c.getString( 1).toLowerCase(), c.getString(2).toUpperCase() );
        } while (c.moveToNext());
        c.close();
    }
    static public
    void shema( SQLiteDatabase db) {
        ArrayList< String> tablenames = new ArrayList();
        shema_tables( db, tablenames);
        for (String tname : tablenames) {
            HashMap< String,String> colnametypes = new HashMap();
            shema_columns( db, tname, colnametypes);
            Log.d( "SHEEEma: "+ tname + ": " + colnametypes);
        }
    }
    //alter table %tablename add column %columndef


/////////////////

static public
abstract class Base {
    //create an instance of this to autoregister, BEFORE db is created
    public Base() {
        Class c = modelClass();
        sqlite.modelklas2db.put( c, this);
        //tablename = "T4"+c.getName().split( '.')[-1].split('$')[-1]
        //Log.d( "BBBBBBBBase: " + c );
    }
    //String tablename;
    public abstract Class modelClass();
    public abstract Model load( Model target, Cursor c);   //ret target
    abstract public ContentValues save( Model source, ContentValues result); //ret result
    abstract public String whereBy( Model target);
    abstract public String whereIn( Collection<Model> targets);
    abstract public String keyName();
    abstract public String tablename();
    abstract public String creation();
    abstract public String[] colnames();
    abstract public String[] coltypes();
    //abstract Model  factory();    //to optimize

    public
    Model load( Cursor c) { return load( null, c); }
    public
    Model factory() { try {
        Model r = (Model)modelClass().newInstance();
        return r;
        } catch (Exception e) { return null; }
    }

    public
    void load_from_cursor( Cursor c, Collection< Model> result) {
        if (c.moveToFirst())
            do {
                result.add( load(c));
            } while (c.moveToNext());
        c.close();
    }
/*
    public
    void load_from_cursor( Cursor c, Map< Integer,Model> result) {
        if (c.moveToFirst())
            do {
                Model x = load(c);
                result.put( x.getId(), x);
            } while (c.moveToNext());
        c.close();
    }
*/
}

static public String escape1( String text) {
    String[] special = { "%", "_" };
    text = text.replace( "'", "''");
    String esc = "\\";
    text = text.replace( esc, esc+esc);
    for (String s : special) {
        if (esc.equals( s)) continue;
        text = text.replace( s, esc+s);
    }
    return text;
}
static public String escape2( String s) { return s.replace( "\"", "\\\""); }
static public String str1( String s)    { return "'" + s.replace( "'", "\\'") + "'"; }
static public String str2( String s)    { return "\"" + s.replace( "\"", "\\\"") + "\""; }

static public int      sql2int(    int i, Cursor c) { return i < 0 ? 0 : c.getInt( i); }
static public long     sql2long(   int i, Cursor c) { return i < 0 ? 0 : c.getLong( i); }
static public float    sql2float(  int i, Cursor c) { return i < 0 ? 0 : c.getFloat( i); }
static public String   sql2string( int i, Cursor c) { return i < 0 ? "" : funk.defaults( c.getString( i), ""); }
static public boolean  sql2bool(   int i, Cursor c) { return i < 0 ? false : sql2bool( c.getInt( i)); }

static public boolean  sql2bool( int x) { return x!=0; }
static public int      bool2sql( boolean x) { return x ? 1: 0; }

static public
Date sql2TimeStamp( int timestamp ) { //String timestamp) {
    try {
        return new Date( 1000*(long) timestamp);
        //return new Date( 1000*(long)Float.parseFloat( timestamp));
        //return BaseSAXHandler.parseTimeStamp( timestamp);
    } catch (Exception e) { return null; }
}
static public
int TimeStamp2sqlI( Date timestamp) {
    return (int)(timestamp.getTime()/1000);
}
//String TimeStamp2sqlS( Date timestamp) {
    //return BaseSAXHandler.datetimeFormat.format( timestamp);

static int[] _sql2weekday = {
    Calendar.MONDAY,
    Calendar.TUESDAY,
    Calendar.WEDNESDAY,
    Calendar.THURSDAY,
    Calendar.FRIDAY,
    Calendar.SATURDAY,
    Calendar.SUNDAY,
};
static HashMap< Integer, Integer> _weekday2sql = new HashMap();
static {
    for (int i=_sql2weekday.length; --i>=0; )
        _weekday2sql.put( _sql2weekday[i], i );
}
static public
int sql2weekday( int sqlwd ) { return _sql2weekday[ sqlwd ]; }
static public
int weekday2sql( int weekday ) { return _weekday2sql.get( weekday); }

static public
byte[] sql2blob( int i, Cursor c) { return c.getBlob( i); }
static public
Bitmap sql2Bitmap( int i, Cursor c) {
    byte[] data = c.getBlob( i);
    return BitmapFactory.decodeByteArray( data, 0, data.length);
}

/*
static
ArrayList< String> export_table( SQLiteDatabase db, String tablename, ArrayList< String> rows) {
    if (rows==null) rows = new ArrayList();
    //Cursor c = db.rawQuery( ".dump "+tablename, null);
    Cursor c = query( db, tablename, null, null, null);
    DatabaseUtils.dumpCursor( c);
    if (c.moveToFirst()) do {
        rows.add( c.getString( 0) );
    } while (c.moveToNext());
    c.close();
    return rows;
}
static
boolean import_stuff( SQLiteDatabase db, BufferedReader in) {
    try {
        for (String r; (r=in.readLine())!=null; )
            db.execSQL( r);
    } catch (Exception e) {
        Log.d( e);
        return false;
    }
    return true;
}

*/


static public
class DBhelper extends SQLiteOpenHelper {
    public
    DBhelper( Context context, String name, int version ) { super( context, name, null, version); }

    static public
    void create_index( SQLiteDatabase db, String table, String indexes) {
        final String crtix = "create index if not exists ";
        int i=0;
        for (String ix: funk.split( indexes)) {
            //Log.d( "INDeeeex: "+table +"."+ ix);
            db.execSQL( crtix + table+"_"+ (i++) +" on " +table+ " (" +ix+ ")");
        }
    }

    //XXX TODO FIXME this whole version stuff is weak. if it happens that db exists but
    // has no tables (e.g. by crash in onCreate), onCreate is never ever called
    // XXX it should be "create if doesnot exist" for each table, even if same version
    //see SQLiteOpenHelper src e.g. http://www.netmite.com/android/mydroid/1.5/frameworks/base/core/java/android/database/sqlite/SQLiteOpenHelper.java
    //it uses context.openOrCreateDatabase() (giving a per-package db)
    //there's also onOpen( db) which may be used for partial create if something is missing.
    @Override
    public void onOpen( SQLiteDatabase db) {
        for (Base b: modelklas2db.values()) {
            db.execSQL( "create table if not exists " + b.tablename() + " ( "+ b.creation() + " )" );
            if (true) {
                String tname = b.tablename();
                HashMap< String,String> colnametypes = new HashMap();
                shema_columns( db, tname, colnametypes);
                //Log.d( "SHEEEma: "+ tname + ": " + colnametypes);
                for (int i= funk.len( b.coltypes()); --i>=0; ) {
                    String cname= b.colnames()[i];
                    String ctype= b.coltypes()[i].toUpperCase();
                    String type = colnametypes.get( cname.toLowerCase());
                    if (type==null) {
                        Log.d( "SHEEEma: " +tname+ "." + cname + " adding as " +ctype);
                        db.execSQL( "alter table " + tname + " add column "+ cname + " " + ctype );
                    }
                    else if (!type.equals( ctype) ) {
                        Log.d( "SHEEEma: " +tname+ "." + cname + ": wrong type " +type+ ", expected "+ ctype);
                        funk.assertTrue( false);
                    }
                }
            }
        }
/*
        create_index( db, "t4program",  "key chid programType timeStart start_time start_date title timeEnd"
            //+ " category,start_time,chid"
            //+ " category,start_time"
            );

        db.execSQL( "create view if not exists "+
                    "prord AS SELECT "+ funk.join( columns4query4program_ordered, ", ")
                    + " FROM " + table4query4program_ordered
                    );
        */
    }

    @Override
    public void onCreate( SQLiteDatabase db) { onOpen( db); }
    @Override
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion) {
        //http://developer.android.com/reference/android/database/sqlite/package-summary.html
        //http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html
        //for (Base b: modelklas2db.values())
        //    db.execSQL( "DROP TABLE IF EXISTS " + b.tablename() );
        onCreate( db);
    }

} //DBhelper

} //sqlite

// vim:ts=4:sw=4:expandtab
