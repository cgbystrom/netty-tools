package se.cgbystrom.netty;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.jboss.netty.util.CharsetUtil;
import se.cgbystrom.netty.http.nsgi.BaseNsgiHttpResponse;
import se.cgbystrom.netty.http.nsgi.NsgiCallable;
import se.cgbystrom.netty.http.nsgi.NsgiHandler;

import java.util.concurrent.atomic.AtomicInteger;

public class NsgiTest {

    public void handleError() {
        final AtomicInteger called = new AtomicInteger(0);

        NsgiHandler handler = new NsgiHandler(
            new NsgiCallable() {
                public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) {
                    called.incrementAndGet();
                    next.call(new Exception("Test error"), request, response, null);

                }
            },
            new NsgiCallable() {
                public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) {
                    called.incrementAndGet();
                    response.setContent(ChannelBuffers.copiedBuffer(error.getMessage(), CharsetUtil.UTF_8));
                    next.call(error, request, response, null);
                }
            },
            new NsgiCallable() {
                public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) {
                    called.incrementAndGet();
                    // Recover exception state
                    next.call(error, request, response, null);
                }
            },
            new NsgiCallable() {
                public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) {
                    response.writeHead(200, "OK", null);
                    response.end(request.getContent().toString());
                }
            }
            //, // Insert errorHandler
        );

        
    }
}
