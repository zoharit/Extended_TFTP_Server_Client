package bgu.spl171.net.Packet;

public class ACK extends Messaging{

	public static final short packetOpcode = 4;
	public short block;
	
	public ACK(short block){
		this.block = block;
	}
	
	@Override
	public byte[] enc() {
		byte[] res = new byte[4];		
		byte[] opcode=EncoderDecoderImpl.shortToBytes(packetOpcode);
		byte[] arrBlock = EncoderDecoderImpl.shortToBytes(block);
		for(int i = 0; i < opcode.length; i++){
			res[i] = opcode[i];
		}
		for(int i=0;i<arrBlock.length;i++){
			res[i+opcode.length]=arrBlock[i];
		}
		return res;	
	}
}
