package com.svilendobrev.pref;

import com.svilendobrev.jbase.datetime;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;
import java.text.DateFormat;

public class Date extends Time {
    public static DateFormat format4pref = datetime.makeDateFormat( "yyyy.MM.dd");
    protected DateFormat fmt4pref() { return format4pref; }
    private DatePicker dp;

	public Date(Context context, AttributeSet attrs) { super(context, attrs); }
	//public Date(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

	@Override
	protected View onCreateDialogView() {
        Calendar c = getValue();
		dp = new DatePicker(getContext());
        dp.init( c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), null);
		return dp;
	}

	@Override
    protected void onDialogClosed( boolean positiveResult) {
        if (!positiveResult) return;
        Calendar c = datetime.calendar();
        c.set( Calendar.YEAR, dp.getYear());
        c.set( Calendar.MONTH, dp.getMonth());
        c.set( Calendar.DAY_OF_MONTH, dp.getDayOfMonth());
        saveValue( c);
    }

    public static
    Calendar toCalendar( String value, Calendar c) {
        Calendar d = datetime.str2calendar( value, format4pref);
        if (c==null) return d;
        c.set( d.get(Calendar.YEAR), d.get(Calendar.MONTH), d.get(Calendar.DAY_OF_MONTH));
        return c;
    }
}
// vim:ts=4:sw=4:expandtab
