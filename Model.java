package com.svilendobrev.jbase;

import java.io.Serializable;

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
}

// vim:ts=4:sw=4:expandtab
