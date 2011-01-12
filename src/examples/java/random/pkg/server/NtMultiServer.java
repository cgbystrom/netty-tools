package random.pkg.server;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import random.pkg.service.MathServ;
import random.pkg.service.MathServImpl;
import se.cgbystrom.netty.thrift.ThriftPipelineFactory;
import se.cgbystrom.netty.thrift.ThriftServerHandler;
import se.cgbystrom.netty.thrift.http.ThriftHttpServerPipelineFactory;


public class NtMultiServer {
	ThriftServerHandler x = null;
	
	private synchronized ThriftServerHandler getTHandler()
	{
		if( x == null)
		{
			x = new ThriftServerHandler(new MathServ.Processor( new MathServImpl()));
		}
		return x;
	}
	
	private void framedSocketServer() {
		ChannelPipelineFactory factory = new ThriftPipelineFactory(getTHandler());
		ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool(),
                        1));

        bootstrap.setPipelineFactory(factory);

        bootstrap.bind(new InetSocketAddress(7912));		
	}

	private void httpServer() {
		ChannelPipelineFactory factory = new ThriftHttpServerPipelineFactory(getTHandler());
		ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool(),
                        1));

        bootstrap.setPipelineFactory(factory);

        bootstrap.bind(new InetSocketAddress(9981));		
	}

	public static void main(String args[]) {
		NtMultiServer srv = new NtMultiServer();
		
		srv.framedSocketServer();
		srv.httpServer();
	}
}
