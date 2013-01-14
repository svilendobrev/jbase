package com.svilendobrev.jbase;

import java.io.Serializable;
import java.util.ArrayList;

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

    static public interface Collection extends java.util.List<Model> {};
    static public class _List extends ArrayList<Model> implements Collection {
        public _List() { super(); }
        public _List( java.util.Collection c) { super(c); }
    };
    static public Collection newCollection() { return new _List(); }
    static public Collection newCollection( java.util.Collection c) { return new _List( c); }
}


// vim:ts=4:sw=4:expandtab
