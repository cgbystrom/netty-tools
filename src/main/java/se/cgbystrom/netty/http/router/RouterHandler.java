package se.cgbystrom.netty.http.router;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpRequest;

import se.cgbystrom.netty.http.CacheHandler;
import se.cgbystrom.netty.http.SimpleResponseHandler;
import se.cgbystrom.netty.http.router.Matcher;

import java.util.LinkedHashMap;
import java.util.Map;

public class RouterHandler extends SimpleChannelUpstreamHandler {

    private Map<Matcher, ChannelHandler> routes = new LinkedHashMap<Matcher, ChannelHandler>();
    private static final String STARTS_WITH = "startsWith:";
    private static final String ENDS_WITH = "endsWith:";
    private static final String EQUALS = "equals:";
    private static final ChannelHandler HANDLER_404 = new SimpleResponseHandler("Not found", 404);
    private ChannelHandler defaultHandler;;
    
    private boolean handleNotFound;

    public RouterHandler(LinkedHashMap<String, ChannelHandler> routes, boolean handleNotFound, ChannelHandler defaultHandler) throws Exception {
        this.handleNotFound = handleNotFound;
        this.defaultHandler = defaultHandler;
        setupRoutes(routes);
    }
    
    public RouterHandler(LinkedHashMap<String, ChannelHandler> routes, boolean handleNotFound) throws Exception {
        this.handleNotFound = handleNotFound;
        this.defaultHandler = null;
        setupRoutes(routes);
    }

    public RouterHandler(LinkedHashMap<String, ChannelHandler> routes) throws Exception {
        this.handleNotFound = true;
        this.defaultHandler = null;
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
                    addOrReplaceHandler(ctx.getPipeline(), m.getValue(), "route-generated");
                    matchFound = true;
                    break;
                }
            }

            //If we found a match above, we want to _ensure_ that the default handler is NOT called
            if (matchFound) {
            	removeHandler(ctx.getPipeline(), "default-handler");
            } 
            	
            //use the default handler is specified
            if (!matchFound && defaultHandler != null) {
            	addOrReplaceHandler(ctx.getPipeline(), defaultHandler, "default-handler");
        	} else if (!matchFound && handleNotFound) {
        		//Use the not found handler if specified
        		addOrReplaceHandler(ctx.getPipeline(), HANDLER_404, "404-handler");
        	}
        }

        super.handleUpstream(ctx, e);
    }

	private void addOrReplaceHandler(ChannelPipeline pipeline, ChannelHandler channelHandler, String handleName) {
		synchronized (pipeline) {
		    if (pipeline.get(handleName) == null) {
		    	pipeline.addLast(handleName, channelHandler);
		    } else {
		    	pipeline.replace(handleName, handleName, channelHandler);
		    }
		}
	}
	
	private void removeHandler(ChannelPipeline pipeline, String handleName) {
		synchronized (pipeline) {
			if (pipeline.get(handleName) != null) {
				System.out.println("removing handle: " + handleName);
				pipeline.remove(handleName);
			}
		}
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
