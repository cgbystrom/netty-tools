package se.cgbystrom.netty.thrift;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

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
        ChannelBuffer cb = (ChannelBuffer)e.getMessage();
        while (cb.readableBytes() > 0) {
            peer.offer(cb.readByte());
        }
    }
}
