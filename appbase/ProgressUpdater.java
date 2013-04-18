package com.svilendobrev.appbase;

abstract public class ProgressUpdater {
    static public
    class Mutex extends Object {
        public boolean progressCancelled = false;
    }
    static public final Mutex mutex = new Mutex();
    final static int MAX = 100;
    private int cur   = -1;
    private int max   = MAX;

    public  int unit = 1; //divide value and max by this - e.g.for bytes shown in KB: unit=1024

        //multiple runs seens as one whole:
    public  int scale = 1;      //scaling the max
    private int offset= 0;      //move the start after each run

    public void offset_to_current() { offset = cur+1; }  //might be wrong
    public void offset_to_next()    { //might be wrong too
        offset += max/scale;
        if (offset<=cur) offset_to_current();
    }

    static public boolean stopped() {
        synchronized (mutex) {
            return mutex.progressCancelled;
    } }
    static public void cancel() {
        synchronized (mutex) {
            mutex.progressCancelled = true;
    } }
    //show as percent (0..100)
    public void update( int percent ) {
        update( percent, MAX); }

    //show as percent (= value/max)
    public void update1( int value, int max) {
        update( (int)(100*(value/(double)max))); } //max*scale ?

    //show both percent/100 and value/max
    public void update( int value, int max) {
        //Log.d( "update1: " + value + " , 0=" + offset + " max="+max);
        if (value>=0) value += offset;
        if (max<=0) max = MAX;
        value /= unit;
        max   /= unit;
        //moving resolution 1%
        int rcur = (int)(100*(cur  /(double)this.max));
        int rnew = (int)(100*(value/(double)max));
        if (rcur == rnew) return;

        cur = value;
        this.max = max;
        _update( value, max*scale );
    }

    abstract public void _update( int value, int max);
}

// vim:ts=4:sw=4:expandtab
