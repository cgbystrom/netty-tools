package se.cgbystrom.netty.thrift.http;

import static io.netty.channel.Channels.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPipelineFactory;
import io.netty.handler.codec.http.HttpChunkAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;

import se.cgbystrom.netty.thrift.ThriftHandler;

/**
 * Pipeline factory for Thrift Http client
 *
 * @author Davide Inglima <limacat@gmail.com>
 */
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
