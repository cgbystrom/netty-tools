package se.cgbystrom.netty;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.jboss.netty.util.CharsetUtil;
import org.junit.Ignore;
import org.junit.Test;
import se.cgbystrom.netty.http.nsgi.BaseNsgiHttpResponse;
import se.cgbystrom.netty.http.nsgi.NsgiCallable;
import se.cgbystrom.netty.http.nsgi.NsgiHandler;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class NsgiTest extends BaseHttpTest {

    @Test
    public void handleError() throws Exception {
        final AtomicInteger called = new AtomicInteger(0);

        NsgiHandler handler = new NsgiHandler(
            new NsgiCallable() {
                public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) throws Exception {
                    next.call(new Exception("Test error"), request, response, null);
                }
            },
            new NsgiCallable() {
                public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) throws Exception {
                    called.incrementAndGet();
                    response.setContent(ChannelBuffers.copiedBuffer(error.getMessage(), CharsetUtil.UTF_8));
                    next.call(error, request, response, null);
                }
            },
            new NsgiCallable() {
                public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) throws Exception {
                    called.incrementAndGet();
                    // Recover exception state
                    next.call(error, request, response, null);
                }
            },
            new NsgiCallable() {
                public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) throws Exception {
                    response.writeHead(200, "OK", null);
                    response.end(request.getContent().toString(CharsetUtil.UTF_8));
                }
            }
            //, // Insert errorHandler
        );

        startServer(new ChunkedWriteHandler(), handler);
        assertEquals("Test error", get("/"));
        assertEquals(2, called.get());
    }

    @Test
    public void catchError() throws Exception {
        startServer(new ChunkedWriteHandler(), new NsgiHandler(
            new NsgiCallable() {
                public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) throws Exception {
                    int a = 1;
                    int b = 0;
                    int c = a / b;
                }
            }));
        String output = get("/", 500);
        System.out.println(output);
    }
}
