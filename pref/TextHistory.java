package com.svilendobrev.pref;

import com.svilendobrev.jbase.funk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.EditText;
import android.preference.DialogPreference;
import android.widget.ArrayAdapter;
import android.content.SharedPreferences;
import android.widget.AdapterView;

import android.text.method.SingleLineTransformationMethod;

import java.util.ArrayList;

// XXX a lot of playing with styles (text color/size/padding/...) is needed
// XXX to get this looking ~ok, and then the dropdown is too short, and items dont show well...

public class TextHistory extends DialogPreference {
    static
    public class aTextHistory {
        protected String key;
        protected Context context;

        public void init( Context context, String key) {
            this.context = context;
            this.key = key;
        }

        protected String getPersistedString()                       { return null; }    //with defaults
        protected void   persistString( String value)               { }
        protected String getPrefString( String key, String defval)  { return null; }
        protected void   setPrefString( String key, String value)   { }
        protected boolean callChangeListener( String value)         { return true; }

        final static String suffix = "_history";
        final static int n_items = 5;

        protected EditText veditor;

        protected ArrayList< String> items;
        protected String  oldvalue;

        protected void loadItems() {
            String vitems = getPrefString( key + suffix, "");
            items = funk.split_skip_empties( vitems, "\n");
        }

        public void set_oldvalue() {
            oldvalue = getPersistedString(); //defaults() );
            veditor.setText( oldvalue );
        }

        public View onCreateDialogView() {
            LinearLayout l = new LinearLayout( context);
            l.setOrientation( LinearLayout.VERTICAL);

            veditor = new EditText( context );
            veditor.setTransformationMethod( SingleLineTransformationMethod.getInstance() );  //singleLine, i.e. no newlines plz
            l.addView( veditor, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            ListView lv = new ListView( context );
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1 );
            lp.setMargins( 15,0,0,5 );  //(left, top, right, bottom)
            l.addView( lv, lp);

            set_oldvalue();
            loadItems();
            if (funk.any( items) ) {
                lv.setAdapter( new ArrayAdapter<String>( context,
                            android.R.layout.simple_dropdown_item_1line,    //XXX better hack your own
                            funk.toArray( items) ));

                lv.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                    public void onItemClick( AdapterView<?> parent, View view, int position, long id) {
                        veditor.setText( items.get( position));
                    } } );
            }
            return l;
        }

        public void onDialogClosed( boolean positiveResult) {
            if (!positiveResult) return;
            String value = getValue();
            if (!callChangeListener( value)) return;
            persistString( value);

            if (value.equals( oldvalue)) return;
            items.remove( value);
            if (funk.len( items) >= n_items)
                funk.pop( items, -1);

            items.add( 0, oldvalue);
            setPrefString( key + suffix, funk.join( items, "\n"));
        }
        public String getValue() { return veditor.getText().toString(); }
    }


    static class dTextHistory extends aTextHistory {
	    TextHistory parent;
	    public dTextHistory( TextHistory parent) {
            init( parent.getContext(), parent.getKey() );
            this.parent = parent;
        }

        String defaults()                       { return ((PrefActivity) context).defaultStr( key ); }
        protected String getPersistedString()   { return parent.getPersistedString( defaults() ); }
        protected void   persistString( String value)   { parent.persistString( value); }
        protected String getPrefString( String k, String defval) {
            return parent.getSharedPreferences().getString( k, defval);
        }
        protected void   setPrefString( String k, String value) {
            SharedPreferences.Editor e = parent.getSharedPreferences().edit();
            e.putString( k, value);
            e.commit();
        }
        protected boolean callChangeListener( String value) { return parent.callChangeListener( value); }
    }

    public aTextHistory th;

	public TextHistory( Context context, AttributeSet attrs) {
        super(context, attrs);
        th = new dTextHistory( this);
    }
	public TextHistory( Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        th = new dTextHistory( this);
    }

	@Override
	protected View onCreateDialogView() { return th.onCreateDialogView(); }
	@Override
    protected void onDialogClosed( boolean positiveResult) { th.onDialogClosed( positiveResult); }
}
// vim:ts=4:sw=4:expandtab
