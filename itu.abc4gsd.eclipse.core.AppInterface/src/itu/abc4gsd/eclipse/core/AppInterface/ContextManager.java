package itu.abc4gsd.eclipse.core.AppInterface;

import org.zeromq.ZMQ;

public class ContextManager {
	private static ZMQ.Context context = ZMQ.context(1);
	public static ZMQ.Context get() { return context; }
}
