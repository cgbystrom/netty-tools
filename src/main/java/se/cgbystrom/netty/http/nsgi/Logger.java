package se.cgbystrom.netty.http.nsgi;

import org.jboss.netty.handler.codec.http.HttpRequest;

public class Logger implements NsgiCallable {
    public void call(final HttpRequest request, final BaseNsgiHttpResponse response, NsgiCallable next) {
        // Overload writeHead() to snag status code and headers
        // Overload end to write log output and response time

        next.call(request, new BaseNsgiHttpResponse(response) {
            int statusCode;
            boolean logged = false;

            @Override
            public void writeHead(int statusCode, String reasonPhrase, String[] headers) {
                if (!logged) {
                    this.statusCode = statusCode;
                }
                super.writeHead(statusCode, reasonPhrase, headers);
            }

            @Override
            public void end() {
                super.end();
                System.out.println("Logged " + request.getUri() + " " + statusCode);
            }

        }, null);
    }
}
