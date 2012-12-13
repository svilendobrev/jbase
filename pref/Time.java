package com.svilendobrev.pref;

import com.svilendobrev.jbase.datetime;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;
import android.preference.DialogPreference;

import java.util.Calendar;
import java.text.DateFormat;

public class Time extends DialogPreference {
    static public DateFormat format4pref = datetime.makeDateFormat( "HH:mm");
    protected DateFormat fmt4pref() { return format4pref; }
    private TimePicker tp;

	public Time( Context context, AttributeSet attrs) { super(context, attrs); }
	//public Time( Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    String getDefaultValue() {
        return fmt4pref().format( datetime.now().getTime());
    }
    Calendar getValue() {
        return datetime.str2calendar( getPersistedString( getDefaultValue()), fmt4pref() );
    }
    void saveValue( Calendar c) {
        String value = fmt4pref().format( c.getTime());
        if (callChangeListener( value)) persistString( value);
    }

	@Override
	protected View onCreateDialogView() {
        Calendar c = getValue();
		tp = new TimePicker(getContext());
        tp.setIs24HourView(true);
        tp.setCurrentHour( c.get(Calendar.HOUR_OF_DAY));
        tp.setCurrentMinute( c.get(Calendar.MINUTE));
		return tp;
	}

	@Override
    protected void onDialogClosed( boolean positiveResult) {
        if (!positiveResult) return;
        Calendar c = datetime.calendar();
        c.set( Calendar.HOUR_OF_DAY, tp.getCurrentHour());
        c.set( Calendar.MINUTE, tp.getCurrentMinute());
        saveValue( c);
    }

    public static
    Calendar toCalendar( String value, Calendar c) {
        Calendar d = datetime.str2calendar( value, format4pref);
        if (c==null) return d;
        c.set( Calendar.HOUR_OF_DAY, d.get( Calendar.HOUR_OF_DAY));
        c.set( Calendar.MINUTE, d.get( Calendar.MINUTE));
        return c;
    }
}
// vim:ts=4:sw=4:expandtab
