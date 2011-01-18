package se.cgbystrom.netty.http.nsgi;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class NsgiHandler extends SimpleChannelUpstreamHandler implements NsgiCallable {
    LinkedList<NsgiCallable> layers = new LinkedList<NsgiCallable>();
    NsgiCallable current;

    public NsgiHandler(LinkedList<NsgiCallable> layers) {
        this.layers = layers;
        current = layers.getFirst();
    }

    public NsgiHandler(NsgiCallable... layers) {
        this(new LinkedList<NsgiCallable>(Arrays.asList(layers)));
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        BaseNsgiHttpResponse response = new BaseNsgiHttpResponse(e.getChannel());
        current.call(null, request, response, this);
    }



    public void call(Throwable error, HttpRequest request, BaseNsgiHttpResponse response, NsgiCallable next) {
        //To change body of implemented methods use File | Settings | File Templates.

        // Naive impl of next in layer to call
        NsgiCallable nextToCall = null;
        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i) == current && i != layers.size() - 1) {
                nextToCall = layers.get(i + 1);
            }
        }

        if (nextToCall != null) {
            nextToCall.call(null, request, response, this);
        }

    }
}
