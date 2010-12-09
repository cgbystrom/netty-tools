package se.cgbystrom.netty.http.rest;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import se.cgbystrom.netty.http.router.Matcher;
import se.cgbystrom.netty.http.router.Router;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;

public class RestHandler extends SimpleChannelHandler {
    public Object rest;
    private Router<Method> router = new Router<Method>();
    private Map<Integer, Method> errorHandlers = new HashMap<Integer, Method>();

    public RestHandler(Object rest) {
        this.rest = rest;
        final Method[] methods = rest.getClass().getDeclaredMethods();
        for (Method method : methods) {
            Route r = method.getAnnotation(Route.class);
            if (r != null) {
                router.addRoute(r.path(), method);
            }

            ErrorHandler eh = method.getAnnotation(ErrorHandler.class);
            if (eh != null) {
                errorHandlers.put(eh.value(), method);
            }
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        final Matcher<Method> matcher = router.route(request.getUri());
        HttpResponse response;
        if (matcher != null) {
            response = (HttpResponse) matcher.getTarget().invoke(null, request);
        } else {
            Method m = errorHandlers.get(404);
            if (m != null) {
                response = (HttpResponse)m.invoke(null, request);
            } else {
                response = default404();
            }
        }

        e.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }

    private HttpResponse default404() {
        final Response response = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.NOT_FOUND);
        response.setContent(ChannelBuffers.copiedBuffer("404 Not Found", CharsetUtil.UTF_8));
        return response;
    }
}
