package se.cgbystrom.netty.thrift.http;

import io.netty.buffer.ChannelBuffer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * Client-side Http Encoder.
 *
 * @author Davide Inglima <limacat@gmail.com>
 */
public class HttpEncoder extends OneToOneEncoder {
	
	private final String host;
	
	private final String uri;
	
	public HttpEncoder(String host, String uri) {
		this.host = host;
		this.uri = uri;
	}

	@Override
	protected Object encode(ChannelHandlerContext context, Channel channel,	Object message) throws Exception {
		ChannelBuffer content = (ChannelBuffer) message;
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri);
		request.setHeader(HttpHeaders.Names.HOST, host);
		request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
		request.setContent(content);
		return request;
	}

}