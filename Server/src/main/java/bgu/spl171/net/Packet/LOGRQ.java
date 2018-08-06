package bgu.spl171.net.Packet;

import java.io.UnsupportedEncodingException;

public class LOGRQ extends Messaging{
	public static final short packetOpcode = 7;
	public String userName;

	
	public LOGRQ(String userName){
		this.userName = userName;
	}
	
	@Override
	public byte[] enc() {
		byte[] opcode= new byte[2];
		byte[] username;
		byte[] result = null;
		try {
			username= userName.getBytes("UTF-8");
			opcode= EncoderDecoderImpl.shortToBytes(packetOpcode);
			result = new byte[username.length + 3];
			result[0]=opcode[0];
			result[1]=opcode[1];
			for (int i=0;i<username.length;i++){
				result[i+2]=username[i];
			}
			result[result.length -1]='\0';
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
}
