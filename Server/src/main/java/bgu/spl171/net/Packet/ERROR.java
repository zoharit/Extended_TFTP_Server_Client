package bgu.spl171.net.Packet;

import java.io.UnsupportedEncodingException;

public class ERROR extends Messaging{

	public static final short packetOpcode=5;
	public short errcode;
	public String errMsg;
	public static final String[] MSG = {
			"Not defined, see error message (if any)",
			"File not found – RRQ or DELRQ of non-existing file",
			"Access violation – File cannot be written, read or deleted.",
			"Disk full or allocation exceeded – No room in disk.",
			"Illegal TFTP operation – Unknown Opcode.",
			"File already exists – File name exists on WRQ.",
			"User not logged in – Any opcode received before Login completes.",
			"User already logged in – Login username already connected."};

	public ERROR(short errcode,String errMsg){
		this.errMsg = errMsg;
		this.errcode = errcode;
	}
	
	@Override
	public byte[] enc() {
		byte[] res = null;
		byte[] opcode= EncoderDecoderImpl.shortToBytes(packetOpcode);
		byte[] errcode = EncoderDecoderImpl.shortToBytes(this.errcode);
		byte[] errmsg;
		try {
			errmsg= errMsg.getBytes("UTF-8");
			res = new byte[errmsg.length + 5];
			res[0]=opcode[0];
			res[1]=opcode[1];
			res[2]=errcode[0];
			res[3]=errcode[1];
			for (int i = 0; i <errmsg.length; i++){
				res[i+4] =errmsg[i];
			}
			res[res.length-1]='\0';

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return res;
	}
}
