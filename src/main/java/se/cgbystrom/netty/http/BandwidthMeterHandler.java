package se.cgbystrom.netty.http;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class BandwidthMeterHandler extends SimpleChannelHandler {
    private static final int RESOLUTION = 1000;

    private AtomicLong bytesSent = new AtomicLong();
    private AtomicLong bytesReceived = new AtomicLong();
    private AtomicLong bytesSentPerSecond = new AtomicLong();
    private AtomicLong bytesReceivedPerSecond = new AtomicLong();

    /**
     * Collect a 60-second, moving average of the bytes sent/received per second
     */
    private class CollectBandwidthStats implements TimerTask {
        private long lastTimestamp = 0;
        private long lastBytesSent = bytesSent.get();
        private long lastBytesReceived = bytesReceived.get();
        private ArrayBlockingQueue<Long> sentHistory = new ArrayBlockingQueue<Long>(60); // Keep a 60 second history
        private ArrayBlockingQueue<Long> receivedHistory = new ArrayBlockingQueue<Long>(60); // Keep a 60 second history

        public void run(Timeout timeout) throws Exception {
            if (timeout.isCancelled())
                return;

            final long timestamp = System.currentTimeMillis();
            final long deltaTime = timestamp - lastTimestamp;
            long sent = bytesSent.get() - lastBytesSent;
            long received = bytesReceived.get() - lastBytesReceived;
            lastBytesSent = bytesSent.get();
            lastBytesReceived = bytesReceived.get();

            if (sent < 0)
                sent = 0;

            if (received < 0)
                received = 0;

            sentHistory.add(sent / deltaTime);
            receivedHistory.add(received / deltaTime);
            bytesSentPerSecond.set(average(sentHistory) * 1000);
            bytesReceivedPerSecond.set(average(receivedHistory) * 1000);

            lastTimestamp = timestamp;
            timeout.getTimer().newTimeout(timeout.getTask(), RESOLUTION, TimeUnit.MILLISECONDS);
        }

        private Long average(Collection<Long> values) {
            if (values.isEmpty())
                return 0L;

            long total = 0;
            for (Long messageTiming : values)
                total += messageTiming;

            return total / values.size();
        }
    }

    /**
     * Constructs a new instance without time based statistics.
     * {@link #getBytesSentPerSecond()} and {@link #getBytesReceivedPerSecond()} will not work.
     * For these statistics, instantiate with {@link #BandwidthMeterHandler(org.jboss.netty.util.Timer)} instead.
     */
    public BandwidthMeterHandler() {
    }

    /**
     * Constructs a new instance with time based statistics.
     * Uses a 60-second, moving average of the bytes sent/received per second.
     * @param timer Timer to schedule the collection of bandwidth statistics over time
     */
    public BandwidthMeterHandler(Timer timer) {
        timer.newTimeout(new CollectBandwidthStats(), 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof MessageEvent && ((MessageEvent)e).getMessage() instanceof ChannelBuffer) {
            ChannelBuffer b = (ChannelBuffer)((MessageEvent)e).getMessage();
            bytesReceived.addAndGet(b.readableBytes());
        }

        super.handleUpstream(ctx, e);
     }

    @Override
    public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof MessageEvent && ((MessageEvent)e).getMessage() instanceof ChannelBuffer) {
            ChannelBuffer b = (ChannelBuffer)((MessageEvent)e).getMessage();
            bytesSent.addAndGet(b.readableBytes());
        }

        super.handleDownstream(ctx, e);
    }

    public void reset() {
        bytesSent.set(0);
        bytesReceived.set(0);
    }

    public long getBytesSent() {
        return bytesSent.get();
    }

    public long getBytesReceived() {
        return bytesReceived.get();
    }

    public long getBytesSentPerSecond() {
        return bytesSentPerSecond.get();
    }

    public long getBytesReceivedPerSecond() {
        return bytesReceivedPerSecond.get();
    }
}
