package se.cgbystrom.netty.http.rest;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import se.cgbystrom.netty.http.router.Matcher;
import se.cgbystrom.netty.http.router.Router;

import java.lang.reflect.Method;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;

public class RestHandler extends SimpleChannelHandler {
    public Object rest;
    private Router router = new Router<Long>();

    public RestHandler(Object rest) {
        this.rest = rest;
        final Method[] methods = rest.getClass().getDeclaredMethods();
        for (Method method : methods) {
            Route r = method.getAnnotation(Route.class);
            if (r != null) {
                router.addRoute(r.path(), method);
            }
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        //System.out.println("Path: " + r.path());
        //super.messageReceived(ctx, e);

        //System.out.println("Methods: " + r.path());
        final Matcher matcher = router.route(request.getUri());
        Method m = (Method)matcher.getTarget();
        if (m != null) {
            HttpResponse response = (HttpResponse) m.invoke(null, request);
            ChannelFuture writeFuture = e.getChannel().write(response);
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
