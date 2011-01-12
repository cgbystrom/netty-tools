package random.pkg.service;
import org.apache.thrift.TException;

public class MathServImpl implements MathServ.Iface {

	@Override
	public long add(long a, long b) throws TException {
		return a+b;
	}

	@Override
	public long sub(long a, long b) throws TException {
		return a-b;
	}

}
