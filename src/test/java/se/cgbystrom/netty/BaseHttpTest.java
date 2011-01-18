package se.cgbystrom.netty;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import se.cgbystrom.netty.http.websocket.WebSocketCallback;
import se.cgbystrom.netty.http.websocket.WebSocketClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;

public abstract class BaseHttpTest {
    protected int port;

    protected File createTemporaryFile(String content) throws IOException
    {
        File f = File.createTempFile("FileServerTest", null);
        f.deleteOnExit();
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        out.write(content);
        out.close();
        return f;
    }

    protected String get(String uri) throws IOException {
        return get(uri, 200);
    }

    protected String get(String uri, int expectedStatusCode) throws IOException {
        HttpClient client = new HttpClient();

        GetMethod method = new GetMethod("http://localhost:" + port + uri);
        method.getParams().setParameter("http.socket.timeout", new Integer(3000));

        assertEquals(expectedStatusCode, client.executeMethod(method));
        return new String(method.getResponseBody());
    }

    protected int startServer(ChannelHandler... handlers) throws InterruptedException {
        final int port = startServer(new PipelineFactory(handlers));
        Thread.sleep(1000);
        return port;
    }

    protected int startServer(ChannelPipelineFactory factory) {
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(factory);

        port = bindBootstrap(bootstrap, 0);
        return port;
    }

    protected int bindBootstrap(ServerBootstrap bootstrap, int retryCount) {
        try {
            bootstrap.bind(new InetSocketAddress(18080 + retryCount));
        } catch (ChannelException e) {
            retryCount++;
            if (retryCount < 100) {
               return bindBootstrap(bootstrap, retryCount);
            }
        }

        return 18080 + retryCount;
    }

    protected static class PipelineFactory implements ChannelPipelineFactory {
        private ChannelHandler[] handlers;
        private ChannelHandler first = null;

        public PipelineFactory(ChannelHandler... handlers) {
            this.handlers = handlers;
        }

        public void setFirst(ChannelHandler first) {
            this.first = first;
        }

        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = Channels.pipeline();

            if (first != null) {
                pipeline.addLast("first", first);
            }
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
            pipeline.addLast("encoder", new HttpResponseEncoder());

            for (ChannelHandler handler : handlers) {
                pipeline.addLast("handler_" + handler.toString(), handler);
            }

            return pipeline;
        }
    }

    protected static class TestClient implements WebSocketCallback {
        public static final String TEST_MESSAGE = "Testing this WebSocket";
        public boolean connected = false;
        public String messageReceived = null;

        public void onConnect(WebSocketClient client) {
            System.out.println("WebSocket connected!");
            connected = true;
            client.send(new DefaultWebSocketFrame(TEST_MESSAGE));
        }

        public void onDisconnect(WebSocketClient client) {
            System.out.println("WebSocket disconnected!");
            connected = false;
        }

        public void onMessage(WebSocketClient client, WebSocketFrame frame) {
            System.out.println("Message:" + frame.getTextData());
            messageReceived = frame.getTextData();
        }

        public void onError(Throwable t) {
            t.printStackTrace();
        }
    }
}
