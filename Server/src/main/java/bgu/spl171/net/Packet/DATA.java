package bgu.spl171.net.Packet;


public class DATA extends Messaging {
	public static final short packetOpcode= 3;
	public byte[] data;
	public short packetByte;
	public short blockByte;
	
	public DATA(short packetByte, short blockByte, byte[] data){
		this.packetByte = packetByte;
		this.blockByte= blockByte;
		this.data = data;
	}
	
	@Override
	public byte[] enc() {
		byte[] res = new byte[6+packetByte];
		byte[] opcode = EncoderDecoderImpl.shortToBytes(packetOpcode);
		byte[] packetByte = EncoderDecoderImpl.shortToBytes(this.packetByte);
		byte[] blockByte = EncoderDecoderImpl.shortToBytes(this.blockByte);
		res[0] = opcode[0];
		res[1] = opcode[1];
		res[2] = packetByte[0];
		res[3] = packetByte[1];
		res[4] = blockByte[0];
		res[5] = blockByte[1];
        for (int i=0;i<this.packetByte;i++){
    		res[i+6]=data[i];
		} 
		return res;	
	}
}
