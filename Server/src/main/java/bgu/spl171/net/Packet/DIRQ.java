package bgu.spl171.net.Packet;

public class DIRQ extends Messaging{
	public static final short packetOpcode = 6;
	
	@Override
	public byte[] enc() {
		return EncoderDecoderImpl.shortToBytes(packetOpcode);
	}
}
