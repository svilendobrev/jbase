package com.svilendobrev.jbase;

import java.lang.Integer;

public class Error {
    public Integer msgId;
    public String msg = "";
    public Error( Integer msgId)    { this.msgId = msgId; }
    public Error( String msg)       { this.msg = msg; }
    public Error( Integer msgId, String msg) { this.msgId = msgId; this.msg = msg; }

    public String toString() { return "Error: " + msgId + " " + msg; }

    public static
    class UserCancel extends Error {
        UserCancel() { super( "cancelled by user"); }
    }
}

// vim:ts=4:sw=4:expandtab
