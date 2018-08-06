package bgu.spl171.net.Packet;

import java.io.UnsupportedEncodingException;

public class WRQ extends Messaging{
	public static final short packetOpcode = 2;
	public String fileName;

	public WRQ(String fileName){
		this.fileName = fileName;
	}
	@Override
	public byte[] enc() {
		byte[] opcode= new byte[2];
		byte[] filename;
		byte[] result = null;
		try {
			filename= fileName.getBytes("UTF-8");
			opcode=EncoderDecoderImpl.shortToBytes(packetOpcode);
			result = new byte[filename.length + 3];
			result[0]=opcode[0];
			result[1]=opcode[1];
			for (int i=0;i<filename.length;i++){
				result[i+2]=filename[i];
			}
			result[result.length-1]='\0';
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
