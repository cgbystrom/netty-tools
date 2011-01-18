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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;

public class RestHandler extends SimpleChannelHandler {
    public Object rest;
    private Router<Method> router = new Router<Method>();
    private Map<Integer, Method> errorHandlers = new HashMap<Integer, Method>();
    private List<Method> beforeHandlers = new ArrayList<Method>();
    private List<Method> afterHandlers = new ArrayList<Method>();

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

            BeforeHandler bh = method.getAnnotation(BeforeHandler.class);
            if (bh != null) {
                beforeHandlers.add(method);
            }

            AfterHandler ah = method.getAnnotation(AfterHandler.class);
            if (ah != null) {
                afterHandlers.add(method);
            }
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        final Matcher<Method> matcher = router.route(request.getUri());
        HttpResponse response;
        try {
            if (matcher != null) {
                Object o = matcher.getTarget().invoke(null, filterRequest(request));
                response = filterResponse(o);
            } else {
                Method m = errorHandlers.get(404);
                if (m != null) {
                    response = (HttpResponse)m.invoke(null, request);
                } else {
                    response = default404();
                }
            }
        } catch (RestException re) {
            response = internalServerError();
        }


        e.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }

    //@Override
    public void exceptionCaugh1t(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
    {
        HttpResponse response = internalServerError();
        e.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }

    private HttpResponse default404() {
        final Response response = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.NOT_FOUND);
        response.setContent(ChannelBuffers.copiedBuffer("404 Not Found", CharsetUtil.UTF_8));
        return response;
    }

    private HttpRequest filterRequest(HttpRequest request) throws InvocationTargetException, IllegalAccessException {
        HttpRequest filtered = request;
        for (Method method : beforeHandlers) {
            filtered = (HttpRequest)method.invoke(null, filtered);
        }
        return filtered;
    }

    private HttpResponse filterResponse(Object response) throws InvocationTargetException, IllegalAccessException {
        Object filtered = response;
        for (Method method : afterHandlers) {
            filtered = method.invoke(null, filtered);
        }
        return (HttpResponse)filtered;
    }

    private HttpResponse internalServerError() {
        final Response response = new Response(HttpVersion.HTTP_1_0, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        response.setContent(ChannelBuffers.copiedBuffer("500 Internal Server Error", CharsetUtil.UTF_8));
        return response;
    }
}
