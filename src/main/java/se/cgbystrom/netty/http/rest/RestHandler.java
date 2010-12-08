package se.cgbystrom.netty.http.rest;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import java.lang.reflect.Method;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;

public class RestHandler extends SimpleChannelHandler {
    public Object rest;

    public RestHandler(Object rest) {
        this.rest = rest;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        //super.messageReceived(ctx, e);
        Method method = rest.getClass().getDeclaredMethods()[0];
        Route r = method.getAnnotation(Route.class);
        if (r != null) {
            System.out.println("Path: " + r.path());
            //System.out.println("Methods: " + r.path());
            HttpResponse response = (HttpResponse) method.invoke(null, request);
            ChannelFuture writeFuture = e.getChannel().write(response);


            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
