package com.svilendobrev.appbase;

import com.svilendobrev.jbase.funk;

import android.os.AsyncTask;
import java.util.HashMap;

public abstract class Task extends AsyncTask< Void, Object, Void> {
    public Flow.Event result;
    public Flow flow;
    public ProgressUpdater updater;
    HashMap<Class<? extends Exception>, String> exception2result;

    public ProgressUpdater makeProgressUpdater() {
        ProgressUpdater updater = new ProgressUpdater() { public void _update( int value, int max) {
            Task.this.publishProgress( value, max);
        }};
        updater.mutex.progressCancelled = false;
        return updater;
    }
    public void on_pre_execute() {}
    public abstract Flow.Event work();
    public void on_post_execute() {}

    public void start( Flow f) { start(f, null); }
    public void start( Flow f, HashMap<Class<? extends Exception>, String> exception2result) {
        this.exception2result = exception2result;
        flow = f;
        execute();
    }
    @Override protected Void doInBackground( Void... unused) {
        try {
            setResult( work());
        } catch (Exception e) {
            if (funk.any( exception2result)) {
                String s = exception2result.get( e.getClass());
                if (funk.not(s)) s = exception2result.get( null);
                setResult( s, e.getMessage());
            }
            else e.printStackTrace();
        }
        return null;
    }
    @Override protected void onPreExecute() {
        updater = makeProgressUpdater();
        on_pre_execute();
    }
    @Override protected void onPostExecute( Void unused) {
        on_post_execute();
        flow.handle( result);
    }
    @Override protected void onProgressUpdate( Object... values) {
        flow.updateProgressValue( values[0], funk.len(values)>1 ? values[1] : null);
    }
    public void setResult( String name) { setResult( name, null); }
    public void setResult( String name, Object data) { setResult( new Flow.Event( name, data)); }
    public void setResult( Flow.Event e) { result = e; }
} //Task
// vim:ts=4:sw=4:expandtab
