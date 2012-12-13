package com.svilendobrev.pref;

import com.svilendobrev.jbase.funk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.AutoCompleteTextView;
import android.preference.DialogPreference;
import android.widget.ArrayAdapter;
import android.content.SharedPreferences;

import android.text.method.SingleLineTransformationMethod;

import java.util.ArrayList;

// FIXME not good for a history - use TextHistory itself and use+fix this when a real Completer is needed
// XXX a lot of playing with styles (text color/size/padding/...) is needed
// XXX to get this looking ~ok, and then the dropdown is too short, and items dont show well...

public class TextCompleter extends TextHistory {
	public TextCompleter( Context context, AttributeSet attrs) { super(context, attrs); }
	public TextCompleter( Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

	@Override
	protected View onCreateDialogView() {
        LinearLayout l = new LinearLayout( getContext());
        AutoCompleteTextView av = new AutoCompleteTextView( getContext() ) {
                @Override public boolean enoughToFilter() { return true; }  //XXX forget about threshold etc
                @Override protected void performFiltering( CharSequence text, int keyCode) {
                    getFilter().filter( "", this); }    //no filter: show all
                };
        av.setTransformationMethod( SingleLineTransformationMethod.getInstance() );  //singleLine, i.e. no newlines plz
        l.addView( av, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        l.setPadding( 0,0,0,90);  //(left, top, right, bottom)  //to have more space for popup? but it wont really use it

        th.veditor = av;
        th.set_oldvalue();
        th.loadItems();
        if (funk.any( th.items) )
            av.setAdapter( new ArrayAdapter<String>( getContext(),
                        android.R.layout.simple_dropdown_item_1line,    //XXX better hack your own
                        funk.toArray( th.items) ));

		return l;
    }
}
// vim:ts=4:sw=4:expandtab
