package se.cgbystrom.netty.http.websocket;

import io.netty.bootstrap.ClientBootstrap;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPipelineFactory;
import io.netty.channel.Channels;
import io.netty.channel.socket.nio.NioClientSocketChannelFactory;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

import java.net.URI;
import java.util.concurrent.Executors;

/**
 * A factory for creating WebSocket clients.
 * The entry point for creating and connecting a client.
 * Can and should be used to create multiple instances.
 *
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public class WebSocketClientFactory {

    private NioClientSocketChannelFactory socketChannelFactory = new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool());

    /**
     * Create a new WebSocket client
     * @param url URL to connect to.
     * @param callback Callback interface to receive events
     * @return A WebSocket client. Call {@link WebSocketClient#connect()} to connect.
     */
    public WebSocketClient newClient(final URI url, final WebSocketCallback callback) {
        ClientBootstrap bootstrap = new ClientBootstrap(socketChannelFactory);

        String protocol = url.getScheme();
        if (!protocol.equals("ws") && !protocol.equals("wss")) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }

        final WebSocketClientHandler clientHandler = new WebSocketClientHandler(bootstrap, url, callback);

        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new HttpResponseDecoder());
                pipeline.addLast("encoder", new HttpRequestEncoder());
                pipeline.addLast("ws-handler", clientHandler);
                return pipeline;
            }
        });

        return clientHandler;
    }
}
