package se.cgbystrom.netty;

import org.jboss.netty.handler.stream.ChunkedWriteHandler;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class CacheTest {

    @Test
    public void maxAge() throws IOException, InterruptedException {
        final String content = "Testing the file system";
        File f = FileServerTest.createTemporaryFile(content);
        int port = FileServerTest.startServer(new CacheHandler(), new ChunkedWriteHandler(), new FileServerHandler(f.getParent(), 100));
        Thread.sleep(1000);

        assertEquals(content, FileServerTest.get("http://localhost:" + port + "/" + f.getName()));
        assertTrue(f.delete());
        assertEquals(content, FileServerTest.get("http://localhost:" + port + "/" + f.getName()));
    }

    @Test
    public void maxAgeExpire() throws IOException, InterruptedException {
        final String content = "Testing the file system";
        File f = FileServerTest.createTemporaryFile(content);
        int port = FileServerTest.startServer(new CacheHandler(), new ChunkedWriteHandler(), new FileServerHandler(f.getParent(), 1));
        Thread.sleep(1000);

        assertEquals(content, FileServerTest.get("http://localhost:" + port + "/" + f.getName()));
        Thread.sleep(2000);
        assertTrue(f.delete());
        FileServerTest.get("http://localhost:18080/" + f.getName(), 404);
    }
}
