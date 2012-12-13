package com.svilendobrev.appbase;

//import com.svilendobrev.appbase.Flow;
//import com.svilendobrev.ui.ActivityBase;

public abstract class FlowApp<A extends AppActivity> extends Flow<A> {
    protected abstract AppData getAppData();
    @Override protected void _start( ActivityBase activity) {
        getAppData().open();
        super._start( activity);
    }
    @Override public void end( Event e) {
        super.end( e);
        getAppData().close();
    }
}
// vim:ts=4:sw=4:expandtab
