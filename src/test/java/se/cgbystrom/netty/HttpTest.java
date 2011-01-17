package se.cgbystrom.netty;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.junit.Test;
import se.cgbystrom.netty.http.BandwidthMeterHandler;
import se.cgbystrom.netty.http.CacheHandler;
import se.cgbystrom.netty.http.FileServerHandler;
import se.cgbystrom.netty.http.SimpleResponseHandler;
import se.cgbystrom.netty.http.nsgi.BaseNsgiHttpResponse;
import se.cgbystrom.netty.http.nsgi.NsgiCallable;
import se.cgbystrom.netty.http.nsgi.NsgiHandler;
import se.cgbystrom.netty.http.router.RouterHandler;
import se.cgbystrom.netty.http.websocket.WebSocketClient;
import se.cgbystrom.netty.http.websocket.WebSocketClientFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;

public class HttpTest extends BaseHttpTest {

    @Test
    public void serveFromFileSystem() throws IOException, InterruptedException {
        final String content = "Testing the file system";
        File f = createTemporaryFile(content);
        startServer(new ChunkedWriteHandler(), new FileServerHandler(f.getParent()));
        Thread.sleep(1000);

        assertEquals(content, get("/" + f.getName()));
    }

    @Test
    public void serveFromClassPath() throws IOException, InterruptedException {
        startServer(new ChunkedWriteHandler(), new FileServerHandler("classpath:///"));
        Thread.sleep(1000);

        assertEquals("Testing the class path", get("/test.txt"));
    }

    @Test
    public void cacheMaxAge() throws IOException, InterruptedException {
        final String content = "Testing the file system";
        File f = createTemporaryFile(content);
        startServer(new CacheHandler(), new ChunkedWriteHandler(), new FileServerHandler(f.getParent(), 100));
        Thread.sleep(1000);

        assertEquals(content, get("/" + f.getName()));
        assertTrue(f.delete());
        assertEquals(content, get("/" + f.getName()));
    }

    @Test
    public void cacheMaxAgeExpire() throws IOException, InterruptedException {
        final String content = "Testing the file system";
        File f = createTemporaryFile(content);
        startServer(new CacheHandler(), new ChunkedWriteHandler(), new FileServerHandler(f.getParent(), 1));
        Thread.sleep(1000);

        assertEquals(content, get("/" + f.getName()));
        Thread.sleep(2000);
        assertTrue(f.delete());
        get("/" + f.getName(), 404);
    }

    @Test
    public void router() throws Exception {
        final String startsWith = "startsWith:/hello-world";
        final String endsWith = "endsWith:/the-very-end";
        final String equals = "equals:/perfect-match";
        LinkedHashMap<String, ChannelHandler> routes = new LinkedHashMap<String, ChannelHandler>();
        routes.put(startsWith, new SimpleResponseHandler(startsWith));
        routes.put(endsWith, new SimpleResponseHandler(endsWith));
        routes.put(equals, new SimpleResponseHandler(equals));

        startServer(new ChunkedWriteHandler(), new RouterHandler(routes));

        assertEquals(startsWith, get("/hello-world/not-used"));
        assertEquals(endsWith, get("/blah-blah/blah/the-very-end"));
        assertEquals(equals, get("/perfect-match"));
        assertFalse(equals.equals(get("/perfect-match/test", 404)));
        assertEquals("Not found", get("/not-found", 404));
    }

    @Test
    public void bandwidthMeter() throws Exception {
        final String data = "Bandwidth metering!";
        final BandwidthMeterHandler meter = new BandwidthMeterHandler();

        PipelineFactory pf = new PipelineFactory(new ChunkedWriteHandler(), new SimpleResponseHandler(data));
        pf.setFirst(meter);
        startServer(pf);

        assertEquals(data, get("/bandwidth"));
        assertEquals(79, meter.getBytesSent());
        assertEquals(94, meter.getBytesReceived());

        meter.reset();
        assertEquals(0, meter.getBytesSent());
        assertEquals(0, meter.getBytesReceived());
    }

    @Test
    public void webSocketClient() throws Exception {
        startServer(new WebSocketServerHandler());

        WebSocketClientFactory clientFactory = new WebSocketClientFactory();
        final TestClient callback = new TestClient();

        WebSocketClient client = clientFactory.newClient(new URI("ws://localhost:" + port + "/websocket"), callback);

        client.connect().awaitUninterruptibly();
        Thread.sleep(1000);

        assertTrue(callback.connected);
        assertEquals(TestClient.TEST_MESSAGE.toUpperCase(), callback.messageReceived);
        client.disconnect();
        Thread.sleep(1000);

        assertFalse(callback.connected);
    }

    @Test
    public void nsgi() throws Exception {
        startServer(new ChunkedWriteHandler(), new NsgiHandler(new NsgiCallable() {
            void call(HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) {
                response.writeHead(200, "OK", null);
                response.end("Din mamma");
            }
        }));

        assertEquals("Din mamma", get("/"));
    }


}
