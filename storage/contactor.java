package com.svilendobrev.jbase;

import com.svilendobrev.jbase.funk;

import android.content.Intent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;


import android.content.Context;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

import android.util.Log;

import java.util.ArrayList;

// AndroidManifest.xml:
//  <uses-permission android:name="android.permission.READ_CONTACTS" />
// maybe <uses-permission android:name="android.permission.WRITE_CONTACTS" />

public
class contactor {
/* usage:
    ... somewhere:
        startContactPicker( a, SOME_UNIQ_ID_FOR_PARTICULAR_OP);
        ...
    @Override protected void onActivityResult( int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
         case SOME_UNIQ_ID_FOR_PARTICULAR_OP:
            String idContact = contactor.resultContactPicker2id( resultCode, data);
            particular_op( idContact);
            toastLong( "picked: "+ contactor.id2contact( this, idContact) + " " + contactor.id2emails( this, idContact) );
            break;
        ...
*/

static public
void startContactPicker( Activity a, int id4result) {
    a.startActivityForResult( new Intent(
        Intent.ACTION_PICK, Contacts.CONTENT_URI),
        id4result
        );
}

static public
String resultContactPicker2id( int resultCode, Intent data) {
    if (resultCode != Activity.RESULT_OK) return null;
    Uri result = data.getData();
    Log.v( TAG, "got contact: " + result);
    // get the contact id from the Uri
    return result.getLastPathSegment();
}

static public
ArrayList< String> id2emails( Context a, String id) {
    if (funk.not(id)) return null;
    return query1column( a.getContentResolver(),
            CommonDataKinds.Email.CONTENT_URI,  //CONTENT_LOOKUP_URI ??
            CommonDataKinds.Email.DATA,
            CommonDataKinds.Email.CONTACT_ID + "="+id,
            null, null );
}

static public
aContact id2contact( Context a, String id) {
    if (funk.not(id)) return null;
    return getContact( a, id);
}

/////////

static public
ArrayList< String> query1column( ContentResolver cr, Uri uri, String column, String where, String[] where_args, String orderBy ) {
    ArrayList< String> r = new ArrayList();
    Cursor c = cr.query( uri, new String[] { column }, where, where_args, orderBy);
    if (c.moveToFirst()) do {
            r.add( c.getString(0));
        } while (c.moveToNext());
    c.close();
    return r;
}

static public
class aContact {
    public String id;
    public String name;
    public ArrayList< String > phones;
    public ArrayList< String > emails;
    public String mimeid;
    @Override public
    String toString() { return "aContact: id="+id
        + " name="+name
        + " phones="+phones
        + " emails="+emails
        + " mimeid="+mimeid
        ; }
}

static String _mimetype = "vnd.android.cursor.item/something";

static aContact loadContact( ContentResolver cr, Cursor c, String mimetype ) {
    aContact ac = new aContact();
    ac.id   = c.getString( c.getColumnIndex( Contacts._ID));
    ac.name = c.getString( c.getColumnIndex( Contacts.DISPLAY_NAME));
    if (Integer.parseInt( c.getString( c.getColumnIndex( Contacts.HAS_PHONE_NUMBER))) > 0)
        ac.phones = query1column( cr,
               CommonDataKinds.Phone.CONTENT_URI,
               CommonDataKinds.Phone.NUMBER,
               CommonDataKinds.Phone.CONTACT_ID +"="+ac.id,
               null, null);
    ac.emails = query1column( cr,
        CommonDataKinds.Email.CONTENT_URI,
        CommonDataKinds.Email.DATA,     // use Email.ADDRESS for API-Level 11+
        //maybe also Email.TYPE : CommonDataKinds.Email.getTypeLabelResource( cursor.getInt( ) )
        CommonDataKinds.Email.CONTACT_ID + "="+ac.id,
            null, null );
    if (funk.any( mimetype))
        ac.mimeid = funk.get( query1column( cr,
            Data.CONTENT_URI,
            Data.DATA1,
            Data.CONTACT_ID + "=? AND " + Data.MIMETYPE + "=?" //// '" +mimetype+ "'",
                ,new String[] { ac.id, mimetype },
            null ),
            0, null);
    return ac;
}

static public
ArrayList< aContact> getContacts( ContentResolver cr, String id, String mimetype) {
    Cursor c = null;
    ArrayList< aContact> r = null;
    try {
        c = cr.query( Contacts.CONTENT_URI,  null,
            funk.any(id) ? Contacts._ID +"="+id : null,
            null, null);
        r = new ArrayList();
        if (c.moveToFirst()) do {
            aContact ac = loadContact( cr, c, mimetype);
            r.add( ac);
        } while (c.moveToNext());
        Log.v( TAG, "got contacts["+funk.defaults( id,"")+ "]: " + r);
    } catch (Exception e) { Log.e( TAG, "getContacts: Failed", e); }

    if (c != null) c.close();
    return r;
}
static public ArrayList< aContact> getContacts( Context a, String id, String mimetype) { return getContacts( a.getContentResolver(), id, mimetype); }
static public ArrayList< aContact> getContacts( Context a, String id) { return getContacts( a, id, null); }
public static ArrayList< aContact> getContacts( Context a) { return getContacts( a, null, null ); }
public static aContact getContact( Context a, String id, String mimetype) { return funk.get( getContacts( a, id, mimetype), 0, null ); }
public static aContact getContact( Context a, String id) { return getContact( a, id, null); }

/*
Builder builder = ContentProviderOperation
    .newInsert( Data.CONTENT_URI)
    .withValueBackReference( Data.RAW_CONTACT_ID, 0)
    .withValue( Data.MIMETYPE, "vnd.android.cursor.item/facebook")
    .withValue( "data1", "bkhashfehFacebookId")
    ;

Builder builder = ContentProviderOperation
    .newInsert( Data.CONTENT_URI)
    .withValueBackReference( Data.RAW_CONTACT_ID,0)
    .withValue( Data.MIMETYPE, "ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE")
    .withValue( CommonDataKinds.Im.DATA, "bkhashfehFacebookId")
    .withValue( CommonDataKinds.Im.TYPE,     CommonDataKinds.Im.TYPE_WORK)
    .withValue( CommonDataKinds.Im.PROTOCOL, CommonDataKinds.Im.CUSTOM_PROTOCOL)
    .withValue( CommonDataKinds.Im.CUSTOM_PROTOCOL, "FaceBook")
    ;
*/
static
void save_attr( ContentResolver cr, String id, String data, String mimetype ) {
    ContentValues values = new ContentValues();
    values.put( Data.DATA1, data );
    if (0 < cr.update( Data.CONTENT_URI,
            values,
            Data.RAW_CONTACT_ID + "=" + id
                + " AND " + Data.MIMETYPE + "= '" +mimetype+ "'",
            null))
        return;

    values.put( Data.RAW_CONTACT_ID, id );
    values.put( Data.MIMETYPE, mimetype );
    //values.put( Data.DATA2, CommonDataKinds.BaseTypes.TYPE_CUSTOM);
    cr.insert(  Data.CONTENT_URI, values );
}
static public
void save_attr( Context a, String id, String data, String mimetype ) { save_attr( a.getContentResolver(), id, data, mimetype); }

static String TAG = "svd:contactor";
}//contactor

// vim:ts=4:sw=4:expandtab
