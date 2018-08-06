package bgu.spl171.net.Packet;

import java.io.UnsupportedEncodingException;

public class DELRQ extends Messaging{
	
	public static final short packetOpcode= 8;
	public String Filename;
	
	public DELRQ(String Filename){
		this.Filename = Filename;
	}
	
	@Override
	public byte[] enc() {
		byte[] result = null;
		byte[] opcode = new byte[2];
		byte[] filename;
		try {
			filename= Filename.getBytes("UTF-8");
			opcode = EncoderDecoderImpl.shortToBytes(packetOpcode);
			result = new byte[filename.length + 3];
			result[0]=opcode[0];
			result[1]=opcode[1];
			for (int i = 0; i <filename.length; i++){
				result[i+2]=filename[i];
			}
			result[result.length-1]='\0';
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

}
