//$Id$
package com.svilendobrev.ui;

import com.svilendobrev.jbase.funk;
import com.svilendobrev.jbase.Log;

import android.content.Context;
import android.util.AttributeSet;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout;

import java.util.HashMap;

public class dnd {

static public
class DragBoundary extends LinearLayout {
    DragCtl dragCtl = null;

    public DragBoundary( Context context, AttributeSet attrs) { super(context, attrs); }

    /*
    For as long as you return false from this function, each following event
       (up to and including the final up) will be delivered first here
       and then to the target's onTouchEvent().
    If you return true from here, you will not receive any following events:
        the target view will receive the same event but with the action ACTION_CANCEL,
        and all further events will be delivered to your onTouchEvent() method and no longer
        appear here.
    */
    @Override
    public boolean onInterceptTouchEvent( MotionEvent ev) {
        boolean r = false;
        if (dragCtl != null)
            r = dragCtl.onIntercept( ev);
        return r ? r : super.onInterceptTouchEvent( ev);
    }
}

static public
abstract class DropCtl {
    public static boolean debug = false;

    public View target;
    public String tag;

    public DropCtl( View target) { this.target = target; }
    public DropCtl( View target, String tag) {
        this( target);
        this.tag = tag;
    }
    public String toString() { return funk.not(tag) ? super.toString() : tag; }

    public void onEnter( DragContext c) { if (debug) Log.d("onEnter: " + this); }
    public void onLeave( DragContext c) { if (debug) Log.d("onLeave: " + this); }
    public void onMove(  DragContext c) { if (debug) Log.d("onMove: " + this);  }
    public void onDrop(  DragContext c) { if (debug) Log.d("onDrop: " + this);  }

    public boolean accepts( DragContext c) { return true; }
}

static public
class DragCtl implements View.OnTouchListener, View.OnLongClickListener {
    private ImageView dragProxy;
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;
    private Bitmap dragBitmap;
    private Rect tmpRect = new Rect();
    private Rect tmpRect2 = new Rect();
    private Rect boundaryRect = new Rect();

    private Context context;
    public DragContext dragContext;
    private DropCtl lastDropCtl;

    private int dragOffsetX, dragOffsetY;    // at what offset inside the item did the user grab it
    private int dragOffsetCenterX, dragOffsetCenterY;    // offset from the center of the item
    private int origX, origY;   // x,y of DOWN event
    private int coordOffsetX, coordOffsetY; // the difference between screen coordinates and boundary coordinates
    private boolean movedAfterIntercept = false;
    private MotionEvent fakeUpEvent4Child;

    private int proxyWidth, proxyHeight;

    public  DragBoundary boundary;

    private HashMap<View,View> handler2item = new HashMap();

    private static enum Ops { ENTER, LEAVE, MOVE, DROP };

    public DragCtl( DragBoundary boundary) {
        this.context = boundary.getContext();
        this.boundary = boundary;
        boundary.dragCtl = this;
        boundary.setOnTouchListener( this);
        boundary.setOnLongClickListener( this);
    }

    public boolean onIntercept( MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //Log.d("ZZZZZZZZZZ intercepted ACTION_DOWN");
            lastDropCtl = null;
            movedAfterIntercept = false;

            origX = (int) ev.getX();
            origY = (int) ev.getY();
            boundary.getDrawingRect(boundaryRect);
            fakeUpEvent4Child = MotionEvent.obtain( ev);
            coordOffsetX = ((int)ev.getRawX()) - origX;
            coordOffsetY = ((int)ev.getRawY()) - origY;
            return true; //capture all events in my onTouch from now on
        }
        if (dragProxy != null) {
            //Log.d("onIntercept while dragging: " + ev);
            return onTouch( boundary, ev); //should happen only for the first event after entering drag mode (onLongClick)
        }
        //Log.d("onIntercept: " + ev);
        return false; //let somebody else handle it
    }

    @Override
    public boolean onLongClick( View v) {
        //Log.d("ZZZZZZZZZZ onLongClick: " + v);
        if (movedAfterIntercept)
            return false;
        return onDragStart( v);
    }

    boolean keep_ACTION_UP_in_boundary = true;

