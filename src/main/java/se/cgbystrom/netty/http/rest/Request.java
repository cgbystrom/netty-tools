package se.cgbystrom.netty.http.rest;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class Request extends DefaultHttpRequest
{
    public Request(HttpVersion httpVersion, HttpMethod method, String uri)
    {
        super(httpVersion, method, uri);
    }
}
