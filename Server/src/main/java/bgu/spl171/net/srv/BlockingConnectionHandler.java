package bgu.spl171.net.srv;

import bgu.spl171.net.Packet.Messaging;
import bgu.spl171.net.api.MessageEncoderDecoder;
import bgu.spl171.net.api.bidi.BidiMessagingProtocol;
import bgu.spl171.net.api.bidi.Connections;
import bgu.spl171.net.api.bidi.ConnectionsImpl;
import bgu.spl171.net.api.bidi.TFTP;
import bgu.spl171.net.srv.bidi.ConnectionHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

	private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private final ConnectionsImpl<T> connections;
    private final int id;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol, ConnectionsImpl<T>connections) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.connections = connections;
        this.id = connections.add(this);
    }

    @Override
    public void run() {
    	try (Socket sock = this.sock) { //just for automatic closing
            int read;
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            protocol.start(id, connections);
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                	protocol.process(nextMessage);             
                }
            }
            close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
    	connections.disconnect(id);
    	connected = false;
        sock.close();
        
    }

	@Override
	public void send(T msg) {
		if (msg != null) {
			try{
				out.write(encdec.encode(msg));
				out.flush();
			}
			catch (IOException e){
				e.printStackTrace();
			}
        }
		
	}
}
