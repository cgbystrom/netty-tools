package se.cgbystrom.netty.http.websocket;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Handles socket communication for a connected WebSocket client
 * Not intended for end-users. Please use {@link WebSocketClient}
 * or {@link WebSocketCallback} for controlling your client.
 *
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler implements WebSocketClient {
    private ClientBootstrap bootstrap;
    private URI url;
    private WebSocketCallback callback;
    private boolean handshakeCompleted = false;
    private Channel channel;

    public WebSocketClientHandler(ClientBootstrap bootstrap, URI url, WebSocketCallback callback) {
        this.bootstrap = bootstrap;
        this.url = url;
        this.callback = callback;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        String path = url.getPath();
        if (url.getQuery() != null && url.getQuery().length() > 0) {
            path = url.getPath() + "?" + url.getQuery();
        }
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
        request.addHeader(Names.UPGRADE, Values.WEBSOCKET);
        request.addHeader(Names.CONNECTION, Values.UPGRADE);
        request.addHeader(Names.HOST, url.getHost());
        request.addHeader(Names.ORIGIN, "http://" + url.getHost());

        e.getChannel().write(request);
        ctx.getPipeline().replace("encoder", "ws-encoder", new WebSocketFrameEncoder());
        channel = e.getChannel();
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        callback.onDisconnect(this);
        handshakeCompleted = false;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!handshakeCompleted) {
            HttpResponse response = (HttpResponse)e.getMessage();
            final HttpResponseStatus status = new HttpResponseStatus(101, "Web Socket Protocol Handshake");

            final boolean validStatus = response.getStatus().equals(status);
            final boolean validUpgrade = response.getHeader(Names.UPGRADE).equals(Values.WEBSOCKET);
            final boolean validConnection = response.getHeader(Names.CONNECTION).equals(Values.UPGRADE);

            if (!validStatus || !validUpgrade || !validConnection) {
                throw new WebSocketException("Invalid handshake response");
            }
            
            handshakeCompleted = true;
            ctx.getPipeline().replace("decoder", "ws-decoder", new WebSocketFrameDecoder());
            callback.onConnect(this);
            return;
        }

        if (e.getMessage() instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) e.getMessage();
            throw new WebSocketException("Unexpected HttpResponse (status=" + response.getStatus() + ", content=" + response.getContent().toString(CharsetUtil.UTF_8) + ")");
        }

        DefaultWebSocketFrame frame = (DefaultWebSocketFrame)e.getMessage();
        callback.onMessage(this, frame);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        final Throwable t = e.getCause();
        callback.onError(t);    
        e.getChannel().close();
    }

    public ChannelFuture connect() {
        return bootstrap.connect(new InetSocketAddress(url.getHost(), url.getPort()));
    }

    public ChannelFuture disconnect() {
        return channel.close();
    }

    public ChannelFuture send(WebSocketFrame frame) {
        return channel.write(frame);
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }
}