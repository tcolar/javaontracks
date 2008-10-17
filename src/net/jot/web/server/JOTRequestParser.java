/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jot.web.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * // TODO: deal with anchors in url (#)
 * // TODO: deal with jsessionID in URL
 * 
 * 
 * Parse a socket input (web request) into an easy to use JOTWebRequest object
 * @author thibautc
 */
public class JOTRequestParser
{
    // Ex: GET /test?toto=3&block=4 HTTP/1.1

    public static Pattern REQUEST_PARSER = Pattern.compile("^(\\S+)\\s+([^\\? ]*)(\\?\\S*)?\\s+(HTTP/\\d+\\.\\d+)");
    public static Pattern HEADER_PATTERN = Pattern.compile("^(\\S+)\\s*:(.*)");

    public static JOTWebRequest parseRequest(Socket socket) throws Exception
    {
        boolean valid=false;
        JOTWebRequest request = new JOTWebRequest();
        request.socket = socket;
        request.setLocalHost(socket.getLocalAddress().getHostAddress());
        request.setLocalPort(socket.getLocalPort());
        request.setRemoteHost(socket.getInetAddress().getHostAddress());
        request.setRemotePort(socket.getPort());

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = "";
        while (reader.ready() && line != null)
        {
            line = reader.readLine();
            //System.out.println("ln: "+line);
            // handle the main request line (path/query/protocol)
            Matcher m2 = HEADER_PATTERN.matcher(line);
            Matcher m = REQUEST_PARSER.matcher(line);
            if (m.matches())
            {
                valid=true;
                parseRequestLine(request, line);
            } else if (m2.matches())
            {
                request.addHeader(m2.group(1), m2.group(2));
            }
        //handler.handleGetRequest(socket, path, params);*/
        }

        // TODO: close the inputstream ??
        /*reader.close();
        if (!socket.isClosed())
        {
        try
        {
        socket.getOutputStream().close();
        } catch (Exception e2)
        {
        
        }
        }*/
        if(!valid)
            throw new IllegalArgumentException("Main Request Line missing or not parseable !");
        return request;
    }

    protected static Hashtable parseParameters(String parameters) throws Exception
    {
        Hashtable hash = new Hashtable();
        if (parameters != null)
        {
            String[] params = parameters.split("&");
            for (int i = 0; i != params.length; i++)
            {
                String p = params[i];
                int index = p.indexOf("=");
                if (index != -1)
                {
                    hash.put(URLDecoder.decode(p.substring(0, index), "UTF-8"), URLDecoder.decode(p.substring(index + 1, p.length()), "UTF-8"));
                } else
                {
                    hash.put(URLDecoder.decode(p, "UTF-8"), "");
                }
            }
        }
        return hash;
    }

    /**
     * Builds a "fake" request for testing.
     * Does not set any headers, etc.., you can add those manually as needed
     * @param socket: a scoket for simulating a connection
     * @param requestLine: ie: something like "GET /test#anchor?toto=3&block=4 HTTP/1.1"
     * @return
     */
    public static JOTWebRequest getTestRequest(Socket socket, String requestLine) throws Exception
    {
        JOTWebRequest request = new JOTWebRequest();
        request.socket = socket;
        request.setLocalHost(socket.getLocalAddress().getHostAddress());
        request.setLocalPort(socket.getLocalPort());
        request.setRemoteHost(socket.getInetAddress().getHostAddress());
        request.setRemotePort(socket.getPort());
        
        Matcher m = REQUEST_PARSER.matcher(requestLine);
        if (!m.matches())
        {
              throw new IllegalArgumentException("Main Request Line not parseable : "+requestLine);
        }
        parseRequestLine(request, requestLine);
 
        return request;
    }

    private static void parseRequestLine(JOTWebRequest request, String line) throws Exception
    {
        Matcher m = REQUEST_PARSER.matcher(line);
        if (m.matches())
        {
            String method = m.group(1);
            String path = URLDecoder.decode(m.group(2), "UTF-8");
            String params = m.group(3);
            if (params != null && params.length() > 0)
            {
                params = params.substring(1, params.length());
            }
            String protocol = m.group(4);
            Hashtable params2 = parseParameters(params);
            request.setRawRequestLine(line);
            request.setMethod(method);
            request.setRawParameters(params);
            request.setParameters(params2);
            request.setPath(path);
            request.setProtocol(protocol);
        }
    }
}