    @Override
    public boolean onTouch(View view, MotionEvent ev) {
        //Log.d("onTouch:"+  view + " " + ev);
        int action = ev.getAction();
        if (dragProxy == null) {
            if (action == MotionEvent.ACTION_MOVE)
                movedAfterIntercept = true;
            //Log.d("no drag");
            View child = findChildForEvent( origX, origY);
            if (child != null) {
                MotionEvent childEvent = MotionEvent.obtain( ev);
                Rect r = tmpRect;
                child.getDrawingRect( r);
                boundary.offsetDescendantRectToMyCoords( child, r);
                childEvent.offsetLocation( -r.left, -r.top);
                //Log.d("passToListView: " + child + " "+ childEvent);
                child.onTouchEvent( childEvent);
            }
            return false;
        }

        int x = (int) ev.getX();
        int y = (int) ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:   funk.assertTrue( false); break;
            case MotionEvent.ACTION_CANCEL: stopDragging(); break;

            case MotionEvent.ACTION_UP:
                stopDragging();
                if (!keep_ACTION_UP_in_boundary) {
                    callDropCtl( point2ctl(x, y), x, y, Ops.DROP);
                    break;
                } //else fallover to move

            case MotionEvent.ACTION_MOVE:
                //keep proxy in boundary
                //Log.d("ACTION_MOVE: " + x+ "," + y);
/*
                int x0 = x;// + dragOffsetCenterX - dragOffsetX; //proxy left
                int y0 = y;// + dragOffsetCenterY - dragOffsetY; //proxy top

                Log.d("ACTION_MOVE: " + x0+ "," + y0);
                Rect r = tmpRect;   r.set( boundaryRect);
                Rect p = tmpRect2;  p.set( x0, y0, x0 + proxyWidth, y0 + proxyHeight);
                boolean inside = r.intersect(p) && r.equals( p);
                if (true || !inside) {
                    if (p.left   <  boundaryRect.left)      x += boundaryRect.left - p.left;
                    if (p.right  >  boundaryRect.right)     x -= p.right - boundaryRect.right;
                    if (p.top    <  boundaryRect.top)       y += boundaryRect.top - p.top;
                    if (p.bottom >  boundaryRect.bottom)    y -= p.bottom - boundaryRect.bottom;
                }
*/
                x = funk.max( boundaryRect.left, x);
                y = funk.max( boundaryRect.top,  y);
                x = funk.min( boundaryRect.right-1, x);
                y = funk.min( boundaryRect.bottom-1, y);
                //Log.d("ACTION_MOVE--: " + x+ "," + y);

                DropCtl ctl = point2ctl(x, y);

                if (ev.ACTION_UP == ev.getAction()) {
                    callDropCtl( ctl, x, y, Ops.DROP);
                    break;
                }

                dragMove(x, y);

                Log.d("ACTION_MOVE: " + lastDropCtl + " " + ctl);
                if (lastDropCtl != ctl) {
                    callDropCtl( lastDropCtl, x, y, Ops.LEAVE);
                    enterCtl( ctl, x, y);
                }
                callDropCtl( ctl, x, y, Ops.MOVE);
                break;
        }
        return true;
    }

    private void enterCtl( DropCtl ctl, int x, int y) {
        callDropCtl( ctl, x, y, Ops.ENTER);
        lastDropCtl = ctl;
    }

    private void callDropCtl( DropCtl ctl, int x, int y, Ops op) {
        Log.d("callDropCtl: " + ctl + " " + op + " " + lastDropCtl);

        if (ctl == null) return;

        DragContext c = dragContext;
        if (!ctl.accepts( c)) return;

        View v = ctl.target;
        Rect r = tmpRect;
        v.getDrawingRect( r);
        boundary.offsetDescendantRectToMyCoords( v, r);
        c.x = x - r.left;
        c.y = y - r.top;
        switch (op) {
            case ENTER: ctl.onEnter( c); break;
            case LEAVE: ctl.onLeave( c); break;
            case MOVE:  ctl.onMove( c); break;
            case DROP:  ctl.onDrop( c); break;
            default: funk.assertTrue(false);
        }
    }

    public void makeDraggable( View item) { makeDraggable( item, item); }
    public void makeDraggable( View handler, View item) {
        //handler.setOnLongClickListener( this);
        //handler.setOnTouchListener( this);
        handler2item.put( handler, item);
        //Log.d("added draggable: " + this + " " +  handler + " " + handler2item.size());
    }
    public void makeNotDraggable( View handler) { handler2item.remove( handler); }

    public void registerDropCtl( DropCtl ctl) {
        targets.put( ctl.target, ctl);
    }
    public void unregisterDropCtl( DropCtl ctl) {
        targets.remove( ctl.target);
    }

    protected boolean onDragStart(View zzz) {
        funk.assertTrue( zzz.equals( boundary));

        int x = origX;
        int y = origY;
        View dragItem = null;
        Rect r = tmpRect;
        //Log.d("");
        for (View handler : handler2item.keySet()) {
            View parent = (View) handler.getParent();
            if (parent==null) {
                //handler.getDrawingRect( r);
                //System.err.print("OOPS: no parent; drawing rect: " + r + " handler visibility:" + handler.getVisibility());
                continue;
            }
            handler.getHitRect( r); //Hit rectangle in parent's coordinates
            //System.err.print("hitrect: " + r);
            boundary.offsetDescendantRectToMyCoords( parent, r);
            //System.err.print(" offseted: " + r);
            if (r.contains( x, y)) {
                //Log.d("found item: " + r + " " + origX + " " + origY);
                dragItem = handler2item.get( handler);
                dragOffsetX = x - r.left;
                dragOffsetY = y - r.top;
                dragOffsetCenterX = x - r.centerX();
                dragOffsetCenterY = y - r.centerY();
                break;
            }
            //Log.d("");
        }
        //assertTrue( dragItem != null);
        if (dragItem == null)
            return false;

        dragContext = new DragContext( boundary, dragItem, x, y);
        dragItem.setDrawingCacheEnabled(true); //make sure dragItem does not get recycled when the list tries to clean up memory
        Bitmap bitmap = Bitmap.createBitmap( dragItem.getDrawingCache());

        startDragging( bitmap, x, y);
        //Log.d("DRAG START");
        dragMove( x, y);
        enterCtl( point2ctl(x, y), x, y);
        return true;
    }

    private HashMap<View, DropCtl> targets = new HashMap();

    protected DropCtl point2ctl( int x, int y) {
        Rect r = tmpRect;
        for (View v : targets.keySet()) {
            if (v.getVisibility() != View.VISIBLE) continue;
            v.getDrawingRect(r);
            boundary.offsetDescendantRectToMyCoords( v, r);
            if (r.contains(x, y))
                return targets.get(v);
        }
        return null;
    }

    private void startDragging( Bitmap bm, int x, int y) {
        stopDragging();

        windowParams = new WindowManager.LayoutParams();
        windowParams.gravity = Gravity.LEFT | Gravity.TOP;
        setCoords( x, y);

        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;

        ImageView v = new ImageView(context);
        //int backGroundColor = context.getResources().getColor(R.color.dragndrop_background);
        //v.setBackgroundColor(backGroundColor);
        v.setImageBitmap(bm);
        dragBitmap = bm;

        windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(v, windowParams);
        Rect r = tmpRect;
        v.getDrawingRect( r);
        proxyWidth = r.width();
        proxyHeight = r.height();
        dragProxy = v;
    }

    protected void stopDragging() {
        Log.d( "stopDragging");
        if (dragProxy != null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(dragProxy);
            dragProxy.setImageDrawable(null);
            dragProxy = null;
        }
        if (dragBitmap != null) {
            dragBitmap.recycle();
            dragBitmap = null;
        }
    }

    private void setCoords( int x, int y) {
        //Log.d("setCoords: " + x+ "," + y);
        windowParams.x = x + coordOffsetX;
        windowParams.y = y + coordOffsetY;
        windowParams.x += dragOffsetCenterX - dragOffsetX;
        windowParams.y += dragOffsetCenterY - dragOffsetY;
        //Log.d("setCoords: " + windowParams.x+ "," + windowParams.y);
    }

    private void dragMove( int x, int y) {
        setCoords( x, y);
        windowManager.updateViewLayout(dragProxy, windowParams);
    }

    private View findChildForEvent( int x, int y) {
        int[] childCoords = new int[2]; //unused
        return findChildForEvent( boundary, x, y, childCoords);
    }
    private View findChildForEvent( ViewGroup container, int x, int y, int[] childCoords) {
        final Rect r = tmpRect;
        final int count = container.getChildCount();
        final int scrolledX = x + container.getScrollX();
        final int scrolledY = y + container.getScrollY();

        for (int i = count - 1; i >= 0; i--) {
            final View child = container.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;
            child.getHitRect(r);
            if ( r.contains( scrolledX, scrolledY)) {
                if (!(child instanceof ViewGroup) || targets.containsKey( child) ) {
                    childCoords[0] = x;
                    childCoords[1] = y;
                    return child;
                }
                x = scrolledX - child.getLeft();
                y = scrolledY - child.getTop();
                return findChildForEvent((ViewGroup) child, x, y, childCoords);
            }
        }
        return null;
    }
}


