package se.cgbystrom.netty.http.websocket;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

/**
 * A WebSocket client
 * Controls the basic features of a client.
 *
 * To get notified of events, please see {@link WebSocketCallback}.
 *
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public interface WebSocketClient {
    /**
     * Connect to server
     * Host and port is setup by the factory.
     *
     * @return Connect future. Fires when connected.
     */
    public ChannelFuture connect();

    /**
     * Disconnect from the server
     * @return Disconnect future. Fires when disconnected.
     */
    public ChannelFuture disconnect();

    /**
     * Send data to server
     * @param frame Data for sending
     * @return Write future. Will fire when the data is sent.
     */
    public ChannelFuture send(WebSocketFrame frame);
}
