package se.cgbystrom.netty.http;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

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