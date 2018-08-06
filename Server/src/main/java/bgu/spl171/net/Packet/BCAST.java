package bgu.spl171.net.Packet;

import java.io.UnsupportedEncodingException;


public class BCAST extends Messaging{

	public static final short packetOpcode=9;
	private String fileName;
	private byte delORaddI;


	public BCAST(byte delORaddI, String fileName){
		this.fileName = fileName;
		this.delORaddI = delORaddI;
	}
	@Override
	public byte[] enc() {
		byte[] res = null;
		byte[] filename;
		byte[] opcode=EncoderDecoderImpl.shortToBytes(packetOpcode);
		try {
			filename= fileName.getBytes("UTF-8");
			res = new byte[filename.length+4];
			res[0] = opcode[0];
			res[1] = opcode[1];
			res[2] = delORaddI;
			for (int i = 0; i<filename.length;i++){
				res[i+3]=filename[i];
			}
			res[res.length -1]='\0';
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return res;
	}


}
