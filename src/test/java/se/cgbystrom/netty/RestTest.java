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
        @Route(path="/api/basic1", methods={"GET", "POST"})
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

    @Before
    public void setUp() throws Exception {
        startServer(new ChunkedWriteHandler(), new RestHandler(new SimpleRest()));
        Thread.sleep(1000);
    }

    @Test
    public void basicPath() throws Exception {
        assertEquals("Hello Planet!", get("/api/basic1"));
        //get("/api/bas", 404);
        //get("/api/basic123", 404);
    }

    @Test
    public void basicPathTrailing() throws Exception {
        assertEquals("Hello Planet!", get("/api/basic1/"));
    }


    @Test
    @Ignore
    public void basicPath2() throws Exception {
        assertEquals("Hello Planet!", get("/api/hello"));
    }
}