static public
class DragContext {
    public ViewGroup dragSource;
    public View dragItem;
    public int x, y;

    public DragContext( ViewGroup dragSource, View v, int x, int y) {
        this.dragSource = dragSource;
        this.dragItem = v;
        this.x = x;
        this.y = y;
    }
}

///////////////////////////////////////


static public
class ListViewDropCtl extends DropCtl {
    public ListView lv;
    public int itemHeight= -1;
    public Integer itemPosition; // where was the dragged item originally; -1 means not taken from this listview
    public int dropPos;         // where is/was dropped
    private int topBound;
    private int bottomBound;
    private int height;
    private final int touchSlop;

    private Rect tmpRect = new Rect();

    public ListViewDropCtl( ListView target, String tag) {
        super( target, tag);
        this.lv = target;
        touchSlop = ViewConfiguration.get( lv.getContext()).getScaledTouchSlop();
    }


    public void onEnter( DragContext c) {
        super.onEnter( c);
        int y = c.y;
        height = lv.getHeight();
        itemHeight = c.dragItem.getHeight(); //FIXME which size? of the dragItem or of the my first view
        topBound    = height/3;     //Math.min( 1000000+ y - touchSlop, height / 3);
        bottomBound = height*2/3;   //Math.max( -1000000+y + touchSlop, height * 2 /3);

        dropPos = lv.pointToPosition( c.x, c.y);
        funk.assertTrue( itemPosition == null);
        itemPosition = ((dropPos != AdapterView.INVALID_POSITION) && lv.equals( c.dragItem.getParent())) ? dropPos : -1;
        if (debug) Log.d("ZZZZZZZZZZ onEnter:" +this + " " + itemPosition);
    }

