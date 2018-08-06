package bgu.spl171.net.Packet;

public class DISC extends Messaging{
	public static final short packetOpcode = 10;
	@Override
	
	public byte[] enc() {
		return EncoderDecoderImpl.shortToBytes(packetOpcode);
	}

}
