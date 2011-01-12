package random.pkg.client;
import static org.testng.Assert.assertEquals;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import random.pkg.service.MathServ;

public class SimpleHttpClient {
	TTransport transport;
	MathServ.Client client;
	
	@BeforeTest
	private void setup() {
		try {
			transport = new THttpClient("http://localhost:9981");
			TProtocol protocol = new TBinaryProtocol(transport);
			client = new MathServ.Client(protocol);
			transport.open();
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}
	}

	@AfterTest
	void tearDown()
	{
		transport.close();
	}
	
	@Test
	public void a() throws TException {
		long x = 2239, y =21;
		long a = client.add(x ,y);
		assertEquals(a, x+y);
	}
}