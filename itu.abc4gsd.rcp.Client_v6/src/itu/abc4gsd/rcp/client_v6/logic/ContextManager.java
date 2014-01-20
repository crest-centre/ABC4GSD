package itu.abc4gsd.rcp.client_v6.logic;

import org.zeromq.ZMQ;

public class ContextManager {
	private static ZMQ.Context context = ZMQ.context(1);
	public static ZMQ.Context get() { return context; }
}
