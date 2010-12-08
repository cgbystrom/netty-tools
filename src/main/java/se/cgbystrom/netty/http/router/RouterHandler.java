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

    public RouterHandler(LinkedHashMap<String, ChannelHandler> routes) throws Exception {
        for (Map.Entry<String, ChannelHandler> m : routes.entrySet()) {
            /*if (m.getKey().startsWith(STARTS_WITH)) {
                this.routes.put(new Matchers.StartsWithMatcher(m.getKey().replace(STARTS_WITH, "")), m.getValue());
            } else if (m.getKey().startsWith(ENDS_WITH)) {
                this.routes.put(new Matchers.EndsWithMatcher(m.getKey().replace(ENDS_WITH, "")), m.getValue());
            } else if (m.getKey().startsWith(EQUALS)) {
                this.routes.put(new Matchers.EqualsMatcher(m.getKey().replace(EQUALS, "")), m.getValue());
            } else {
                throw new Exception("No matcher found in route " + m.getKey());
            }*/
        }
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

            if (!matchFound) {
                ctx.getPipeline().addLast("404-handler", HANDLER_404);
            }
        }

        super.handleUpstream(ctx, e);
    }

    
}
