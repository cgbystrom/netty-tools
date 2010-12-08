package se.cgbystrom.netty.http.rest;

import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class Response extends DefaultHttpResponse
{
    public Response(HttpVersion version, HttpResponseStatus status)
    {
        super(version, status);
    }
}
