package com.svilendobrev.appbase;

import com.svilendobrev.jbase.funk;
import com.svilendobrev.jbase.Log;
//import com.svilendobrev.ui.ActivityBase;
//import com.svilendobrev.ui.ManagedDialog;

import android.os.Bundle;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.lang.reflect.Method;

public abstract class Flow<A extends ActivityBase> {
    public A activity;
    public HashMap<String, State> states = new HashMap();
    public ArrayList<Flow> children = new ArrayList();
    public Event result;//passed to parent flow when this one has finished
    public String init_state, current_state, last_state;
    public Event current_event;
    public boolean pending = false;
    ArrayList<Class<? extends Flow>> bases_order = new ArrayList();
    HashMap<Class<? extends Flow>, Flow> bases = new HashMap();

    public Flow parent, dispatcher;

    public Flow inherit( Class<? extends Flow> parent) {
        bases_order.add(0, parent);
        return this;
    }
    public Flow inherit( Class<? extends Flow>... parents) {
        funk.addAll( bases_order, parents);
        return this;
    }

    public State makeDef() { return new State(); }

    public <M extends Flow> M Super( Class<M> klas) {
        M base = (M) bases.get( klas);
        base.dispatcher = this;
        return base;
    }

    public State def( String name) {
        State d = states.get( name);
        if (d == null) {
            d = makeDef();
            d.setup( this, name);
            states.put( name, d);
        }
        return d;
    }
    public State def( String name, String descr) {
        State d = def( name);
        d.event2dest.putAll( parse_transitions( descr));
        return d;
    }
    public State def_init( String name, String descr) {
        init_state = name;
        return def( name, descr);
    }

    public Flow disable( String state) {
        states.get( state).disabled = true;
        return this;
    }
    public HashMap<String, String> parse_transitions( String description) {
        HashMap<String, String> res = new HashMap();
        ArrayList<String> words = funk.split( description);
        for (String w : words) {
            ArrayList<String> events_dest = funk.split_skip_empties(w, ":");
            funk.assertTrue( funk.len( events_dest) == 2);
            String dest = events_dest.get(1);
            if ("-".equals( dest) || "null".equals(dest))
                dest = null;
            ArrayList<String> events = funk.split_skip_empties( events_dest.get(0), ",");
            for (String e : events)
                res.put( e, dest);
        }
        return res;
    }

