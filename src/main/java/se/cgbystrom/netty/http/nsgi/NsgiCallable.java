package se.cgbystrom.netty.http.nsgi;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public interface NsgiCallable {
    public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next);
}