    public void onMove( DragContext c) {
        super.onMove( c);
        //System.err.print(".");
        int y = c.y;
        int itemnum = getItemForPosition(y);
        //Log.d("ZZZZZZZZZZ onTouchEvent itemnum="+itemnum + " dropPos="+dropPos);

        if (itemnum >= 0) {
            if (itemnum != dropPos)
                dropPos = itemnum;
            int speed = 0;
            adjustScrollBounds(y);
            if (y > bottomBound)
                speed += y > (bottomBound + height/6) ? height/6 :15; // scroll the list up a bit
            else if (y < topBound)
                speed -= y < (topBound    - height/6) ? height/6 :15; // scroll the list down a bit
            if (speed != 0) {
                if (debug) Log.d("ZZZZZZZZZZ onMove "+y+ " speed:" + speed + " top/" + topBound + " bot/"+bottomBound);
                int ref = lv.pointToPosition(0, height / 2);
                if (ref == AdapterView.INVALID_POSITION) {
                    //we hit a divider or an invisible view, check somewhere else
                    ref = lv.pointToPosition(0, height / 2 + lv.getDividerHeight() );//+ 64);
                }
                View v = lv.getChildAt(ref - lv.getFirstVisiblePosition());
                //if (debug) Log.d("ZZZZZZZZZZ scrol "+v);
                if (v!= null) {
                    int pos = v.getTop();
                    lv.setSelectionFromTop(ref, pos - speed);
                }
            }
        }
        if (debug) Log.d("DDDDDDDDDD onMove itemnum="+itemnum + " dropPos="+dropPos);
    }

    public void onLeave( DragContext c) {
        super.onLeave( c);
        itemPosition = null;
    }

    public void onDrop( DragContext c) {
        super.onDrop( c);
        itemPosition = null;
    }

    /*
     * pointToPosition() doesn't consider invisible views, but we
     * need to, so implement a slightly different version.
     */
    //find i so y is just above/before middle of item(i)
    // before middle of item(0): 0
    // after middle of item(len-1): len
    // no items: INVALID_POSITION
    private int myPointToPosition( int y) {
        if (y < 0)
            return lv.getFirstVisiblePosition();
        Rect r = tmpRect;
        int i = lv.getChildCount();
        if (i<=0)
            return ListView.INVALID_POSITION;       //<0
        while (--i >= 0) {
            final View child = lv.getChildAt(i);
            child.getHitRect( r);
            if (r.centerY() < y ) break;
        }
        return lv.getFirstVisiblePosition() + i + 1;    //>=0, maybe ==len
    }

    private int getItemForPosition(int y) {
        int p = myPointToPosition( y);
        if (debug) Log.d("myp2p "+y + " ="+p);
        return p;
    }

    private void adjustScrollBounds( int y) {
        int h = height / 3;
        if (y >= h) topBound = h;
        h = height * 2 / 3;
        if (y <= h) bottomBound = h;
    }
}
} //dnd
// vim:ts=4:sw=4:expandtab
