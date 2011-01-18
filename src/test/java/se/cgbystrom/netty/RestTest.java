package se.cgbystrom.netty;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.jboss.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.cgbystrom.netty.http.FileServerHandler;
import se.cgbystrom.netty.http.rest.*;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class RestTest extends BaseHttpTest {
    public static class SimpleRest {
        @Route(path="/api/basic", methods={"GET", "POST"})
        public static HttpResponse basic1(HttpRequest req) {
            final Response response = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
            response.setContent(ChannelBuffers.copiedBuffer("Hello Planet!", CharsetUtil.UTF_8));
            return response;
        }

        @Route(path="/api/basic2", methods={"GET", "POST"})
        public static HttpResponse basic2(HttpRequest req) {
            final Response response = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
            response.setContent(ChannelBuffers.copiedBuffer("Hello Planet!", CharsetUtil.UTF_8));
            return response;
        }

        @Route(path="/users/<username>", methods={"GET", "POST"})
        public static HttpResponse variableRule(Request req) {
            final Response response = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
            response.setContent(ChannelBuffers.copiedBuffer("Hello Planet!", CharsetUtil.UTF_8));
            return response;
        }
    }
    public static class CustomizedRest {
        @Route(path="/api/customized", methods={"GET", "POST"})
        public static HttpResponse basic1(HttpRequest req) {
            final Response response = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
            response.setContent(ChannelBuffers.copiedBuffer("Hello Planet!", CharsetUtil.UTF_8));
            return response;
        }

        @ErrorHandler(404)
        public static HttpResponse notFound(HttpRequest req) {
            final Response response = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.NOT_FOUND);
            response.setContent(ChannelBuffers.copiedBuffer("Nope, that wasn't found", CharsetUtil.UTF_8));
            return response;
        }
    }

    public static class Before {
        @Route(path="/api/before", methods={"GET", "POST"})
        public static HttpResponse basic1(HttpRequest req) {
            final Response response = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
            response.setContent(ChannelBuffers.copiedBuffer(req.getHeader("Fake-Header"), CharsetUtil.UTF_8));
            return response;
        }

        @BeforeHandler
        public static HttpRequest before(HttpRequest req) {
            req.addHeader("Fake-Header", "This is the before handler");
            return req;
        }
    }

    public static class MultipleBefore {
        @Route(path="/api/before", methods={"GET", "POST"})
        public static HttpResponse basic1(HttpRequest req) {
            final Response response = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
            response.setContent(ChannelBuffers.copiedBuffer(req.getHeader("Fake-Header"), CharsetUtil.UTF_8));
            return response;
        }

        @BeforeHandler
        public static HttpRequest before1(HttpRequest req) {
            req.setHeader("Fake-Header", "1");
            return req;
        }

        @BeforeHandler
        public static HttpRequest before2(HttpRequest req) {
            req.setHeader("Fake-Header", req.getHeader("Fake-Header").concat("2"));
            return req;
        }
    }

    public static class After {
        @Route(path="/api/after", methods={"GET", "POST"})
        public static Object basic1(HttpRequest req) {
            return 3728;
        }

        @AfterHandler
        public static Object after(Object response) {
            String s = "My lucky number is " + (Integer) response;
            final Response httpResponse = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
            httpResponse.setContent(ChannelBuffers.copiedBuffer(s, CharsetUtil.UTF_8));
            return httpResponse;
        }
    }

    public static class MultipleAfter {
        @Route(path="/api/after", methods={"GET", "POST"})
        public static Object basic1(HttpRequest req) {
            return 1;
        }

        @AfterHandler
        public static Object after1(Object response) {
            int i = (Integer)response;
            return ++i;
        }

        @AfterHandler
        public static Object after2(Object response) {
            String s = "My lucky number is " + (Integer) response;
            final Response httpResponse = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
            httpResponse.setContent(ChannelBuffers.copiedBuffer(s, CharsetUtil.UTF_8));
            return httpResponse;
        }
    }

    @Test
    public void basic() throws Exception {
        startServer(new ChunkedWriteHandler(), new RestHandler(new SimpleRest()));
        assertEquals("Hello Planet!", get("/api/basic"));
    }

    @Test
    public void nonExisting() throws Exception {
        startServer(new ChunkedWriteHandler(), new RestHandler(new SimpleRest()));
        get("/api/non-existing", 404);
    }

    @Test
    public void nonExistingCustomized() throws Exception {
        startServer(new ChunkedWriteHandler(), new RestHandler(new CustomizedRest()));
        assertEquals("Nope, that wasn't found", get("/api/non-existing", 404));
    }

    @Test
    public void beforeHandler() throws Exception {
        startServer(new ChunkedWriteHandler(), new RestHandler(new Before()));
        assertEquals("This is the before handler", get("/api/before"));
    }

    @Test
    public void multipleBeforeHandler() throws Exception {
        startServer(new ChunkedWriteHandler(), new RestHandler(new MultipleBefore()));
        assertEquals("12", get("/api/before"));
    }

    @Test
    public void afterHandler() throws Exception {
        startServer(new ChunkedWriteHandler(), new RestHandler(new After()));
        assertEquals("My lucky number is 3728", get("/api/after"));
    }

    @Test
    public void multipleAfterHandlers() throws Exception {
        startServer(new ChunkedWriteHandler(), new RestHandler(new MultipleAfter()));
        assertEquals("My lucky number is 2", get("/api/after"));
    }

    // Make parameters callable

    // Test method POST, GET etc routing
    // Exception handling
    // Non Response responses
    // Unhandled exceptions
    // Customized 404, 500 etc.
}
