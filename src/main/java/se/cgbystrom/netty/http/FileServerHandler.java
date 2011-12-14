package se.cgbystrom.netty.http;

import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import io.netty.buffer.ChannelBuffers;
import io.netty.buffer.ChannelBuffer;
import io.netty.channel.*;
import io.netty.handler.codec.frame.TooLongFrameException;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.*;

/**
 * A file server that can serve files from file system and class path.
 *
 * If you wish to customize the error message, please sub-class and override sendError().
 * Based on Trustin Lee's original file serving example
 */
public class FileServerHandler extends SimpleChannelUpstreamHandler {
    private String rootPath;
    private String stripFromUri;
    private int cacheMaxAge = -1;
    private boolean fromClasspath = false;
    private MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

    public FileServerHandler(String path) {
        if (path.startsWith("classpath://")) {
            fromClasspath = true;
            //rootPath = getClass().getResource(path.replace("classpath://", "")).getPath();
            rootPath = path.replace("classpath://", "");
            if (rootPath.lastIndexOf("/") == rootPath.length() -1)
                rootPath = rootPath.substring(0, rootPath.length() -1);
        } else {
            rootPath = path;
        }
        rootPath = rootPath.replace(File.separatorChar, '/');
    }

    public FileServerHandler(String path, String stripFromUri) {
        this(path);
        this.stripFromUri = stripFromUri;
    }

    public FileServerHandler(String path, int cacheMaxAge) {
        this(path);
        this.cacheMaxAge = cacheMaxAge;
    }

    public FileServerHandler(String path, int cacheMaxAge, String stripFromUri) {
        this(path, cacheMaxAge);
        this.stripFromUri = stripFromUri;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();
        if (request.getMethod() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        String uri = request.getUri();
        if (stripFromUri != null) {
            uri = uri.replaceFirst(stripFromUri, "");
        }

        final String path = sanitizeUri(uri);
        if (path == null) {
            sendError(ctx, FORBIDDEN);
            return;
        }


        ChannelBuffer content = getFileContent(path);
        if (content == null) {
            sendError(ctx, NOT_FOUND);
            return;
        }

        String contentType = fileTypeMap.getContentType(path);

        CachableHttpResponse response = new CachableHttpResponse(HTTP_1_1, OK);
        response.setRequestUri(request.getUri());
        response.setCacheMaxAge(cacheMaxAge);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
        setContentLength(response, content.readableBytes());

        response.setContent(content);
        ChannelFuture writeFuture = e.getChannel().write(response);

        // Decide whether to close the connection or not.
        if (!isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private ChannelBuffer getFileContent(String path) {
        InputStream is;
        try {
            if (fromClasspath) {
                is = this.getClass().getResourceAsStream(rootPath + path);
            } else {
                is = new FileInputStream(rootPath + path);
            }

            if (is == null) {
                return null;
            }
            
            final int maxSize = 512 * 1024;
            ByteArrayOutputStream out = new ByteArrayOutputStream(maxSize);
            byte[] bytes = new byte[maxSize];

            while (true) {
                int r = is.read(bytes);
                if (r == -1) break;

                out.write(bytes, 0, r);
            }

            ChannelBuffer cb = ChannelBuffers.copiedBuffer(out.toByteArray());
            out.close();
            is.close();
            return cb;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        cause.printStackTrace();
        if (ch.isConnected()) {
            sendError(ctx, INTERNAL_SERVER_ERROR);
        }
    }

    private String sanitizeUri(String uri) throws URISyntaxException {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }

        // Convert file separators.
        uri = uri.replace(File.separatorChar, '/');

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + ".") ||
            uri.contains("." + File.separator) ||
            uri.startsWith(".") || uri.endsWith(".")) {
            return null;
        }

        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        uri = decoder.getPath();

        if (uri.endsWith("/")) {
            uri += "index.html";
        }

        return uri;
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer(
                "Failure: " + status.toString() + "\r\n",
                CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }
}
