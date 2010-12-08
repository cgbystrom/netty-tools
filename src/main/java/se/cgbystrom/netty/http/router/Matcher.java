package se.cgbystrom.netty.http.router;

public interface Matcher<T> {
    public boolean match(String uri);
    public T getTarget();
    public String getParameter(String name);
}
