package se.cgbystrom.netty.thrift;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;

import static org.jboss.netty.channel.Channels.pipeline;

public class ThriftPipelineFactory implements ChannelPipelineFactory {
    private ThriftServerHandler handler;
    private int maxFrameSize = 512 * 1024;

    public ThriftPipelineFactory(ThriftServerHandler handler) {
        this.handler = handler;
    }

    public ThriftPipelineFactory(ThriftServerHandler handler, int maxFrameSize)	{
        this(handler);
        this.maxFrameSize = maxFrameSize;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(maxFrameSize, 0, 4, 0, 4));
        pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
        pipeline.addLast("thrift-handler", handler);
        return pipeline;
    }
}
