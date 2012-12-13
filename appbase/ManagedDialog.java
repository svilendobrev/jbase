package com.svilendobrev.appbase;

import com.svilendobrev.jbase.funk;
//import com.svilendobrev.appbase.ActivityBase;
//import com.svilendobrev.appbase.ProgressUpdater;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Map;

public abstract class ManagedDialog {
    public static class Listener {
        public void onPositive( DialogInterface dlg, ActivityBase activity) {}
        public void onNegative( DialogInterface dlg, ActivityBase activity) {}
        public void onItem( DialogInterface dlg, int pos, ActivityBase activity) {}
        public Integer validate( DialogInterface dlg, ActivityBase activity) { return null;}
    }

    public Listener listener;
    public String key;
    public Integer title, message, layout;
    public String free_text_message;

    public ManagedDialog message( String message) {
        this.free_text_message = message;
        return this;
    }
    public ManagedDialog message( Integer message) {
        this.message = message;
        return this;
    }
    public ManagedDialog title( Integer title) {
        this.title = title;
        return this;
    }
    public ManagedDialog layout( Integer layout) {
        this.layout = layout;
        return this;
    }
    public ManagedDialog key( String key) {
        this.key = key;
        return this;
    }
    public ManagedDialog listener( Listener listener) {
        this.listener = listener;
        return this;
    }
    public Builder setup( final ActivityBase activity) {
        Builder b = new Builder( activity)
            .setOnCancelListener( new DialogInterface.OnCancelListener() { public void onCancel( DialogInterface dialog) {
                ManagedDialog.this.dismiss( activity);
                if ( ManagedDialog.this.listener != null) ManagedDialog.this.listener.onNegative( dialog, activity);
                else ManagedDialog.this.onNegative( dialog, activity);
            }});
        if (title != null)                  b.setTitle( title);
        if (free_text_message != null)      b.setMessage( free_text_message);
        else if (message != null)           b.setMessage( activity.getText(message));
        if (layout != null) {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService( activity.LAYOUT_INFLATER_SERVICE);
            b.setView( inflater.inflate( layout, null));
        }
        return b;
    }

    public void show( ActivityBase a) {
        if (key == null) key = getClass().getName();
        a.showDialog( this.key, this);
    }
    public Dialog create( ActivityBase activity) {
        return setup( activity).create();
    }
    public void dismiss( ActivityBase a) { a.dismissDialog( key); }
    //public abstract Dialog onCreate( ActivityBase activity);
    //public void onPrepare( Dialog dlg, ActivityBase activity) {};

    public void onNegative( DialogInterface dialog, ActivityBase a) {}

/////////////////////////

public static class Progress extends ManagedDialog {
    public final static String KEY = "progress";
    Integer value, max = 100;    //<=0 -> 100
    //TODO secondary sub-progress?

    public Progress() {
        super();
        key = KEY;
    }
    @Override public ManagedDialog key( String key) { funk.assertTrue(false); return null; }

    public Progress value( int value) {
        this.value = value;
        return this;
    }
    public Progress max( int value) {
        this.max = value;
        return this;
    }
    public boolean horizontal() { return value != null; }

    @Override public Dialog create( ActivityBase activity) {
        ProgressDialog dlg = new ProgressDialog( activity);
        dlg.setCancelable(false);
        if (horizontal()) {
            dlg.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL);
            dlg.setMax( max );
            dlg.setButton( ProgressDialog.BUTTON_POSITIVE, activity.getString( activity.btn_cancel()),
                new DialogInterface.OnClickListener() { public void onClick( DialogInterface dialog, int id) {
                    synchronized (ProgressUpdater.mutex) {
                        ProgressUpdater.mutex.progressCancelled = true;
                    }
                }}
            );
        } else {
            dlg.setProgressStyle( ProgressDialog.STYLE_SPINNER);
            dlg.setIndeterminate(true);
        }
        activity.progressDlg = dlg;
        update( activity, dlg);
        return dlg;
    }

    public void update( ActivityBase activity, ProgressDialog dlg) {
        if (message != null) {
            if (horizontal())
                dlg.setTitle( message);
            else
                dlg.setMessage( activity.getText(message));
        }
        if (max != null) dlg.setMax( max);
        if (value != null) dlg.setProgress( value);
    }
}

public static class Info extends ManagedDialog {
    @Override public Builder setup( final ActivityBase activity) {
        return super.setup( activity).setPositiveButton( activity.btn_ok(),
                new DialogInterface.OnClickListener() { public void onClick( DialogInterface dialog, int id) {
                    Info.this.dismiss( activity);
                    if (listener != null) listener.onPositive( dialog, activity);
                    else Info.this.onPositive( dialog, activity);
                }});
    }
    public void onPositive( DialogInterface dialog, ActivityBase a) {}
}

