package se.cgbystrom.netty.thrift;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 * Handler for Thrift RPC processors
 *
 * Requires properly decoded ChannelBuffers of raw Thrift data as input
 * Will use normal Netty ChannelBuffers for performance.
 * If an HttpRequest message is found, it will automagically extract the
 * content and use that as input for protocol decoding.
 *
 * For optimal performance, please tweak the default response size by
 * setting {@link #setResponseSize(int)}
 *
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public class ThriftHandler extends SimpleChannelUpstreamHandler {
    private TProcessor processor;
    private TProtocolFactory protocolFactory;
    private int responseSize = 4096;

    /**
     * Creates a Thrift processor handler with the default binary protocol
     * @param processor Processor to handle incoming calls
     */
    public ThriftHandler(TProcessor processor) {
        this.processor = processor;
        this.protocolFactory = new TBinaryProtocol.Factory();
    }

    /**
     * Creates a Thrift processor handler
     * @param processor Processor to handle incoming calls
     * @param protocolFactory Protocol factory to use when encoding/decoding incoming calls.
     */
    public ThriftHandler(TProcessor processor, TProtocolFactory protocolFactory) {
        this.processor = processor;
        this.protocolFactory = protocolFactory;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelBuffer input;
        HttpRequest httpRequest = null;
        if (e.getMessage() instanceof HttpRequest) {
            httpRequest = ((HttpRequest)e.getMessage());
            input = httpRequest.getContent();
        } else {
            input = (ChannelBuffer)e.getMessage();
        }
        ChannelBuffer output = ChannelBuffers.dynamicBuffer(responseSize);
        TProtocol protocol = protocolFactory.getProtocol(new TNettyChannelBuffer(input, output));

        processor.process(protocol, protocol);

        if (httpRequest != null) {
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            response.setContent(output);
            ChannelFuture future = e.getChannel().write(response);
            if (!HttpHeaders.isKeepAlive(httpRequest)) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            e.getChannel().write(output);
        }
    }

    /**
     * @return Default size for response buffer
     */
    public int getResponseSize() {
        return responseSize;
    }

    /**
     * Sets the default size for response buffer
     * @param responseSize New default size
     */
    public void setResponseSize(int responseSize) {
        this.responseSize = responseSize;
    }
}
