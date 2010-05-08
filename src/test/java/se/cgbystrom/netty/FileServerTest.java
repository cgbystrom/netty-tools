package se.cgbystrom.netty;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileServerTest {

    @Test
    public void fromFileSystem() throws IOException, InterruptedException {
        final String content = "Testing the file system";
        File f = createTemporaryFile(content);
        new TestServer(new FileServerHandler(f.getParent()));
        Thread.sleep(1000);

        assertEquals(content, get("http://localhost:18080/" + f.getName()));
    }

    @Test
    public void fromClassPath() throws IOException, InterruptedException {
        new TestServer(new FileServerHandler("classpath:///"));
        Thread.sleep(1000);

        assertEquals("Testing the class path", get("http://localhost:18080/test.txt"));
    }

    public static File createTemporaryFile(String content) throws IOException {
        File f = File.createTempFile("FileServerTest", null);
        f.deleteOnExit();
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        out.write(content);
        out.close();
        return f;
    }

    public static String get(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);

        assertEquals(200, client.executeMethod(method));
        return new String(method.getResponseBody());
    }
}
