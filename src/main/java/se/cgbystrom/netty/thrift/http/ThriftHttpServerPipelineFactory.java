package se.cgbystrom.netty.thrift.http;

import static io.netty.channel.Channels.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPipelineFactory;
import io.netty.handler.codec.http.HttpChunkAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import se.cgbystrom.netty.thrift.ThriftHandler;

/**
 * Pipeline factory for Thrift Http client
 *
 * @author Davide Inglima <limacat@gmail.com>
 */
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
        pipeline.addLast("thriftHandler", (ChannelHandler) handler);
        return pipeline;
    }

}
