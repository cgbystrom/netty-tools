package se.cgbystrom.netty.thrift.http;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;

import se.cgbystrom.netty.http.HttpEncoder;
import se.cgbystrom.netty.thrift.ThriftHandler;

public class ThriftHttpClientPipelineFactory implements ChannelPipelineFactory {

    private final ThriftHandler handler;
    
    private final String host;
    
    private final String uri;

    public ThriftHttpClientPipelineFactory(ThriftHandler handler, String host, String uri) {
        this.handler = handler;
        this.host = host;
        this.uri = uri;
    }

	@Override
	public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("requestEncoder", new HttpRequestEncoder());
        pipeline.addLast("channelEncoder", new HttpEncoder(host, uri));
        pipeline.addLast("responseDecoder", new HttpResponseDecoder());
		pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
		pipeline.addLast("thriftHandler", (ChannelHandler)handler);
		return pipeline;
	}
}
