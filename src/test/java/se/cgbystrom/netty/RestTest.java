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
import se.cgbystrom.netty.http.rest.ErrorHandler;
import se.cgbystrom.netty.http.rest.Request;
import se.cgbystrom.netty.http.rest.Response;
import se.cgbystrom.netty.http.rest.RestHandler;
import se.cgbystrom.netty.http.rest.Route;

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

    @Before
    public void setUp() throws Exception {
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

    // 404
    // Before, after handler, even multiple
    // Test method routing
    // Exception handling
    // Non Response responses
    // Unhandled exceptions
    // Customized 404, 500 etc.
}
