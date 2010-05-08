package se.cgbystrom.netty;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import static org.junit.Assert.*;

import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class FileServerTest {

    @Test
    public void fromFileSystem() throws IOException, InterruptedException {
        final String randomData = generateRandomData();
        File f = createTemporaryFile(randomData);
        new FileServer(f.getParent());
        Thread.sleep(1000);

        String url = "http://localhost:18080/" + f.getName();
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);

        assertEquals(200, client.executeMethod(method));
        assertEquals(randomData, new String(method.getResponseBody()));
    }

    @Test
    public void fromClassPath() throws IOException, InterruptedException {
        new FileServer("classpath:///");
        Thread.sleep(1000);

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod("http://localhost:18080/test.txt");

        assertEquals(200, client.executeMethod(method));
        assertEquals("Testing the class path", new String(method.getResponseBody()));
    }


    private File createTemporaryFile(String content) throws IOException {
        File f = File.createTempFile("FileServerTest", null);
        f.deleteOnExit();
        BufferedWriter out = new BufferedWriter(new FileWriter(f));
        out.write(content);
        out.close();
        return f;
    }

    private String generateRandomData() {
        byte[] data = new byte[1024 * 100];
        new Random().nextBytes(data);
        return new String(data);
    }
}
