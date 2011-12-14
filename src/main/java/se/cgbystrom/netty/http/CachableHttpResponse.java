package se.cgbystrom.netty.http;

import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class CachableHttpResponse extends DefaultHttpResponse {
    private String requestUri;
    private int cacheMaxAge;

    public CachableHttpResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public int getCacheMaxAge() {
        return cacheMaxAge;
    }

    public void setCacheMaxAge(int cacheMaxAge) {
        this.cacheMaxAge = cacheMaxAge;
    }
}
