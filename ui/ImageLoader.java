package com.svilendobrev.jbase;

import com.svilendobrev.jbase.funk;
import com.svilendobrev.ui.Common;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.ref.WeakReference;
import android.os.AsyncTask;

import android.widget.ImageView;

//http://stackoverflow.com/questions/541966/how-do-i-do-a-lazy-load-of-images-in-listview/3068012#3068012
//http://android-developers.blogspot.com/2010/07/multithreading-for-performance.html
//http://code.google.com/p/iosched/source/browse/android/src/com/google/android/apps/iosched/util/

public abstract
class ImageLoader {
    abstract public void set( Drawable image) ;
    Map< String, Drawable> cache4image;
    String iconURL; //current
    public ImageLoader( Map< String, Drawable> cache4image) { this.cache4image = cache4image; }
    public void set_image( final String iconURL, QueueImage.Observer async ) {
        set( null);
        this.iconURL = iconURL;
        if (funk.not( iconURL)) return ;
        Drawable i = cache4image.get( iconURL);
        if (i!=null) { set( i); return ; }
        if (null==async) {
            i = Common.loadImageFromURL( iconURL);
            cache4image.put( iconURL, i);
            set( i);
            return ;
        }
        //_set_image_async( iconURL, async);

        QueueImage q = queue4image.get( iconURL);
        Log.d( "ImageLoader._set_image_async", "" + iconURL + " " +q+ ": "+async);
        boolean isnew = null==q;
        if (isnew) {
            q = new QueueImage( iconURL);
            queue4image.put( iconURL, q);
        }
        q.observers.add( oimg);
        q.observers.add( async);
        if (isnew) q.execute();
    }
    static private HashMap< String, QueueImage> queue4image = new HashMap();    //todo merge queue4image and cache4image
    QueueImage.Observer oimg = new QueueImage.Observer() { @Override public void run( QueueImage q) {
        Log.d( "ImageLoader.obsi", q.iconURL + " " +q.result + " " + iconURL );
        cache4image.put( q.iconURL, q.result);
        queue4image.remove( q.iconURL);
        if (funk.ne( q.iconURL, iconURL)) return;
        set( q.result);
    }};

/* usage:
    static funk.CacheMap< String, Drawable> cache4image = new funk.CacheMap( 40);
    ImageLoader iii = new ImageLoader( cache4image) { @Override public void set( Drawable i) {
        image=i;
    }};
    public void set_image( QueueImage.Observer async ) {
        iii.set_image( iconURL(), async);
    }
*/
/*
    public QueueImage set_image( QueueImage.Observer async ) {
        image = null;
        String iconURL = iconURL();
        if (funk.not( iconURL)) return null;
        Drawable i = cache4image.get( iconURL);
        if (i!=null) { image = i; return null; }
        if (null==async) {
            image = Common.loadImageFromURL( iconURL);
            cache4image.put( iconURL, image);
            return null;
        } else
            return _set_image_async( iconURL, async);
    }
*/
    static public
    class QueueImage extends AsyncTask< String, Void, Drawable> {
        static public class Observer {  //overload only one
            public void run( String qiconURL) {}
            public void run( QueueImage q) {}
            /*usage:
                PlaceData row;
                ...
                vimage.setTag( theiconURL );
                row.set_image( new QueueImage.Observer() { public void run( String qiconURL) {
                    if (funk.ne( qiconURL, (String)vimage.getTag())) return;
                    vimage.setImageDrawable( row.image); //or q.result
                    }});
                vimage.setImageDrawable( row.image);
            */
        }
        public ArrayList< Observer> observers = new ArrayList();
        public final String iconURL;
        public Drawable result;
        public QueueImage( String iconURL) { this.iconURL = iconURL; }

        @Override protected Drawable doInBackground( String... args) {
            return Common.loadImageFromURL( iconURL); }
        @Override protected void onPostExecute( Drawable image) {
            //progressDlg.dismiss();
            if (isCancelled()) image = null;
            Log.d( "PlaceData.QueueImage", iconURL + " " +image + ": "+observers);
            //cache4image.put( iconURL, image);
            result = image;
            for (Observer o: observers) {
                if (null==o) continue;
                o.run( iconURL);
                o.run( this);
            }
            observers.clear();
        }
        static public class Observer4ImageView extends Observer {
            WeakReference< ImageView> iv;
            public Observer4ImageView( ImageView v, String iconURL) {
                if (null==v) return;
                iv = new WeakReference( v);
                v.setTag( iconURL);
            }
            @Override public void run( QueueImage q) {
                if (null==iv) return;
                ImageView v = iv.get(); if (null==v) return;
                Log.d( "PlaceData.obzv", q.iconURL + " " + q.result + ": "+ (String)v.getTag());
                if (funk.ne( q.iconURL, (String)v.getTag())) return;
                v.setImageDrawable( q.result);
            }
            /*usage:
                PlaceData row;
                ...
                row.set_image( new QueueImage.Observer4ImageView( vimage, theiconURL ));
                vimage.setImageDrawable( row.image);
            */
        }
    }//QueueImage
/*
    static HashMap< String, QueueImage> queue4image = new HashMap();    //todo merge queue4image and cache4image

    QueueImage.Observer oimg = new QueueImage.Observer() { @Override public void run( QueueImage q) {
        image = q.result;
        Log.d( "PlaceData.obsi", q.iconURL + " " +q.result + " " +name()+"/"+iconURL() );
        }};

    public QueueImage _set_image_async( final String iconURL, QueueImage.Observer async) {
        QueueImage q = queue4image.get( iconURL);
        Log.d( "PlaceData._set_image_async", "" + iconURL + " " +q+ ": "+async);
        boolean isnew = null==q;
        if (isnew) {
            q = new QueueImage( iconURL);
            queue4image.put( iconURL, q);
        }
        q.observers.add( oimg);
        q.observers.add( async);
        if (isnew) q.execute();
        return q;
    }
*/

}//ImageLoader
// vim:ts=4:sw=4:expandtab
