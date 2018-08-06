package bgu.spl171.net.impl.rci;

import bgu.spl171.net.api.MessageEncoderDecoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class ObjectEncoderDecoder implements MessageEncoderDecoder<Serializable> {

	private final ByteBuffer len = ByteBuffer.allocate(4);
	private byte[] bytesArr = null;
	private int bytesPlace = 0;


	@Override
	public byte[] encode(Serializable message) {
		return serializeObject(message);
	}

	@Override
	public Serializable decodeNextByte(byte nextByte) {
		if (bytesArr == null) { 
			len.put(nextByte);
			if (!len.hasRemaining()) { 
				len.flip();
				bytesArr = new byte[len.getInt()];
				bytesPlace = 0;
				len.clear();
			}
		} else {
			bytesArr[bytesPlace] = nextByte;
			if (++bytesPlace == bytesArr.length) {
				Serializable result = deserializeObject();
				bytesArr = null;
				return result;
			}
		}
		return null;
	}

	private byte[] serializeObject(Serializable message) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			for (int i = 0; i < 4; i++) {
				bytes.write(0);
			}
			ObjectOutput out = new ObjectOutputStream(bytes);
			out.writeObject(message);
			out.flush();
			byte[] result = bytes.toByteArray();
			ByteBuffer.wrap(result).putInt(result.length - 4);
			return result;
		} catch (Exception ex) {
			throw new IllegalArgumentException("cannot serialize the object", ex);
		}
	}

	private Serializable deserializeObject() {
		try {
			ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(bytesArr));
			return (Serializable) in.readObject();
		} catch (Exception ex) {
			throw new IllegalArgumentException("cannot desrialize the object", ex);
		}
	}
}
