package se.cgbystrom.netty.http.nsgi;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

import java.nio.charset.Charset;

// Class not thread-safe. Should it be? This design assumes channel futures are called by the same I/O thread initiating the future
public class BaseNsgiHttpResponse extends DefaultHttpResponse implements ChannelFutureListener {
    Channel channel;
    private boolean pendingWrites = false;
    private boolean closeOnCompletion = false;
    private boolean headersSent = false;

    public BaseNsgiHttpResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }

    public BaseNsgiHttpResponse(Channel channel) {
        super(HttpVersion.HTTP_1_0, HttpResponseStatus.OK);
        this.channel = channel;
    }

    public BaseNsgiHttpResponse(BaseNsgiHttpResponse response) {
        this(response.channel);
    }


    public void writeContinue() {
        pendingWrites = true;

    }

    public void writeHead(int statusCode, String reasonPhrase, String[] headers) {
        pendingWrites = true;
        headersSent = true;
        this.setStatus(HttpResponseStatus.valueOf(statusCode));
        channel.write(this).addListener(this);
    }

    public void write(String chunk, Charset encoding) {
        if (!headersSent) {
            throw new RuntimeException("Headers not sent. Call writeHead() before write()");
        }
        pendingWrites = true;
        channel.write(ChannelBuffers.copiedBuffer(chunk, CharsetUtil.UTF_8)).addListener(this);
    }

    //public void addTrailers(String headers) {
    //}

    public void end() {
        if (pendingWrites) {
            closeOnCompletion = true;
        } else {
            channel.close();
        }
    }

    public void end(String data) {
        write(data, CharsetUtil.UTF_8);
        end();
    }

    public void end(String data, Charset encoding) {
        write(data, encoding);
        end();
    }

    public void operationComplete(ChannelFuture channelFuture) throws Exception {
        pendingWrites = false;
        if (closeOnCompletion) {
            channel.close();
        }
    }
}
