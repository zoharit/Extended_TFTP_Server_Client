package bgu.spl171.net.Packet;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bgu.spl171.net.api.MessageEncoderDecoder;

public class EncoderDecoderImpl implements MessageEncoderDecoder<Messaging> {

	private List<Byte> bytes = new ArrayList<Byte>();
	private short opCode=-1;
	private short packetSize;
	private short countdata=0;
	private int count = 0;

	@Override
	public Messaging decodeNextByte(byte nextByte) {
		if (count == 0) { 
			this.bytes.add(nextByte);
			this.count++;
		} 
		else if(count == 1){
			this.bytes.add(nextByte);
			this.count++;
			Byte[] byteop = bytes.toArray(new Byte[bytes.size()]);
			this.opCode = bytesToShort(byteop);
			this.bytes.clear();
			Messaging msg = null;
			if(this.opCode == DIRQ.packetOpcode){
				msg = new DIRQ();
				bytes.clear();
				count = 0;
				return msg;
			}
			else if(this.opCode == DISC.packetOpcode){
				msg = new DISC();
				bytes.clear();
				count = 0;
				return msg;
			}
		}
		else {
			Messaging msg = null;
			if ((this.opCode == DELRQ.packetOpcode )|| (this.opCode == LOGRQ.packetOpcode) || (this.opCode == RRQ.packetOpcode) || (this.opCode == WRQ.packetOpcode)){
				if(nextByte!='\0'){
					bytes.add(nextByte);
				}
				else{
					Byte[] byteop=bytes.toArray(new Byte[bytes.size()]);
					String res=new String(buildArr(byteop), 0, bytes.size(), StandardCharsets.UTF_8);
					if(this.opCode == DELRQ.packetOpcode){
						msg=new DELRQ(res);
					}
					else if(this.opCode == LOGRQ.packetOpcode){
						msg=new LOGRQ(res);
					}
					else if(this.opCode == RRQ.packetOpcode){
						msg=new RRQ(res);
					}
					else if(this.opCode == WRQ.packetOpcode){
						msg=new WRQ(res);
					}
					bytes.clear();
					count = 0;
					return msg;
				}
			}
			else if(this.opCode==ACK.packetOpcode){
				if(bytes.size()<1){
					bytes.add(nextByte);
				}
				else{
					bytes.add(nextByte);
					Byte[] blockshortBytes=bytes.toArray(new Byte[bytes.size()]);
					short blockshort=bytesToShort(blockshortBytes);
					bytes.clear();
					count = 0;
					return new ACK(blockshort);
				}
			}
			else if(this.opCode==ERROR.packetOpcode){	

				if((nextByte != '\0') || (nextByte == '\0' && bytes.size() <= 2)){ //check!
					bytes.add(nextByte);
				}
				else{	        			
					Byte[] arrayBytes = bytes.toArray(new Byte[bytes.size()]);
					Byte[] errorCode = new Byte[2];
					errorCode[0]=arrayBytes[0];
					errorCode[1]=arrayBytes[1];
					short errCode=bytesToShort(errorCode);
					String errMsg=new String(buildArr(arrayBytes), 2, bytes.size()-2, StandardCharsets.UTF_8);
					msg = new ERROR(errCode,errMsg);
					bytes.clear();
					count = 0;
					return msg;
				}
			}
			else if(this.opCode==DATA.packetOpcode){
				if(bytes.size()<2){
					bytes.add(nextByte);
				}
				else if(bytes.size()==2){
					Byte[] datalength = new Byte[2];
					datalength[0] = bytes.get(0);
					datalength[1] = bytes.get(1);
					this.packetSize = bytesToShort(datalength);
					this.countdata = 0;
					bytes.add(nextByte);
				}
				else if (this.countdata < packetSize){
					bytes.add(nextByte);
					this.countdata++;
				}
				else{
					bytes.add(nextByte);
					Byte[] blockshortLength = new Byte[2];
					blockshortLength[0] = bytes.get(2);
					blockshortLength[1] = bytes.get(3);
					short blockshort = bytesToShort(blockshortLength);
					Byte[] arrayBytes = bytes.toArray(new Byte[bytes.size()]);
					arrayBytes = Arrays.copyOfRange(arrayBytes, 4, arrayBytes.length);
					byte[] data = buildArr(arrayBytes);
					msg = new DATA(packetSize , blockshort , data);
					bytes.clear();
					count = 0;
					this.countdata = 0;
					return msg;
				}
			}
			else{
				bytes.clear();
				count = 0;
				return new BadInput();
			}
		}
		return null;
	}

	@Override
	public byte[] encode(Messaging message) {
		return message.enc();
	}   

	private byte[] buildArr (Byte[] build){
		byte[] res = new byte[build.length];
		for (int i=0; i< build.length; i++) {
			res[i] = build[i].byteValue();   
		}
		return res;
	}

	public static short bytesToShort(Byte[] byteArr) {
		short res = (short)((byteArr[0].byteValue() & 0xff) << 8);
		res += (short)(byteArr[1].byteValue() & 0xff);     
		return res;
	}

	public static byte[] shortToBytes(short num) {
		byte[] bytesArr = new byte[2];
		bytesArr[0] = (byte)((num >> 8) & 0xFF);
		bytesArr[1] = (byte)(num & 0xFF);
		return bytesArr;
	}
}
