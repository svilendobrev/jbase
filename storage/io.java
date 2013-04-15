package com.svilendobrev.storage;

import com.svilendobrev.jbase.funk;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public
class io {
    public static
    String read( InputStream is) {
        char[] buffer = new char[0x10000];
        StringBuilder out = new StringBuilder();
        InputStreamReader in;
        try {
            in = new InputStreamReader( is, "UTF-8");
        } catch (Exception e) { return null; }
        try {
            int nread;
            do {
                nread = in.read( buffer, 0, buffer.length);
                if (nread>0)
                    out.append( buffer, 0, nread);
                } while (nread>=0);
        } catch (Exception e) { out.append( "\n!!!"+e ); }
        return out.toString();
    }

    public static
    byte[] readBinary( InputStream data, int length) {
        if (length<=0) return null;     //can be -1??? hmm
        byte[] bdata = new byte[ length];
        try {
            int numBytesRead = 0, offset = 0;
            while ((numBytesRead = data.read( bdata, offset, length-offset)) != -1)
                offset += numBytesRead;
            return bdata;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static public class BinData {
        public byte[] data;
        public int length;

        public boolean save2file( String localPathname) {
            try {
                File localFile = new File( localPathname);
                //if (!localFile.exists())
                BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( localFile));
                try {
                    out.write( data, 0, length);
                    return true;
                } finally { out.close(); }
            } catch (IOException e) {
                //e.printStackTrace();
            }
            return false;
        }
        public boolean readBinary( InputStream is, int length ) {
            this.length = length;
            data = io.readBinary( is, length);
            return data != null;
        }
    }
}//io
// vim:ts=4:sw=4:expandtab
