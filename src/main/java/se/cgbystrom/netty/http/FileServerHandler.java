package se.cgbystrom.netty.http;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.*;

/**
 * A file server that can serve files from file system and class path.
 *
 * If you wish to customize the error message, please sub-class and override sendError().
 * Based on Trustin Lee's original file serving example
 */
public class FileServerHandler extends SimpleChannelUpstreamHandler
{
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


        FileContentInfo contentInfo = getFileContent(path);
        if (contentInfo == null) {
            sendError(ctx, NOT_FOUND);
            return;
        }

        String contentType = fileTypeMap.getContentType(path);

        CachableHttpResponse response = new CachableHttpResponse(HTTP_1_1, OK);
        response.setRequestUri(request.getUri());
        response.setCacheMaxAge(cacheMaxAge);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
        setContentLength(response, contentInfo.content.readableBytes());

        response.setBackingFileChannel(contentInfo.fileChannel);
        response.setContent(contentInfo.content);
        ChannelFuture writeFuture = e.getChannel().write(response);

        // Decide whether to close the connection or not.
        if (!isKeepAlive(request)) {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private class FileContentInfo
    {
      public ChannelBuffer content;
      public FileChannel fileChannel;
      public FileContentInfo(FileChannel fileChannel, ChannelBuffer content)
      {
        this.fileChannel = fileChannel;
        this.content = content;
      }
    }

    private FileContentInfo getFileContent(String path) {
        FileChannel fc = null;
        FileContentInfo result = null;

        try {
            File file;

            if (fromClasspath) {
                file = new File(this.getClass().getResource(rootPath + path).getFile());
            } else {
                file = new File(rootPath + path);
            }

            fc = new RandomAccessFile(file, "r").getChannel();
            ByteBuffer roBuf = fc.map(FileChannel.MapMode.READ_ONLY, 0, (int)fc.size());
            result = new FileContentInfo(fc, ChannelBuffers.wrappedBuffer(roBuf));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (result == null)
            {
                if (fc != null)
                {
                    try
                    {
                        fc.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        return result;
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
