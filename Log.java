package com.svilendobrev.jbase;

import com.svilendobrev.jbase.funk;

public class Log {

public static void d( Class klas, String msg) {
    String tag = funk.rsplit_last( klas.getName(), "[.]");
    d( tag, msg);
}
public static void d( String msg) {
    android.util.Log.d( "svilendobrev.jbase", msg);
}
public static void d( String tag, String msg) {
    if (android.util.Log.isLoggable( tag, android.util.Log.DEBUG))
        android.util.Log.d( tag, msg);
}
public static void i( String tag, String msg) {
    if (android.util.Log.isLoggable( tag, android.util.Log.INFO))
        android.util.Log.i( tag, msg);
}
public static void e( String tag, String msg) {
    if (android.util.Log.isLoggable( tag, android.util.Log.ERROR))
        android.util.Log.e( tag, msg);
}
public static void v( String tag, String msg) {
    if (android.util.Log.isLoggable( tag, android.util.Log.VERBOSE))
        android.util.Log.v( tag, msg);
}
public static void w( String tag, String msg) {
    if (android.util.Log.isLoggable( tag, android.util.Log.WARN))
        android.util.Log.w( tag, msg);
}
public static  String getStackTraceString( Throwable tr) {
    return android.util.Log.getStackTraceString( tr);
}

}
// vim:ts=4:sw=4:expandtab
