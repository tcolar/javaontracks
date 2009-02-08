/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jot.test.server;

import java.net.InetAddress;
import java.net.Socket;
import net.jot.testing.JOTTestable;
import net.jot.testing.JOTTester;
import net.jot.web.server.JOTRequestParser;
import net.jot.web.server.JOTWebRequest;
import net.jot.web.server.JOTWebResponse;

/**
 *
 * @author thibautc
 */
public class ServerTest implements JOTTestable{

    //private final static String FAKE_SESSION_ID="HKKFFJKIUTYFYKGILU76354345JK";
    
    public void __FIXME__jotTest() throws Throwable
    {
        Socket socket=new Socket("www.google.com",80);
        JOTWebRequest request=JOTRequestParser.getTestRequest(socket, "GET /folder/test#anchor?toto=3&block=4 HTTP/1.1");
        request.getHeaders().put("Host","frenchie:8033");
        JOTWebResponse response=new JOTWebResponse(socket, request);

        // fake session id.
        //request.setsessionID=FAKE_SESSION_ID;

        String path="relative.html";
        //String redir=response.encodeURL("relative.html");
        //JOTTester.checkIf("Test redirect url",redir.equals("relative.html;jsessionid="+FAKE_SESSION_ID),redir);
        //System.out.println(response.encodeURL("relative.html#anchor"));
        //JOTTester.checkIf("Test redirect url2",response.encodeURL("relative.html#anchor").equals("relative.html;jsessionid="+FAKE_SESSION_ID+"#anchor"));
        //JOTTester.checkIf("Test redirect url3",response.encodeURL("relative.html#anchor?key=123").equals("relative.html;jsessionid="+FAKE_SESSION_ID+"#anchor?key=123"));
        //JOTTester.checkIf("Test redirect url4",response.encodeURL("/root/test/index.html#anchor?key=123").equals("/root/test/index.html;jsessionid="+FAKE_SESSION_ID+"#anchor?key=123"));
        JOTTester.checkIf("Test redirect url5",response.encodeURL("http://www.google.com/q#blah?id=3").equals("http://www.google.com/q#blah?id=3"));
        
        
        
        
        System.out.println(response.absoluteURL(path));
        JOTTester.checkIf("Test sendRedirect",response.absoluteURL(path).equals(""));


        //String path="relative.html";
        System.out.println(response.encodeRedirectURL(path));
        System.out.println(response.encodeURL(path));
        System.out.println(response.absoluteURL(path));
        path="relative.html#anchor";
        System.out.println(response.encodeRedirectURL(path));
        System.out.println(response.encodeURL(path));
        System.out.println(response.absoluteURL(path));
        path="/root/test/index.html";
        System.out.println(response.encodeRedirectURL(path));
        System.out.println(response.encodeURL(path));
        System.out.println(response.absoluteURL(path));
        path="/root/test/index.html#anchor";
        System.out.println(response.encodeRedirectURL(path));
        System.out.println(response.encodeURL(path));
        System.out.println(response.absoluteURL(path));
        path="http://www.google.com/q#blah?id=3";
        System.out.println(response.encodeRedirectURL(path));
        System.out.println(response.encodeURL(path));
        System.out.println(response.absoluteURL(path));
        socket.close();
    }

    public void jotTest() throws Throwable
    {
    }

}
