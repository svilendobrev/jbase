package com.svilendobrev.storage;

import com.svilendobrev.jbase.funk;
import com.svilendobrev.jbase.Log;
import com.svilendobrev.storage.io.BinData;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import java.util.LinkedHashMap;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

//XXX help: http://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests

public class http {
    static public class CustomException extends RuntimeException {
        public CustomException() { super(); }
        public CustomException( String msg) { super(msg); }
    }
    static public class LoginRequiredException extends CustomException {} //RuntimeExceptions are not checked and don't have to be declared in method's declaration

    public URL    url;

    public String cookie;

    public int debug = 1;

    //err-handling; see struct Error@STC/bmcs/client/transaction/getreply.h
    public enum ErrType {
        OK,
        ERR_USER,   //comm ok, server ok, req-data wrong
        ERR_SYSTEM, //comm ok, server or protocol not ok
        ERR_COMMUNICATION,
    };
    public ErrType  error_type;
    public String   error_message;
    public boolean  error() { return error_type != ErrType.OK; }
    protected void setError() { setError( ErrType.OK, ""); }
    protected void setError( ErrType typ, String msg) { error_type = typ; error_message = msg; }


    public http()            { }
    public http( URL url)    { this.url = url; }

    public int getConnectTimeout_ms() { return 10*1000; }

    public void clearAuthentication() { cookie = null; }
    public void saveAuthentication() {} //override this - save .cookie, whatever

    static public class Result {
        public String cookie;
        public InputStream data;
        public int length;
        public int code;
        public String message;

        public boolean ok() { return code == HttpURLConnection.HTTP_OK; }

        public byte[] readBinary() {
            if (length<=0) return null;     //can be -1??? hmm
            return io.readBinary( data, length);
        }
        public String readText() { return io.read( data); }
    }

    public static class Request {
        //in
        public int debug = 0;
        public int timeout_ms = 10*1000;
        public boolean followRedirects = true;
        //in-out
        public String cookie;
        //out
        public String redirectUrl;
        public Result result;

        public URL getRedirectUrl() throws MalformedURLException {
            funk.assertTrue( redirectUrl != null);
            return new URL( redirectUrl);
        }

        public boolean ok() { return result.ok(); }

        public void send( URL url, Params data) throws IOException {
            //data == null: GET; else POST
            if (debug>0 && url !=null)     Log.d( "REQUEST: " + url);
            if (debug>1 && Params.any(data)) Log.d( "REQUEST: " + data);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (funk.any( cookie)) conn.setRequestProperty( "Cookie", cookie);      //maybe cookie.split(";", 2)[0]); to ignore expires= etc

            try {
                conn.setConnectTimeout( timeout_ms);
                conn.setReadTimeout( timeout_ms);
                conn.setInstanceFollowRedirects( followRedirects);
                if (data != null) conn.setDoOutput( true);    //triggers POST
                if (Params.any( data)) {
                    String x = data.encode_params();
                    if (debug>2) Log.d( "REQUEST.out: " + x);

                    OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream());
                    try {
                        out.write( x);
                        out.flush();
                    } finally { out.close(); }
                }
                result = new Result();
                result.code = conn.getResponseCode();        //trigers connect() etc
                redirectUrl = conn.getHeaderField( "location");
                if (funk.not( redirectUrl)) redirectUrl = conn.getHeaderField( "uri");
                result.cookie = conn.getHeaderField( "Set-Cookie");
                result.length = conn.getContentLength();

                InputStream r;
                try {
                    r = conn.getInputStream();
                } catch (FileNotFoundException e) {
                    r = conn.getErrorStream();
                    result.message = conn.getResponseMessage();
                }

                if (debug>1) {  //consume the stream and replace with cache
                    String s = io.read( r);    //XXX HACK .. should be readBinary
                    Log.d( "RESPONSE: " + s);
                    if (s!=null)
                        r = new ByteArrayInputStream( s.getBytes() );
                }
                result.data = r;
            } finally {
                //conn.disconnect();
            }
        }
    }
    //override if other Request
    public Request _request() { return new Request(); }

    public Result send_http_request( URL url, Params data) throws IOException {
        if (debug>0 && url !=null) Log.d( "RREQUEST: " + url);
        if (url == null) url = this.url;
        Request r = _request();
        r.debug = debug;
        r.timeout_ms = getConnectTimeout_ms();
        r.cookie = cookie;
        r.send( url, data );
        if (r.ok()) {
            if (cookie != r.cookie) {
                cookie = r.cookie;
                saveAuthentication();
            }
            //XXX how this works if conn already closed() XXX
            return r.result;
        }
        String err = "response code: " + r.result.code + "\n message: " + r.result.message;
        setError( ErrType.ERR_COMMUNICATION, "HTTP error: " + url + "\n" + err );
        return null;
    }
    public Result send_http_request( String url, Params data) throws IOException {
        URL u = new URL( url);
        return send_http_request( u, data);
   }

/*
    public
    String checkResponse( int resultCode, String resultText) {
        switch (resultCode) {
            case 100:
            case 101:
                return null;
            case 0:   return "No result tag in PTVResponse";
            case 403: return "Personal channel does not exist / not found";
            case 405: return "EPG event not found";
            case 406: return "Physical channel does not exist / not found";
            case 410: return "Program has already been aded to this channel";
            case 411: return "Personal channel is locked";
            default:
                return "Result code: "+resultCode+": "+resultText;
        }
    }
*/

    public
    BinData _getFile( String path, String name, String localPath, boolean plain_no_cookie) {
        String url = this.url.toString().replaceFirst( "/*$", "")
                        + "/" + path.replaceFirst( "/*$", "").replaceFirst( "^/*", "")
                        + "/" + name;
        try {
            URL u = new URL( url);
            Result r;
            //XXX should these go via cookies or not???
            if (plain_no_cookie) {
                URLConnection conn = u.openConnection();
                r = new Result();
                r.data = conn.getInputStream();
                r.length = conn.getContentLength();
            } else {
                r = send_http_request( u, null);
                if (r == null)
                    throw new FileNotFoundException( url + " : "+ error_message);
            }

            BinData b = new BinData();
            b.length = r.length;
            b.data = r.readBinary();
            if (b.data == null) return null;

            if (localPath != null)
                b.save2file( localPath + name);
            return b;

        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }
/*
    Bitmap _getImage( String path, String name, String localPath ) {
        BinData b = _getFile( path, String name, String localPath, true );
        if (b==null) return null;
        return BitmapFactory.decodeByteArray( b.data,0, b.length);
*/

static public
class Params {
    LinkedHashMap< String, String> params = new LinkedHashMap();

    public Params put( String key, String  value) { params.put( key, value); return this; }
    public Params put( String key, boolean value) { return put( key, ""+value); }
    public Params put( String key, int     value) { return put( key, ""+value); }

    @Override public String toString() { return "Params/"+params; }

    static public
    boolean any( Params p) { return p != null && funk.any( p.params); }

    public String encode_params() {
        String x = "";
        for (String key : params.keySet())
            try {
                if (funk.any(x)) x+="&";
                x += key + "=" + URLEncoder.encode( params.get(key), "UTF-8");
            } catch (Exception e) { e.printStackTrace(); }
        return x;
    }
    public String encode_params_q() {
        String r = encode_params();
        if (funk.any(r)) return "?"+r;
        return r;
    }
}

} //http
// vim:ts=4:sw=4:expandtab
