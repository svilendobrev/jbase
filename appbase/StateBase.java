package com.svilendobrev.appbase;

//import com.svilendobrev.ui.ManagedDialog;

import java.util.ArrayList;
import java.util.HashMap;

public class StateBase {
    public ArrayList< Flow> runningTasks = new ArrayList();
    public HashMap< Integer, ManagedDialog> dialog2setup = new HashMap();
    @Override public String toString() {
        return getClass().getName() + "==============" +
            "\n runningTasks: " + runningTasks +
            "\n dialog2setup: " + dialog2setup;
    }
}

// vim:ts=4:sw=4:expandtab
