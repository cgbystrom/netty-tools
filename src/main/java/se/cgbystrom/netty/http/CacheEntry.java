package se.cgbystrom.netty.http;

import org.jboss.netty.handler.codec.http.HttpMessage;

public class CacheEntry {
    private HttpMessage content;
    private long expires;

    public CacheEntry(HttpMessage content, long expires) {
        this.content = content;
        this.expires = expires;
    }
    
    public HttpMessage getContent() {
		return content;
	}
    
    public long getExpires() {
		return expires;
	}
}