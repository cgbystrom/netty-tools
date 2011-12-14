package se.cgbystrom.netty.http;

import io.netty.buffer.ChannelBuffers;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageEvent;
import io.netty.channel.SimpleChannelUpstreamHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.Charset;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class SimpleResponseHandler extends SimpleChannelUpstreamHandler {
    private String text;
    private HttpResponseStatus status = HttpResponseStatus.OK;

    public SimpleResponseHandler(String text) {
        this.text = text;
    }

    public SimpleResponseHandler(String text, HttpResponseStatus status) {
        this(text);
        this.status = status;
    }

    public SimpleResponseHandler(String text, int status) {
        this(text);
        this.status = HttpResponseStatus.valueOf(status);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(text, Charset.forName("UTF-8")));
        e.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }
}
