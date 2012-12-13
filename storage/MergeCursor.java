//* ~ Copy paste from The Android Open Source Project, android.database.MergeCursor as that cant be extended

package com.svilendobrev.storage;

import com.svilendobrev.jbase.funk;
import com.svilendobrev.jbase.Log;

import android.database.Cursor;
import android.database.AbstractCursor;
import android.database.DataSetObserver;
import android.database.ContentObserver;
//import android.database.MergeCursor;
import java.util.HashSet;

// an array of constant-sized Cursors as a single linear Cursor.
public class MergeCursor extends AbstractCursor {
//    int[] sizes;
    int[] ends;
    int count;
//    int currentCursor;
    Cursor mCursor;
    Cursor[] mCursors;

    public int getStart( int icursor) { return icursor>0 ? ends[ icursor-1] : 0; }
    public int whichSlice( int newPosition) {
        /* Find the right cursor */
        int i=0;
        for (int end: ends)
            if (newPosition < end) break;
            else i++;
        return i;
    }


    public MergeCursor( Cursor[] cursors, int[] sizes) {
        //for (Cursor c: cursors) Log.d("mcursor " + c);
        mCursors = cursors;
//        currentCursor = 0;
        mCursor = cursors[0];

        funk.assertTrue( sizes.length == mCursors.length);
//        this.sizes = sizes;
        ends = new int[ sizes.length ];

        count = 0;
        for (int i=0; i<sizes.length; i++) {
            count += sizes[i];
            ends[i] = count;
        }

        for (Cursor c: mCursors)
            if (c != null)
                c.registerDataSetObserver(mObserver);
    }

    @Override
    public int getCount() { return count; }

    //XXX all else down is same.... except using whichSlice and re-styled

    private DataSetObserver mObserver = new DataSetObserver() {
        // Reset our position so the optimizations in move-related code
        // don't screw us over
        @Override
        public void onChanged() { mPos = -1; }
        @Override
        public void onInvalidated() { mPos = -1; }
    };

    @Override
    public boolean onMove( int oldPosition, int newPosition) {
        int i = whichSlice( newPosition);
        //Log.d("whichSlice " + newPosition+ "/"+i +"/"+ends[i]);
//      currentCursor = i;
        mCursor = mCursors[i];

        /* Move it to the right position */
        if (mCursor != null) {
            boolean ret = mCursor.moveToPosition( newPosition - getStart(i) );
            //Log.d("onMMMMMMMMMMMMMMMMMove " + newPosition +"/"+i+"/"+ret );
            return ret;
        }
        return false;
    }


    @Override
    public String getString(int column) { return mCursor.getString(column); }
    @Override
    public short getShort(int column) { return mCursor.getShort(column); }
    @Override
    public int getInt(int column) { return mCursor.getInt(column); }
    @Override
    public long getLong(int column) { return mCursor.getLong(column); }
    @Override
    public float getFloat(int column) { return mCursor.getFloat(column); }
    @Override
    public double getDouble(int column) { return mCursor.getDouble(column); }
    @Override
    public boolean isNull(int column) { return mCursor.isNull(column); }
    @Override
    public byte[] getBlob(int column) { return mCursor.getBlob(column); }

    @Override
    public String[] getColumnNames() {
        return (mCursor != null)  ? mCursor.getColumnNames() : new String[0];
    }

    @Override
    public void deactivate() {
        for (Cursor c: mCursors)
            if (c != null)
                c.deactivate();
    }

    @Override
    public void close() {
        for (Cursor c: mCursors)
            if (c != null)
                c.close();
    }

    HashSet< ContentObserver> obc = new HashSet();
    HashSet< DataSetObserver> obd = new HashSet();
    public void unregisterObservers() {
        for (Cursor c: mCursors)
            if (c != null) {
                for (ContentObserver oc: obc) c.unregisterContentObserver(oc);
                for (DataSetObserver od: obd) c.unregisterDataSetObserver(od);
            }
        obc.clear();
        obd.clear();
    }

    @Override
    public void registerContentObserver(ContentObserver observer) {
        if (observer!=null) obc.add( observer);
        for (Cursor c: mCursors)
            if (c != null)
                c.registerContentObserver(observer);
    }
    @Override
    public void unregisterContentObserver(ContentObserver observer) {
        if (observer!=null) obc.remove( observer);
        for (Cursor c: mCursors)
            if (c != null)
                c.unregisterContentObserver(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (observer!=null) obd.add( observer);
        for (Cursor c: mCursors)
            if (c != null)
                c.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer!=null) obd.remove( observer);
        for (Cursor c: mCursors)
            if (c != null)
                c.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean requery() {
        for (Cursor c: mCursors)
            if (c != null)
                if (c.requery() == false)
                    return false;
        return true;
    }

}

// vim:ts=4:sw=4:expandtab
