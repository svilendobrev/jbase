package com.svilendobrev.pref;

import com.svilendobrev.jbase.funk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.preference.DialogPreference;
//import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.content.res.ColorStateList;

public class SeekBar extends DialogPreference implements android.widget.SeekBar.OnSeekBarChangeListener {
    private android.widget.SeekBar seekBar;
    private TextView valueText;

    private CharSequence dlgMessage;
    private String suffix;
    private int defaultValue =0, maxValue =10000, step =1, value = 0;

    public SeekBar( Context context, AttributeSet attrs) {
        super(context, attrs);

        Prefs.Int pi = (Prefs.Int)((PrefActivity) getContext()).prefs().getPref( getKey() );
        step         = funk.defaults( pi.step, 1);
        maxValue     = funk.defaults( pi.max_value, 100);
        defaultValue = pi.default_value() == null ? 0 : (Integer) pi.default_value();

        dlgMessage = getDialogMessage();
    }

    @Override
    protected View onCreateDialogView() {
        Context c = getContext();
        /* using dlg resource
        LayoutInflater inflater = (LayoutInflater) c.getSystemService( c.LAYOUT_INFLATER_SERVICE);
        View l = inflater.inflate( R.layout.dlg_update_db, null);
        if (dlgMessage != null)
            ((TextView)l.findViewById( R.id.text)).setText( dlgMessage);
        valueText = (TextView)l.findViewById( R.id.text2);
        valueText.setVisibility( View.VISIBLE);
        value = getPersistedInt( defaultValue );
        seekBar = (android.widget.SeekBar)l.findViewById( R.id.seek);
        seekBar.setProgress( value);
        */

        LinearLayout layout = new LinearLayout( c);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6,6,6,6);

        //XXX this is in AlertDialog, and is not list, hence background IS dark (see AlertController.java)
        //inside alertdialog, and not a list, hence dark background, hence primary_text_dark color! someone inverses it..
        ColorStateList cl = null;
        try {
        cl = ColorStateList.createFromXml( c.getResources(), c.getResources().getXml( android.R.color.primary_text_dark ) );
        } catch (Exception e) {}

        if (dlgMessage != null) {
            TextView t = new TextView( c);//, null, android.R.attr.textAppearanceSmallInverse );
            t.setTextColor( cl);
            t.setText( dlgMessage);
            layout.addView( t);
        }

        value = getPersistedInt( defaultValue );

        valueText = new TextView( c);//, null, android.R.attr.textAppearanceMediumInverse );
        valueText.setTextColor( cl);
        valueText.setGravity( Gravity.CENTER_HORIZONTAL);
        valueText.setTextSize( (float) (valueText.getTextSize()*1.2));
        //updateValueText();
        LayoutParams params = new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        layout.addView( valueText, params);

        seekBar = new android.widget.SeekBar( c);
        seekBar.setOnSeekBarChangeListener( this);
        seekBar.setMax( maxValue);
        seekBar.setKeyProgressIncrement( step);
        seekBar.setProgress( value);
        layout.addView( seekBar, params);

        return layout;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed( positiveResult);
        if (!positiveResult) return;
        if (callChangeListener( value)) persistInt( value);
    }

    public void onProgressChanged( android.widget.SeekBar seek, int value, boolean fromTouch) {
        this.value = value;
        updateValueText();
    }
    public void onStartTrackingTouch( android.widget.SeekBar seek) {}
    public void onStopTrackingTouch( android.widget.SeekBar seek) {}

    void updateValueText() {
        String s = String.valueOf(value);
        valueText.setText( suffix == null ? s : s.concat(suffix));
    }

}

// vim:ts=4:sw=4:expandtab
