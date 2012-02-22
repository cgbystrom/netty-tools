package se.cgbystrom.netty.http.router;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpRequest;
import se.cgbystrom.netty.http.SimpleResponseHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public class RouterHandler extends SimpleChannelUpstreamHandler {

    private Map<Matcher, ChannelHandler> routes = new LinkedHashMap<Matcher, ChannelHandler>();
    private static final String STARTS_WITH = "startsWith:";
    private static final String ENDS_WITH = "endsWith:";
    private static final String EQUALS = "equals:";
    private static final ChannelHandler HANDLER_404 = new SimpleResponseHandler("Not found", 404);
    private boolean handleNotFound;

    public RouterHandler(LinkedHashMap<String, ChannelHandler> routes, boolean handleNotFound) throws Exception {
        this.handleNotFound = handleNotFound;
        setupRoutes(routes);
    }

    public RouterHandler(LinkedHashMap<String, ChannelHandler> routes) throws Exception {
        this.handleNotFound = true;
        setupRoutes(routes);
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof MessageEvent && ((MessageEvent)e).getMessage() instanceof HttpRequest) {
            HttpRequest request = (HttpRequest)((MessageEvent)e).getMessage();

            String uri = request.getUri();
            boolean matchFound = false;
            for (Map.Entry<Matcher, ChannelHandler> m : routes.entrySet()) {
                if (m.getKey().match(uri)) {
                    ChannelPipeline p = ctx.getPipeline();
                    synchronized (p) {
                        if (p.get("route-generated") == null) {
                            p.addLast("route-generated", m.getValue());
                        } else {
                            p.replace("route-generated", "route-generated", m.getValue());
                        }
                    }
                    matchFound = true;
                    break;
                }
            }

            /*
            If the route can't be found and we are supposed to handle not found URLs we append a 404 handler
             */
            if (!matchFound && handleNotFound) {
                ctx.getPipeline().addLast("404-handler", HANDLER_404);
            }
        }

        super.handleUpstream(ctx, e);
    }

    private class StartsWithMatcher implements Matcher {
        private String route;

        private StartsWithMatcher(String route) {
            this.route = route;
        }

        public boolean match(String uri) {
            return uri.startsWith(route);
        }
    }

    private class EndsWithMatcher implements Matcher {
        private String route;

        private EndsWithMatcher(String route) {
            this.route = route;
        }

        public boolean match(String uri) {
            return uri.endsWith(route);
        }
    }

    private class EqualsMatcher implements Matcher {
        private String route;

        private EqualsMatcher(String route) {
            this.route = route;
        }

        public boolean match(String uri) {
            return uri.equals(route);
        }
    }

    private void setupRoutes(LinkedHashMap<String, ChannelHandler> routes) throws Exception {
        for (Map.Entry<String, ChannelHandler> m : routes.entrySet()) {
            if (m.getKey().startsWith(STARTS_WITH)) {
                this.routes.put(new StartsWithMatcher(m.getKey().replace(STARTS_WITH, "")), m.getValue());
            } else if (m.getKey().startsWith(ENDS_WITH)) {
                this.routes.put(new EndsWithMatcher(m.getKey().replace(ENDS_WITH, "")), m.getValue());
            } else if (m.getKey().startsWith(EQUALS)) {
                this.routes.put(new EqualsMatcher(m.getKey().replace(EQUALS, "")), m.getValue());
            } else {
                throw new Exception("No matcher found in route " + m.getKey());
            }
        }
    }

}
