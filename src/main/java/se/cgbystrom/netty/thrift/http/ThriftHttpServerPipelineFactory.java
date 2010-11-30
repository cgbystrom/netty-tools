package se.cgbystrom.netty.thrift.http;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import se.cgbystrom.netty.thrift.ThriftHandler;

public class ThriftHttpServerPipelineFactory implements ChannelPipelineFactory {

    private final ThriftHandler handler;

    public ThriftHttpServerPipelineFactory(ThriftHandler handler) {
        this.handler = handler;
    }

	@Override
	public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("requestDecoder", new HttpRequestDecoder());
        pipeline.addLast("responseEncoder", new HttpResponseEncoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
		pipeline.addLast("thriftHandler", (ChannelHandler)handler);
		return pipeline;
	}

}