package se.cgbystrom.netty.http.nsgi;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class NsgiHandler extends SimpleChannelUpstreamHandler implements NsgiCallable {
    LinkedList<NsgiCallable> layers = new LinkedList<NsgiCallable>();
    NsgiCallable nextToCall;

    public NsgiHandler(LinkedList<NsgiCallable> layers) {
        this.layers = layers;
        //nextToCall = layers.getFirst();
    }

    public NsgiHandler(NsgiCallable... layers) {
        this(new LinkedList<NsgiCallable>(Arrays.asList(layers)));
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        BaseNsgiHttpResponse response = new BaseNsgiHttpResponse(e.getChannel());
        //nextToCall.call(null, request, response, this);
        call(null, request, response, null);
    }



    public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.

        // Naive impl of next in layer to call
        NsgiCallable prevCall = nextToCall;
        nextToCall = null;
        if (prevCall == null) {
            nextToCall = layers.getFirst();
        } else {
            for (int i = 0; i < layers.size(); i++) {
                if (layers.get(i) == prevCall && i != layers.size() - 1) {
                    nextToCall = layers.get(i + 1);
                }
            }
        }

        if (nextToCall != null) {
            try {
                nextToCall.call(error, request, response, this);
            } catch (Exception e) {
                // TODO: Nested exceptions?
                call(e, request, response, null);
            }
        } else if (error != null) {
            response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
            response.writeHead(500, "Internal Server Error", null);
            response.end("Internal Server Error");
        }

    }
}
