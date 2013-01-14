package com.svilendobrev.jbase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

public abstract class Model implements Serializable {

    public abstract String getKey();    //text      //persistence
    public abstract long getId();       //number    //persistence
    public String getDescription()  { return getKey(); }    //visualisation/long text
    public String toString()        { return dump(); }      //visualisation/short text /explain

//    public abstract String repr();

    public String dump() {
        return //getClass().getName() + "/" +
            super.toString();
    }

    static public interface Many extends List<Model> {};
    static public class _List extends ArrayList<Model> implements Many {
        public _List() { super(); }
        public _List( Collection c) { super(c); }
    };
    static public Many newCollection() { return new _List(); }
    static public Many newCollection( Collection c) { return new _List( c); }
}


// vim:ts=4:sw=4:expandtab