public static class Confirm extends Info {
    public Integer yes, no;

    public Confirm setYesNo( Integer yes, Integer no) {
        this.yes = yes;
        this.no = no;
        return this;
    }
    @Override public Builder setup( final ActivityBase activity) {
        funk.assertTrue( yes != null && no != null);
        return super.setup( activity)
            .setNegativeButton( no, new DialogInterface.OnClickListener() { public void onClick( DialogInterface dialog, int id) {
                    dialog.cancel();
                }})
            .setPositiveButton( yes, new DialogInterface.OnClickListener() { public void onClick( DialogInterface dialog, int id) {
                    Confirm.this.dismiss( activity);
                    Integer err = listener != null ?
                                listener.validate( dialog, activity) : Confirm.this.validate( dialog, activity);
                    if (err != null) {
                        Confirm.this.show( activity);
                        new Info().message(err).title( activity.title4error()).show( activity);
                    } else {
                        if (listener != null) listener.onPositive( dialog, activity);
                        else Confirm.this.onPositive( dialog, activity);
                    }
                }});
    }
    public Integer validate( DialogInterface dialog, ActivityBase a) { return null; }
}
public static class OkCancel extends Confirm {
    @Override public Builder setup( final ActivityBase activity) {
        setYesNo( activity.btn_ok(), activity.btn_cancel());
        return super.setup( activity);
    }
}
public static class YesNo extends Confirm {
    @Override public Builder setup( final ActivityBase activity) {
        setYesNo( activity.btn_yes(), activity.btn_no());
        return super.setup( activity);
    }
}

public static class Chooser extends ManagedDialog {
    public CharSequence[] items;
    public Integer selectedPos;

    public Chooser setItems( CharSequence[] items) {
        this.items = items;
        return this;
    }
    public Chooser setSelected( Integer pos) {
        this.selectedPos = pos;
        return this;
    }

    @Override public Builder setup( final ActivityBase activity) {
        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() { public void onClick( DialogInterface dialog, int pos) {
                Chooser.this.dismiss( activity);
                if (listener != null) listener.onItem( dialog, pos, activity);
                else Chooser.this.onItem( dialog, pos, activity);
            }};
        Builder b = super.setup( activity);
        if (selectedPos != null)
            b.setSingleChoiceItems( items, selectedPos, l);
        else
            b.setItems( items, l);
        return b;
    }
    public void onItem( DialogInterface dlg, int pos, ActivityBase activity) {}
}


public static abstract class MultiChooser extends Confirm {
    //usage: override onPositive to obtain data

    //int items_id;
    public CharSequence[] items;
    public boolean[] selection_mask;
/*
    public ArrayList< Integer> selection_pos;
    void selection_mask_from_pos() {
        if (selection_mask == null) {
            selection_mask = new boolean[ funk.len( items)];
            for (int i= funk.len( items); --i>=0; )
                selection_mask[i] = ( selection_pos != null && selection_pos.contains( i) );
        }
        return selection_mask;
    }
    public ArrayList< Boolean> selection_mask_to_pos() {
        selection_pos.clear();
        for (int i= 0; i<funk.len( items); i++)
            if (selection_mask[i]) selection_pos.add( i);
        return selection_pos;
    }
*/
    public void selection_from_maps( Map< String, Boolean> values, Map< String, String> names) {
        items = new CharSequence[ funk.len( names) ];
        selection_mask = new boolean[ funk.len( names)];
        int i=0;
        for (String k: values.keySet()) {
            items[i] = names.get( k);
            selection_mask[i] = values.get( k);
            i++;
        }
    }
    public void selection_to_map( Map< String, Boolean> values) {
        int i=0;
        for (String k: values.keySet())
            values.put( k, selection_mask[ i++] );
    }

    @Override public Builder setup( final ActivityBase activity) {
        DialogInterface.OnMultiChoiceClickListener l = new DialogInterface.OnMultiChoiceClickListener() {
            @Override public void onClick( DialogInterface dialog, int pos, boolean set) {
                MultiChooser.this.onItem( dialog, pos, set, activity);
            }};
        return super.setup( activity).setMultiChoiceItems( items, selection_mask, l );
    }
    public void onItem( DialogInterface dlg, int pos, boolean set, ActivityBase activity) {
        selection_mask[ pos] = set;    //toggle?
    }
}

} //ManagedDialog

// vim:ts=4:sw=4:expandtab
