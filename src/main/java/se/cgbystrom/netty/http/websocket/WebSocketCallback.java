package se.cgbystrom.netty.http.websocket;

import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

/**
 * Callbacks for the {@link WebSocketClient}.
 * Implement and get notified when events happen.
 *
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public interface WebSocketCallback {
    /**
     * Called when the client is connected to the server
     * @param client Current client used to connect
     */
    public void onConnect(WebSocketClient client);

    /**
     * Called when the client got disconnected from the server.
     * @param client Current client that was disconnected
     */
    public void onDisconnect(WebSocketClient client);

    /**
     * Called when a message arrives from the server.
     * @param client Current client connected
     * @param frame Data received from server
     */
    public void onMessage(WebSocketClient client, WebSocketFrame frame);

    /**
     * Called when an unhandled errors occurs.
     * @param t The causing error
     */
    public void onError(Throwable t);
}
