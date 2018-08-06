package bgu.spl171.net.api.bidi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl171.net.srv.bidi.ConnectionHandler;

public class ConnectionsImpl<T>  implements Connections<T> {
	private ConcurrentHashMap<Integer,ConnectionHandler<T>> connectedUsers = new ConcurrentHashMap<Integer,ConnectionHandler<T>>();
	private AtomicInteger connectedNum= new AtomicInteger(0);
	
	

	public boolean send(int connectionId, T msg) {
		if(connectedUsers.get(connectionId) != null){
			connectedUsers.get(connectionId).send(msg);
			return true;
		}
		return false;
	}

	public int add(ConnectionHandler<T> ch){
		connectedUsers.put(connectedNum.incrementAndGet(), ch);
		return connectedNum.get();
	}
	
	public void broadcast(T msg) {
		for (ConnectionHandler<T> value : connectedUsers.values()) {
			value.send(msg);
		}
	}

	public void disconnect(int connectionId) {
		connectedUsers.remove(connectionId);
	}
	
	
}
