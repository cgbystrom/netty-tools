package se.cgbystrom.netty.http.router;

public interface Matcher {
    public boolean match(String uri);
}
