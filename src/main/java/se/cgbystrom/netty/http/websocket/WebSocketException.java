package se.cgbystrom.netty.http.websocket;

import java.io.IOException;

/**
 * A WebSocket related exception
 *
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a> 
 */
public class WebSocketException extends IOException {
    public WebSocketException(String s) {
        super(s);
    }

    public WebSocketException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
