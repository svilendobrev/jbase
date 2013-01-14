package com.svilendobrev.jbase;

////////////// DateTime timezone etc

import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.AttributedCharacterIterator;

import java.util.ArrayList;

import java.text.DateFormat;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

import android.content.Context;

public
class datetime {

public static TimeZone timezone;
public static void updateTimeZone() {
    timezone = TimeZone.getDefault(); //getTimeZone( "GMT");
}
static {
    updateTimeZone();
}

static long timezoneOffset_db2local_ms() {
    return timezone.getRawOffset() - TimeZone.getDefault().getRawOffset();
}


public static Calendar now() {
    return new GregorianCalendar( timezone );
        //XXX1 timezone);   //timezone==default local
}


//TODO - all these to be removed

static void _fixDateFormat( DateFormat df) {
    df.setTimeZone( timezone);
}
public static DateFormat fixDateFormat( DateFormat df) {
    _fixDateFormat( df);
    _allDateFormats.add( df);
    return df;
}
static ArrayList< DateFormat> _allDateFormats = new ArrayList();
public static void fixAllDateFormats() {
    for (DateFormat df: _allDateFormats)
        _fixDateFormat( df);
}
public static DateFormat makeDateFormat( String fmt) {
    DateFormat df = new SimpleDateFormat( fmt);
    return fixDateFormat( df);
}


/*
locale = US
NNNNow: 10:12 20.04.2010
---FULL
  date: Tuesday, April 20, 2010
  time: 1:12:18 PM GMT+03:00
  datetime: Tuesday, April 20, 2010 1:12:18 PM GMT+03:00
---LONG
  date: April 20, 2010
  time: 1:12:18 PM GMT+03:00
  datetime: April 20, 2010 1:12:18 PM GMT+03:00
---MEDIUM
  date: Apr 20, 2010
  time: 1:12:18 PM
  datetime: Apr 20, 2010 1:12:18 PM
---SHORT
  date: 4/20/10
  time: 1:12 PM
  datetime: 4/20/10 1:12 PM


NNNNow: 10:14 20.04.2010
---FULL
  date: Dienstag, 20. April 2010
  time: 13:14:09 GMT+03:00
  datetime: Dienstag, 20. April 2010 13:14:09 GMT+03:00
---LONG
  date: 20. April 2010
  time: 13:14:09 GMT+03:00
  datetime: 20. April 2010 13:14:09 GMT+03:00
---MEDIUM
  date: 20.04.2010
  time: 13:14:09
  datetime: 20.04.2010 13:14:09
---SHORT
  date: 20.04.10
  time: 13:14
  datetime: 20.04.10 13:14

*/

private static FieldPosition fy = new FieldPosition( DateFormat.YEAR_FIELD );
public static String withoutYear( DateFormat df, Date t) {
    StringBuffer x = df.format( t, new StringBuffer(), fy );
    //delete the year
    x.delete( fy.getBeginIndex(), fy.getEndIndex() );
    //try delete the redundant separator
    String pat = x.toString();
    pat = pat.replaceFirst( "^[ -/.,]*", "");
    pat = pat.replaceFirst( "[ -/.,]*$", "");
    return pat;
}


static public android.text.format.DateFormat andate = new android.text.format.DateFormat();
static public
abstract class dtfm {
    DateFormat fmt;

    static ArrayList< dtfm> _all = new ArrayList();
    dtfm() { _all.add( this); }
    abstract void _setup( Context c);
    public String withoutYear( Date t) { return datetime.withoutYear( fmt, t); }

    public String format( Date dt) { return fmt.format( dt); }

    static String sep = "/";        //where's android's one
    static boolean d_then_m = true;
    static public String format_ddmm( int date, int month) {
        return d_then_m ? date+sep+month : month+sep+date;
    }
    static public String format_ddmm( String date, String month) {
        return d_then_m ? date+sep+month : month+sep+date;
    }

