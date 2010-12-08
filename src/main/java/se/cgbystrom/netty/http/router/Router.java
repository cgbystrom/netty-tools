package se.cgbystrom.netty.http.router;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class Router<T> {
    LinkedList<Matcher> routes = new LinkedList<Matcher>();

    public void addRoute(String path, T target) {
        final Matchers.RegexMatcher<T> a = new Matchers.RegexMatcher<T>("^" + path + "$", target);

        routes.addLast(a);
    }

    public T getIt() {
        return null;
    }

    public Matcher<T> route(String path) {
        boolean hasTrailingSlash = path.lastIndexOf('/') == path.length() - 1 && path.length() > 2;
        if (hasTrailingSlash)
            path = path.substring(0, path.length() - 1);

        if (path.length() == 0)
            path = "/";

        for (Matcher route : routes) {
            if (route.match(path)) {
                return route;
            }
        }
        
        return null;
    }
}
