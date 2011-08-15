package se.cgbystrom.netty.http;

import java.io.IOException;
import java.nio.channels.FileChannel;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class CachableHttpResponse extends DefaultHttpResponse {
    private String requestUri;
    private int cacheMaxAge;
    private FileChannel fileChannel;

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

    public void setBackingFileChannel(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
    }

    public FileChannel getFileChannel()
    {
        return fileChannel;
    }

    public void dispose()
    {
        if (fileChannel != null)
        {
            try
            {
                fileChannel.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
