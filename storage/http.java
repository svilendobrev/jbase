package com.svilendobrev.storage;

import com.svilendobrev.jbase.funk;
import com.svilendobrev.jbase.Log;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import java.util.HashMap;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;

//import java.io.StringBuilder;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

//import org.xml.sax.SAXException;

//XXX help: http://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests

public class http {
    static public class CustomException extends RuntimeException {
        public CustomException() { super(); }
        public CustomException( String msg) { super(msg); }
    }
    static public class LoginRequiredException extends CustomException {} //RuntimeExceptions are not checked and don't have to be declared in method's declaration

    public String url_s;
    public URL    url;

    public String cookie, username, password;

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


    public http( String url) { this.url_s = url; }
    public http( URL url)    { this.url = url; }

    public int getConnectTimeout_ms() { return 10*1000; }

    public void clearAuthentication() { cookie = username = password = null; }
    public void saveAuthentication() {} //override this - save .cookie, whatever

    static public class Result {
        public String cookie;
        public InputStream data;
        public int length;
        public int code;
        public String message;
    }

    static class Request {
        //in
        int debug = 0;
        int timeout_ms = 10*1000;
        String username, password;
        //in-out
        String cookie;
        //out
        String redirectUrl;
        Result result;

        URL getRedirectUrl() throws MalformedURLException {
            funk.assertTrue( redirectUrl != null);
            return new URL( redirectUrl);
        }

        boolean ok() { return result.code == HttpURLConnection.HTTP_OK; }

        void send( URL url, String data) throws IOException, MalformedURLException {
            // - try url( input-data)
            // - in case of redirect - try new url( login-data)
            // - in case of redirect - trying new url( input-data)
            //XXX cycle last one or not?

            HashMap<String, String> d = new HashMap();
            if (funk.any(data)) d.put("data", data);
            _send( url, d);
            if (result.code != 302) return;      //all ok/err , not handled here

            if (funk.not(username) || funk.not(password))
                throw new LoginRequiredException();     //set username+password, repeat all over

            HashMap<String, String> auth = new HashMap();
            auth.put("username", username);
            auth.put("password", password);
            _send( getRedirectUrl(), auth);

            if (result.code == 401)  //Unauthorized
                throw new LoginRequiredException();

            cookie = result.cookie;

            if (result.code == 302) {
                //TODO: avoid infinite redirects
                _send( getRedirectUrl(), d);
            }
        }

        protected
        void _send( URL url, HashMap<String,String> data) throws IOException {
            if (debug>0 && url !=null)     Log.d( "REQUEST: " + url);
            if (debug>1 && funk.any(data)) Log.d( "REQUEST: " + data);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (funk.any( cookie)) conn.setRequestProperty( "Cookie", cookie);      //maybe cookie.split(";", 2)[0]); to ignore expires= etc

            try {
                conn.setDoOutput( true);    //triggers POST
                conn.setConnectTimeout( timeout_ms);
                conn.setReadTimeout( timeout_ms);
                conn.setInstanceFollowRedirects( false);
                if (funk.any(data)) {
                    String x = "";
                    for (String key : data.keySet()) {
                        if (funk.any(x)) x+="&";
                        x += key + "=" + URLEncoder.encode( data.get(key), "UTF-8");
                    }
                    if (debug>2) Log.d( "REQUEST.out: " + x);

                    OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream());
                    try {
                        out.write( x); //URLEncoder.encode( "data="+data, "UTF-8"));
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
                    String s = read( r);
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
    Request request() {
        Request r = new Request();
        r.debug = debug;
        r.timeout_ms = getConnectTimeout_ms();
        r.cookie = cookie;
        r.username = username;
        r.password = password;
        return r;
    }

    public Result send_http_request( URL url, String data) throws IOException {
        if (debug>0 && url !=null) Log.d( "RREQUEST: " + url);
        if (url == null) url = this.url;
        Request r = request();
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

    public
    InputStream get_result_data( URL url, String data) throws IOException {
        Result r = send_http_request( url, data);
        return r != null ? r.data : null;
    }

    public
    BaseSAXHandler _req( URL url, String url_s, String data, BaseSAXHandler handler) {
        setError();
        try {
            // url_s > url > this.url_s > this.url
            if (funk.any( url_s)) url = new URL( url_s);
            else
            if (url==null)
                if (funk.any( this.url_s)) url = new URL( this.url_s);
                //else this.url, in send_http_request
            InputStream i = get_result_data( url, data);
            if (!error())
                handler.parseXML( i);
        } catch (IOException e) {
            setError( ErrType.ERR_COMMUNICATION, e.getMessage());
            e.printStackTrace();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) { //all them  SAXException etc
            setError( ErrType.ERR_SYSTEM, ""+e + "/" + e.getMessage());
            e.printStackTrace();
        }
        if (debug>0 || error())
            Log.d( "RESULT: " +error_type +": "+ (error() ? error_message
                                                                    : "n="+funk.len( handler.many)) );
        if (debug>1 && !error()) Log.d( "RESULT1: " +handler.one);
        if (debug>1 && !error()) Log.d( "RESULTn: " +handler.many);
        return handler;
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

    public static
    String read( InputStream is) {
        char[] buffer = new char[0x10000];
        StringBuilder out = new StringBuilder();
        InputStreamReader in;
        try {
            in = new InputStreamReader(is, "UTF-8");
        } catch (Exception e) { return null; }
        try {
            int read;
            do {
                read = in.read(buffer, 0, buffer.length);
                if (read>0)
                    out.append(buffer, 0, read);
                } while (read>=0);
        } catch (Exception e) { out.append( "\n!!!"+e ); }
        return out.toString();
    }

    public
    Bitmap _getImage( String path, String name, String localPath ) {
        String url = this.url.toString().replaceFirst( "/*$", "")
                        + "/" + path.replaceFirst( "/*$", "").replaceFirst( "^/*", "")
                        + "/" + name;
        try {
            URL u = new URL( url);
            InputStream is;
            int length;
            //XXX should these go via cookies or not???
            if ("plain-non-cookie" == null) {
                URLConnection conn = u.openConnection();
                is = conn.getInputStream();
                length = conn.getContentLength();
            } else {
                Result r = send_http_request( u, null);
                if (r == null)
                    throw new FileNotFoundException( url + " : "+ error_message);
                is = r.data;
                length = r.length;
            }

            byte[] data = new byte[ length];
            int numBytesRead = 0, offset = 0;
            while ((numBytesRead = is.read( data, offset, length-offset)) != -1)
                offset += numBytesRead;

            if (localPath != null)
                try {
                    File localFile = new File( localPath + name);
                    //if (!localFile.exists())
                    BufferedOutputStream out = new BufferedOutputStream( new FileOutputStream( localFile));
                    try {
                        out.write( data,0,length);
                    } finally { out.close(); }
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            return BitmapFactory.decodeByteArray( data,0, length);

        } catch (IOException e) {
            e.printStackTrace(); }
        return null;
    }


} //http
// vim:ts=4:sw=4:expandtab
