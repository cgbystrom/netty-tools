package se.cgbystrom.netty;

import org.junit.Before;
import org.junit.Test;
import se.cgbystrom.netty.http.router.Matcher;
import se.cgbystrom.netty.http.router.Router;

import static org.junit.Assert.*;

public class RouterTest {
    private static class Dummy {
        public String message;

        private Dummy(String message) {
            this.message = message;
        }
    }

    public Dummy helloWorld;
    public Dummy cookies;
    public Dummy bacon;
    public Router<Dummy> router;

    @Before
    public void setUp() {
        helloWorld = new Dummy("Hello World");
        cookies = new Dummy("Cookies");
        bacon = new Dummy("Bacon is good for me");
        router = new Router<Dummy>();
    }

    @Test
    public void rootPath() throws Exception {
        router.addRoute("/", helloWorld);
        assertEquals(helloWorld.message, router.route("/").getTarget().message);
    }

    @Test
    public void rootNonePath() throws Exception {
        router.addRoute("/", helloWorld);
        assertEquals(helloWorld.message, router.route("").getTarget().message);
    }


    @Test
    public void basicPath() throws Exception {
        router.addRoute("/api/basic1", helloWorld);
        assertEquals(helloWorld.message, router.route("/api/basic1").getTarget().message);
    }

    @Test
    public void basicPathTrailing() throws Exception {
        router.addRoute("/api/basic1", helloWorld);
        assertEquals(helloWorld.message, router.route("/api/basic1/").getTarget().message);
    }

    @Test
    public void tooShort() throws Exception {
        router.addRoute("/api/basic1", helloWorld);
        assertNull(router.route("/api/bas"));
    }

    @Test
    public void tooLong() throws Exception {
        router.addRoute("/api/basic1", helloWorld);
        assertNull(router.route("/api/basic1long"));
    }

    @Test
    public void parameter() throws Exception {
        router.addRoute("/api/users/<username>", helloWorld);
        final Matcher<Dummy> m = router.route("/api/users/charlie");
        assertEquals(helloWorld.message, m.getTarget().message);
        assertEquals("charlie", m.getParameter("username"));
    }

    @Test
    public void multipleParameters() throws Exception {
        router.addRoute("/api/users/<username>/channels/<channel>", helloWorld);
        final Matcher<Dummy> m = router.route("/api/users/charlie/channels/news");
        assertEquals(helloWorld.message, m.getTarget().message);
        assertEquals("charlie", m.getParameter("username"));
        assertEquals("news", m.getParameter("channel"));
    }

    // Bad parameter names
    // Multiple routes in same
    // Complex routes
}
