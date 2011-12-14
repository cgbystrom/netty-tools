package se.cgbystrom.netty.thrift;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import io.netty.buffer.ChannelBuffer;
import io.netty.buffer.ChannelBuffers;
import io.netty.channel.Channel;

/**
 * Bridge between Netty and the client-side TTransport.
 * <p/>
 * Usage example:
 * <p/>
 * <pre>
 * {@Code
 * // create the client bootstrap
 * ChannelFactory factory = new NioClientSocketChannelFactory(Executors
 *      .newCachedThreadPool(), Executors.newCachedThreadPool(), 3);
 * ClientBootstrap bootstrap = new ClientBootstrap(factory);
 * <p/>
 * // create the netty handlers, and connect to the server
 * ThriftClientHandler handler = new ThriftClientHandler();
 * <p/>
 * // comment the following line and uncomment the next to use an Http Client. Needs an http-enabled server.
 * ChannelPipelineFactory factory = new ThriftPipelineFactory(handler);
 * // ChannelPipelineFactory factory = new ThriftHttpClientPipelineFactory(handler, "127.0.0.1", 8080);
 * <p/>
 * bootstrap.setPipelineFactory(factory);
 * Channel channel = bootstrap.connect(
 *         new InetSocketAddress(&quot;localhost&quot;, 8080)).awaitUninterruptibly()
 *         .getChannel();
 * <p/>
 * // create your thrift client and use it as normal
 * TTransport transport = new TNettyTransport(channel, handler);
 * try {
 *     TProtocol protocol = new TBinaryProtocol(transport);
 *     user-generated.thrift.Client client = new user-generated.thrift.Client(protocol);
 *     client.ping();
 * } catch (Exception exc) {
 *     exc.printStackTrace();
 * }
 * <p/>
 * // Release the netty resources
 * channel.close().awaitUninterruptibly();
 * bootstrap.releaseExternalResources();
 * }
 * </pre>
 *
 * @author Davide Inglima <limacat@gmail.com>
 */
public class TNettyTransport extends TTransport {

    private final BlockingQueue<Byte> input;

    private final Channel channel;

    private final ChannelBuffer output = ChannelBuffers.dynamicBuffer();

    public TNettyTransport(final Channel channel, final ThriftClientHandler remote) {
        this.channel = channel;
        this.input = remote.getOutputQueue();
    }

    /** Channel is managed via the Netty API */
    @Override
    public void close() {
    }

    /** Returns the status of this channel */
    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    /** Channel is managed via the Netty API. */
    @Override
    public void open() throws TTransportException {
        // the channel must be opened using Netty
    }

    /**
     * Reads from the shared input queue, blocking if there is not enough input
     * in the buffer. It's the only way we can emulate the synchronous nature of
     * TTransport using netty.
     */
    @Override
    public int read(final byte[] output, final int offset, final int length) throws TTransportException {
        int read = 0;
        int index = offset;
        int space = length - offset;
        while (space > 0) {
            byte aByte = readAsynchrously();
            output[index] = aByte;
            space--;
            index++;
            read++;
        }
        return read;
    }

    private byte readAsynchrously() {
        boolean interrupted = false;
        for (; ;) {
            try {
                byte aByte = input.take();
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
                return aByte;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
    }

    /** We need data to be written and flushed at once. */
    @Override
    public void flush() throws TTransportException {
        ChannelBuffer flush = ChannelBuffers.dynamicBuffer();
        flush.writeBytes(output);
        channel.write(flush).awaitUninterruptibly();
    }

    @Override
    public void write(byte[] array, int offset, int length) throws TTransportException {
        output.writeBytes(Arrays.copyOfRange(array, offset, length));
    }
}