    static void _setup_format_ddmm( Context ctx) {
        boolean found_d = false;
        for (char c: andate.getDateFormatOrder( ctx))
            switch (c) {
                case android.text.format.DateFormat.DATE:
                    found_d = true; continue;
                case android.text.format.DateFormat.MONTH:
                    if (!found_d) d_then_m = false;
                    continue;
            }
    }
    static public void fixAllDateFormats( Context c) {
        for (dtfm m: _all) m._setup( c);
        _setup_format_ddmm( c);
    }
}

public static String withoutYear( dtfm df, Date t) { return df.withoutYear( t); }


//DateFormat timeFormat = fixDateFormat( DateFormat.getTimeInstance( DateFormat.SHORT));    //makeDateFormat( "HH:mm");
//DateFormat dateFormat_dmy = fixDateFormat( DateFormat.getDateInstance( DateFormat.SHORT));    //makeDateFormat( "yy.MM.dd");
//DateFormat dateFormat_dow_d_month_y = fixDateFormat( DateFormat.getDateInstance( DateFormat.FULL));

public static dtfm timeFormat               = new dtfm() { void _setup( Context c) { fmt = andate.getTimeFormat( c); }};
public static dtfm dateFormat_dmy           = new dtfm() { void _setup( Context c) { fmt = andate.getDateFormat( c); }};
public static
class dtfm_dow_d_month_y extends dtfm {
    void _setup( Context c)                     { fmt = andate.getLongDateFormat( c); }     //has no day-of-week
    public String with_dow( Date dt, String r)  { return andate.format("EEEE", dt) + ", " + r; }
    public String format( Date dt)              { return with_dow( dt, super.format(dt)); }
    public String withoutYear( Date dt)         { return with_dow( dt, super.withoutYear(dt)); }
};
public static dtfm dateFormat_dow_d_month_y = new dtfm_dow_d_month_y();
public static dtfm dateFormat_dow_d_month_y_short = new dtfm_dow_d_month_y() {
    public String with_dow( Date dt, String r)  { return andate.format("EEE", dt) + ", " + r; }
};

public static DateFormat dateFormat_yyyymmdd = makeDateFormat( "yyyyMMdd");

public static String dateFormat_dm( Date t)             { return withoutYear( dateFormat_dmy, t); }
public static String dateFormat_dow_d_month( Date t)    { return withoutYear( dateFormat_dow_d_month_y, t); }

/*
static {
    AttributedCharacterIterator a = dateFormat_dmy.formatToCharacterIterator( new Date() );
    //loop inside, skipping fy... too compilcated
}
*/

public static DateFormat datetimeFormat_dbg = makeDateFormat( "HH:mm:ss dd.MM.yyyy ZZ");

static public void dump( Calendar c) {
    //Log.d( "NNNNow: " + c);
    Date t = c.getTime();
    Log.d( "NNNNow: " + datetimeFormat_dbg.format( t ) + " : " + t.getTime()/1000 );
/*
    int[] styles = { DateFormat. FULL, DateFormat. LONG, DateFormat. MEDIUM, DateFormat. SHORT };
    String [] sstyles = funk.split2array("FULL LONG MEDIUM SHORT");
    for (int i=0; i<funk.len(styles); i++) {
        Log.d("---" + sstyles[i]);
        int s = styles[i];
        Log.d("  date: " + DateFormat.getDateInstance( s ).format( dt ) );
        Log.d("  time: " + DateFormat.getTimeInstance( s ).format( dt ) );
        Log.d("  datetime: " + DateFormat.getDateTimeInstance( s, s ).format( dt ) );
    }
*/
}


public static Calendar str2calendar( String s, DateFormat fmt) {
    Calendar c = calendar();
    try {
        Date dd = fmt.parse( s);
        c.setTime( dd);
    } catch (Exception e) {
        e.printStackTrace();
        c = null;
    }
    return c;
}

public static Calendar calendar() {
    Calendar c = now();
    //XXX be ware that these keep the timezone as of their creation! clear() does not touch it
    c.clear();
    //c.setTimeZone(
    return c;
}

}
// vim:ts=4:sw=4:expandtab
