package se.cgbystrom.netty.thrift;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.netty.buffer.ChannelBuffer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.MessageEvent;
import io.netty.channel.SimpleChannelHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;

/**
 * A client-side ChannelHandler for the Thrift protocol.
 * Once you create the channelHandler, you must offer back it to the TNettyTransport.
 *
 * @author Davide Inglima <limacat@gmail.com>
 */
public class ThriftClientHandler extends SimpleChannelHandler implements ThriftHandler {

    private final BlockingQueue<Byte> peer = new LinkedBlockingQueue<Byte>();

    BlockingQueue<Byte> getOutputQueue() {
        return peer;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object message = e.getMessage();
        ChannelBuffer cb;
        if (message instanceof DefaultHttpResponse) {
            cb = (ChannelBuffer) ((DefaultHttpResponse) message).getContent();
        } else {
            cb = (ChannelBuffer) message;
        }
        while (cb.readableBytes() > 0) {
            peer.offer(cb.readByte());
        }
    }
}
