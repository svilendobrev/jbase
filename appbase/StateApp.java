package com.svilendobrev.appbase;

//import com.svilendobrev.appbase.StateBase;

public class StateApp extends StateBase {
    public boolean needRefresh = true;
    @Override public String toString() {
        return super.toString()+ "\n needRefresh: " + needRefresh;
    }
}

// vim:ts=4:sw=4:expandtab
