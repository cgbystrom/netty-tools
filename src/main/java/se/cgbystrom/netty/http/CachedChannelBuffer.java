package se.cgbystrom.netty.http;

import org.jboss.netty.buffer.ChannelBuffer;

public class CachedChannelBuffer {
	private ChannelBuffer channelBuffer;
	private long expires;
	
	public CachedChannelBuffer(ChannelBuffer channelBuffer, long expires) {
		this.channelBuffer = channelBuffer;
		this.expires = expires;
	}
	
	public ChannelBuffer getChannelBuffer() {
		return channelBuffer;
	}
	
	public long getExpires() {
		return expires;
	}
}
