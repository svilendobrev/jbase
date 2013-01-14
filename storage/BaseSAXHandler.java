package com.svilendobrev.storage;

//port com.svilendobrev.jbase.funk;
import com.svilendobrev.jbase.Model;
import com.svilendobrev.jbase.datetime;
import com.svilendobrev.jbase.Log;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;

import java.util.Date;
import java.util.ArrayList;
import java.text.DateFormat;

import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;

public class BaseSAXHandler extends DefaultHandler {

    public Model.Many many = Model.newCollection();
    public Model one = null;

    protected void _set_item( Model m) {
        one = m;
        many.add( m);
    }

    public boolean dontCreateItem = false;
    public void new_item() {
        if (!dontCreateItem) _new_item();
    }
    protected void _new_item() {}    //inherit in generated


    public int      resultCode;
    public String   resultText;

    protected boolean _do_buffer =true;
    protected String  _buffer ="";
    protected String buffer() {
        String r = _buffer.trim();
        _buffer = "";
        return r;
    }

    public String path;
    ArrayList< HashMap< String, String>> _lastattrs = new ArrayList();
    public
    HashMap< String, String> lastattrs() { return _lastattrs.get( _lastattrs.size() -1 ); }

    public HashMap< String, HashMap< String, String>> uniqattrs = new HashMap();

    @Override
    public void characters( char[] ch, int start, int length) {
        if (_do_buffer) _buffer+= new String( ch, start, length);
    }
    @Override
    public void startElement( String namespace, String name, String qualName, Attributes attrs) throws SAXException {
        _buffer = "";
        path += "/" + name;
        HashMap< String, String> m = new HashMap();
        for (int i= attrs.getLength(); i-->0; )
            m.put( attrs.getLocalName( i), attrs.getValue( i) );
        _lastattrs.add( m);
        uniqattrs.put( name, m);
        //Log.d( ">>> "+ path + "=" + lastattrs() );
        //TODO optimize: move all if's into endElement, using lastattrs there
        //if ("Result" .equals( name))
        //    resultCode = Integer.parseInt( attrs.getValue( "code"));
        //else if ("Details" .equals( name))
        //    resultCode = Integer.parseInt( attrs.getValue( "code"));
        _startElement( namespace, name, qualName, attrs);
    }
    @Override
    public void endElement( String namespace, String name, String qualName) throws SAXException {
        //Log.d( "<<< "+ path + "=" + _buffer);
        if ("Result" .equals( name)) {
            resultCode = Integer.parseInt( lastattrs().get( "code"));
            resultText = buffer();
        } else
            _endElement( namespace, name, qualName);
        path = path.substring( 0, path.length() - 1 - name.length() );
        _lastattrs.remove( _lastattrs.size() -1);
    }
    void _startElement( String namespace, String name, String qualName, Attributes attrs) throws SAXException {}
    void _endElement(   String namespace, String name, String qualName) throws SAXException {}


    @Override
    public void startDocument() throws SAXException {
        path = "";
        resultText = "";
        resultCode = 0;
        _buffer = "";
        _lastattrs.clear();
    }
    @Override
    public void endDocument() throws SAXException {
        //lastattrs = null;
        _buffer = "";
    }

    //these all should handle null
    public static boolean parseBool( String t) {
        if (t == null) return false;
        t = t.trim().toLowerCase();
        if ("true".equals(  t)) return true;
        if ("false".equals( t)) return false;
        return 0!=parseLong( t);
    }
    public static int   parseInt(   String t) { return t == null ? -12 : Integer.parseInt( t.trim()); }
    public static long  parseLong(  String t) { return t == null ? -12 : Long.parseLong(   t.trim()); }
    public static float parseFloat( String t) { return t == null ? -12 : Float.parseFloat( t.trim()); }
    public static String parseString( String t) { return t == null ? "" : t.trim(); }


    static DateFormat _datetimeFormat = datetime.makeDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss"    //'.SSSSSSZZZZ'
            );
    //override this
    public DateFormat datetimeFormat() { return _datetimeFormat; }

    public static Date  parseTimeStamp( String t, DateFormat df) throws SAXException {
        if (t == null) return null;
        try {
            return df.parse( t.trim());
        } catch (Exception e) {
            throw new SAXException( e.toString() );
        }
    }
    public Date parseTimeStamp( String t) throws SAXException {
        return parseTimeStamp( t, datetimeFormat() );
    }

    ///////text-lang-dicts
    public String   context_lang = null;
    private String _lang;
    protected void store_lang( Attributes attrs ) {
        _lang = parseString( attrs.getValue( "lang"));
    }
    boolean isProperLang( String current_text) {
        //first wins
        if (current_text.length()>0) return false;
        if (context_lang==null) return true;
        if (_lang==null) return true;
        return _lang.equals( context_lang) || _lang.equals("und");
    }
    //usage: on startElem: store_lang(); end endelem: if isProperlang() set it

    public String parseLangString( String t, String curvalue) {
        return isProperLang( curvalue) ? parseString(t) : curvalue;
    }
    public static String getByLang( HashMap<String, String> m) {
        if (m == null) return "";
        String s = m.get("deu");
        if (s == null) s = m.get("und");
        if (s == null) s = "null";
        return s;
    }

    //static public parseRole( String t,

    ////////action
    public void parseXML( InputStream inputStream) throws Exception {
        //so whats the difference?
        if (false) {
        SAXParserFactory.newInstance().newSAXParser().parse( new InputSource( inputStream), this);
        //there is also .parse( URL, this);
        } else {
        XMLReader xr = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        xr.setContentHandler( this);
        xr.parse( new InputSource( inputStream));
        }
    }
}

/*XXX
usage

    void handle_req( String requestxml, DefaultHandler handler) {
        InputStream i = send_http_request( requestxml);
        handler.parseXML( i);
        handle-resultcode - http or handler.resultcode
    }

    void somereq1() {
        DefaultHandler h = new ProperHandler1();
        handle_req( "<whatever xml>, h);
        return h.all;   //multi
        or
        return h.it();  //single
    }
    void somereq2() {
        DefaultHandler h = new ProperHandler2();
        handle_req( "<whatever xml>, h);
        return h.all;   //multi
        or
        return h.it();  //single
    }
*/

// vim:ts=4:sw=4:expandtab
