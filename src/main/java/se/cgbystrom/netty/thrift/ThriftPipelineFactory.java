package se.cgbystrom.netty.thrift;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPipelineFactory;
import io.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.frame.LengthFieldPrepender;

import static io.netty.channel.Channels.pipeline;

/**
 * Pipeline factory for Thrift servers and clients
 *
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public class ThriftPipelineFactory implements ChannelPipelineFactory {
    private ThriftHandler handler;
    private int maxFrameSize = 512 * 1024;

    public ThriftPipelineFactory(ThriftHandler handler) {
        this.handler = handler;
    }

    public ThriftPipelineFactory(ThriftHandler handler, int maxFrameSize)	{
        this(handler);
        this.maxFrameSize = maxFrameSize;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(maxFrameSize, 0, 4, 0, 4));
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        pipeline.addLast("thriftHandler", (ChannelHandler)handler);
        return pipeline;
    }
}
