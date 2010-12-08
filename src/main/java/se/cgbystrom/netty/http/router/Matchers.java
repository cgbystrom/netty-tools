package se.cgbystrom.netty.http.router;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Matchers {
    /*public static class StartsWithMatcher implements Matcher {
        private String route;

        public StartsWithMatcher(String route) {
            this.route = route;
        }

        public boolean match(String uri) {
            return uri.startsWith(route);
        }
    }

    public static class EndsWithMatcher implements Matcher {
        private String route;

        public EndsWithMatcher(String route) {
            this.route = route;
        }

        public boolean match(String uri) {
            return uri.endsWith(route);
        }
    }

    public static class EqualsMatcher implements Matcher {
        private String route;

        public EqualsMatcher(String route) {
            this.route = route;
        }

        public boolean match(String uri) {
            return uri.equals(route);
        }
    }*/

    public static class RegexMatcher<T> implements Matcher<T> {
        private Pattern pattern;
        //private Matcher matcher;
        private T target;
        private LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();

        public RegexMatcher(String route, T target) {
            Pattern parameter = Pattern.compile("<(.*)>");
            java.util.regex.Matcher m = parameter.matcher(route);
            while (m.find()) {
                String name = m.group(1);
                parameters.put(name, null);
                route = route.replace("<" + name + ">", "(.*)");
            }

            pattern = Pattern.compile(route);
            this.target = target;
        }

        public boolean match(String uri) {
            // Use Matcher.reset() for better perf?
            java.util.regex.Matcher m = pattern.matcher(uri);
            if (m.find() && m.groupCount() == parameters.size()) {
                int c = 1;
                for (Map.Entry<String, String> p : parameters.entrySet()) {
                    p.setValue(m.group(c));
                    c++;
                }

                return true;
            }
            return false;
        }

        public T getTarget() {
            return target;
        }

        public String getParameter(String name) {
            return parameters.get(name);
        }
    }
}