    public void start( ActivityBase activity) {
        activity.s.runningTasks.add( this);
        _start( activity);
    }
    public void start( Flow parent) {
        this.parent = parent;
        parent.children.add( this);
        _start( parent.activity);
    }
    protected void _start( ActivityBase activity) {
        dispatcher = this;
        add_if_missing_in( states, this);
        for (State s : states.values()) {
            //Log.d( "22222222222: "+s);
            if (s.flow != this)
                s.flow.dispatcher = this;
        }
        attach( (A) activity);
        if (init_state == null) {
            for (Class<?extends Flow> klas : bases_order) {
                Flow f = bases.get( klas);
                if (f.init_state != null) {
                    init_state = f.init_state;
                    break;
                }
            }
        }
        init();
    }
    private void _add_if_missing_in( HashMap<String, State> s, Flow child) {
        for (String name : states.keySet()) {
            //Log.d( "11111111111: "+getClass()+ " " +name);
            State cs = s.get( name);
            State ps = states.get( name);
            if (cs == null) {
                if (child != this)
                    ps.setup( child, name); //child overrides mixin
                s.put( name, ps);
            } else {
                if (child != this && cs.meth == null) //child inherits mixin when State is defined but not implemented in child
                    cs.setup( this, name);
                for (String ev : ps.event2dest.keySet())
                    if (!cs.event2dest.containsKey( ev))
                        cs.event2dest.put( ev, ps.event2dest.get( ev));
            }
        }
    }
    private void add_if_missing_in( HashMap<String, State> s, Flow child) {
        _add_if_missing_in( s, child);
        for (Class<? extends Flow> klas : bases_order) {
            Flow f = null;
            try {
                f = klas.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            bases.put( klas, f);
            f.add_if_missing_in( s, this);
        }
    }
    public void setResult( String name) { setResult( name, null); }
    public void setResult( String name, Object data) { setResult( new Flow.Event( name, data)); }
    public void setResult( Flow.Event e) { result = e; }

    public void previousState()                     { nextState( last_state); }
    public void nextState( String state)            { enterState( state, null); }
    public void handle( String event)               { handle( new Event( event)); }
    public void handle( String event, Object data)  { handle( new Event( event, data)); }
    public void handle( Event event) {
        Log.d("zzzzzzzz " +this+" " +dispatcher);
        if (dispatcher != this) {
            dispatcher.handle( event);
            return;
        }
        State d = states.get( current_state);
        //Log.d("4444444444444 " + event2dest + " " + event + " "+ states + " "+current_state);
        String name = event.name;
        if (!d.event2dest.containsKey( name)) name = "*";
        if (!d.event2dest.containsKey( name)) {
            Log.d("Unexpected event: " + this.getClass().getName()
                                + " current_state: "+ current_state
                                + " event:" + event);
            //funk.assertTrue(false);
            return;
        }
        enterState( d.event2dest.get( name), event);
    }

    public void init() { nextState( init_state); }
    public void end( Event event) {
        if (parent != null) {
            parent.children.remove(this);
            parent.handle( result);
        } else
            activity.s.runningTasks.remove( this);
        detach();
    }
    public void finish() { nextState( null); }

    public void attach( A activity) {
        Log.d(">>>>> attach: " + this + " "+ activity);
        this.activity = activity;
        for (Flow f : bases.values()) f.attach( activity);
        for (Flow f : children) f.attach( activity);
        if (pending) {
            pending = false;
            enterState( current_state, current_event);
        }
    }
    public void detach() {
        Log.d(">>>>> detach: " + this);
        activity = null;
        for (Flow f : bases.values()) f.detach();
        for (Flow f : children) f.detach();
    }

    public void enterState( String state, Event event) {
        if (dispatcher != this) {
            dispatcher.enterState( state, event);
            return;
        }

        last_state = current_state;
        current_state = state;
        current_event = event;
        if (current_state == null) current_state = "end";
        State d = states.get( current_state);
        if (d != null && d.disabled)
            current_state = "end";

        Log.d(":::::T " + this + " "+ last_state + " --> "+ current_state + " (" + event + ")");

        //leave old state
        if (funk.any( last_state)) {
            State old = states.get( last_state);
            if (old != null && funk.any( old.run_on_exit)) {
                if (activity == null && !old.can_run_detached) {
                    Log.d("" +this+"...can not leave state \"" + state + "\" while detached");
                    pending = true;
                    return;
                }
                for (Runnable r : old.run_on_exit)
                    r.run();
                last_state = null;
            }
        }

        //enter new state
        if (d != null) {
            if (activity == null && !d.can_run_detached) {
                Log.d("" +this+"...can not enter state \"" + state + "\" while detached");
                pending = true;
                return;
            }
            if (funk.any( d.run_on_enter))
                for (Runnable r : d.run_on_enter)
                    r.run();
            if (d.progressText != null || d.horizontal != null) {
                updateProgress( d.progressText, d.horizontal);
                showProgress();
            } else if (d.showProgress != null) {
                if (d.showProgress) showProgress();
                else hideProgress();
            }
        } else {
            d = def( current_state);
            states.put( current_state, d);
        }
        _execState( d, event);
    }

    protected void _execState( State current, Event event) {
        Object[] input = new Object[1];
        input[0] = event;
        try {
            current.meth.invoke( current.flow, input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    Boolean getBool( Bundle b, String key) { return b.containsKey( key) ? b.getBoolean( key) : null; }

    public void hideProgress() {
        if (activity==null) return;
        //activity.sendHideProgress();
        activity.hideProgress();
    }
    public void showProgress() {
        if (activity==null) return;
        //activity.sendShowProgress();
        activity.showProgress();
    }
    public void updateProgress( Integer progressText) { updateProgress( progressText, null); }
    public void updateProgress( Integer progressText, Boolean horizontal) {
        ManagedDialog.Progress p = new ManagedDialog.Progress();
        if (funk.any(horizontal)) p.value(0);
        p.message( progressText);
        updateProgress( p);
    }
    public void updateProgressValue( Object ov, Object om) {
        if (dispatcher != this) {
            dispatcher.updateProgressValue( ov, om);
            return;
        }
        State current = states.get( current_state);
        if (!funk.any(current.horizontal)) {
            Integer textid = (Integer) ov;
            updateProgress( textid, false);
            return;
        }
        updateProgress( new ManagedDialog.Progress()
            .value( (Integer)ov)
            .max( (Integer)om));
    }
    public void updateProgress( ManagedDialog.Progress setup) {
        if (activity==null) return;
        activity.updateProgress( setup);
    }


public static
class State {
    static Class[] par = new Class[] { Event.class };
    public void setup( Flow f, String meth_name) {
        name = meth_name;
        try {
            meth = f.getClass().getMethod( meth_name, par);
        } catch (Exception e) {
            //e.printStackTrace();
            return;
        }
        flow = f;
    }
    String name;
    Flow flow;
    Method meth;
    HashMap<String, String> event2dest = new HashMap();

    ArrayList<Runnable> run_on_enter;
    ArrayList<Runnable> run_on_exit;

    boolean can_run_detached = false;
    boolean disabled = false;

    Boolean showProgress;
    Integer progressText;
    Boolean horizontal;

    public String toString() {
        return name + " " +flow+ " "+ meth;
    }
    public State onEnter( Runnable r) {
        if (run_on_enter == null) run_on_enter = new ArrayList();
        run_on_enter.add( r);
        return this;
    }
    public State onExit( Runnable r) {
        if (run_on_exit == null) run_on_exit = new ArrayList();
        run_on_exit.add( r);
        return this;
    }

    public State progress( boolean show) {
        showProgress = show;
        return this;
    }
    public State progress( Integer progressText) {
        this.progressText = progressText;
        return this;
    }
    public State progress( Integer progressText, Boolean horizontal) {
        progress( progressText);
        this.horizontal = horizontal;
        return this;
    }
}

public static
class Event {
    public Event(String name) { this( name, null); }
    public Event(String name, Object data) {
        this.name = name;
        this.data = data;
    }
    public String name;
    public Object data;

    public String toString() { return name + " " + data; }
}

} //Flow
// vim:ts=4:sw=4:expandtab
