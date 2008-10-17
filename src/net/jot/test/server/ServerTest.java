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

    public void jotTest() throws Throwable
    {
        Socket socket=new Socket("www.google.com",80,InetAddress.getLocalHost(),8044);
        JOTWebRequest request=JOTRequestParser.getTestRequest(socket, "GET /folder/test#anchor?toto=3&block=4 HTTP/1.1");
        JOTWebResponse response=new JOTWebResponse(socket, request);
        String path="relative.html";
        System.out.println(response.encodeRedirectURL(path));
        System.out.println(response.encodeURL(path));
        System.out.println(response.absoluteURL(path));
        path="relative,html#anchor";
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
    }

}
