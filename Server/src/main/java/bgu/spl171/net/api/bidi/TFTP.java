package bgu.spl171.net.api.bidi;

import bgu.spl171.net.Packet.ACK;
import bgu.spl171.net.Packet.BCAST;
import bgu.spl171.net.Packet.DATA;
import bgu.spl171.net.Packet.DELRQ;
import bgu.spl171.net.Packet.DIRQ;
import bgu.spl171.net.Packet.DISC;
import bgu.spl171.net.Packet.ERROR;
import bgu.spl171.net.Packet.LOGRQ;
import bgu.spl171.net.Packet.Messaging;
import bgu.spl171.net.Packet.RRQ;
import bgu.spl171.net.Packet.BadInput;
import bgu.spl171.net.Packet.WRQ;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;



public class TFTP implements BidiMessagingProtocol<Messaging>{

	public static final HashMap<String,Integer> nameList =new HashMap<String,Integer>();
	private final static Path FILEPATH= Paths.get("Files"); 
	private static final short DATASIZE=512;
	private int id;
	private short NUMBERblock=0;
	private long theLastBlock;
	private long numBlcoks;
	private Connections<Messaging> connections;
	private boolean shouldTerminate=false;
	private short cell=0;
	private FileInputStream fileinput;
	private FileOutputStream fileout;
	private String DIREQ="";
	private Messaging LastOPER = null;
	public String fileName;

	@Override
	public void start(int id, Connections<Messaging> connections) {
		this.id=id;
		this.connections=connections;
	}

	@Override
	public void process(Messaging message) {
		Messaging msg=null;
		if(message instanceof LOGRQ){
			msg=logrq((LOGRQ)message);
		}
		else if(message instanceof BadInput){
			msg=new ERROR((short)4, ERROR.MSG[4]);
		}
		else{
			if(!isLogin()){
				msg=new ERROR((short)6, ERROR.MSG[6]);
			}
			else{
				if(message instanceof DELRQ){
					msg=delrq((DELRQ)message);
				}
				else if(message instanceof ACK){
					msg=handler((ACK)message);
				}
				else if(message instanceof WRQ){
					msg=WRQpacket((WRQ)message);
				}


				else if(message instanceof RRQ){
					msg=rrq((RRQ)message);
				}

				else if(message instanceof DISC){
					msg=disc((DISC)message);
				}
				else if(message instanceof ERROR){
					if(LastOPER instanceof WRQ){
						terminateWrite();
					}
					if(LastOPER instanceof RRQ){
						terminateRead();
					}
				}
				else if(message instanceof DATA){
					msg=data((DATA)message);
				}
				else if(message instanceof DIRQ){
					msg=dirq((DIRQ)message);
				}
			}
		}
		connections.send(id , msg);
		if(message instanceof DATA && ((DATA)message).packetByte < DATASIZE && msg instanceof ACK){
			toBCAST((byte)1, fileName);
		}
		else if(message instanceof DELRQ  && msg instanceof ACK){
			toBCAST((byte)0, ((DELRQ)message).Filename);
		}		
	}

	private boolean isLogin(){
		return TFTP.nameList.containsValue(id);
	}

	private void toBCAST(byte delORadd, String fileName){
		for (Integer value :  TFTP.nameList.values()){
			connections.send(value, new BCAST(delORadd, fileName));
		}
	}



	private Messaging DATApacket(){
		Messaging msg = null;
		if(numBlcoks >= 1){
			int size = DATASIZE;
			if(numBlcoks == 1){
				size = (int)theLastBlock;
			}
			byte[] buffer = new byte[size];
			int connectionHand;
			if(size > 0){
				try {
					connectionHand= fileinput.read(buffer);
				} 
				catch (IOException e) {
					terminateRead();	
					return new ERROR((short)2, ERROR.MSG[2]);

				}
			}
			msg = new DATA((short)size , NUMBERblock , buffer);
			numBlcoks--;	
			NUMBERblock++;
			if(numBlcoks == 0){
				terminateRead();
			}
		}
		return msg;
	}

	private Messaging WRQpacket(WRQ message){

		Messaging msg = null;
		fileName = message.fileName;
		synchronized(this){
			if(WRQexist(message.fileName) != null){
				msg = new ERROR((short)5, ERROR.MSG[5]);
			}
			else {			
				try {
					fileName = message.fileName;
					File file = new File(FILEPATH+File.separator+fileName+".tmp");
					fileout = new FileOutputStream(file); 
					msg = new ACK((short)0);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					msg = new ERROR((short)2, ERROR.MSG[2]);
				}			
			}
		}
		return msg;
	}

	private Messaging data(DATA message){
		Messaging msg=null;
		this.cell++;
		short msglen = (short)(message).packetByte;
		if(isFree(msglen)){
			if(new File(FILEPATH.toString()).getUsableSpace() < message.packetByte){
				msg = new ERROR((short)3, ERROR.MSG[3]);
				terminateWrite();
			}
			else{
				try{
					if(cell != message.blockByte || msglen > DATASIZE){
						msg = new ERROR((short)4,ERROR.MSG[4]);
						terminateWrite();
					}
					else{						fileout.write((message).data);
					fileout.flush();
					msg = new ACK(message.blockByte);

					if(msglen<DATASIZE){
						try{
							File file = new File(FILEPATH+File.separator+fileName+".tmp");
							File newFile = new File(FILEPATH+File.separator+fileName);
							file.renameTo(newFile);
							fileout.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
						this.cell = 0;
					}
					}					

				}
				catch(IOException e){
					msg = new ERROR((short)2, ERROR.MSG[2]);
					terminateWrite();
				}
			}

		}
		else{
			msg = new ERROR((short)3, ERROR.MSG[3]);
			terminateWrite();			
		}
		return msg;
	}

	private Messaging logrq(LOGRQ message){
		Messaging msg = null;
		if( TFTP.nameList.containsValue(id)){
			msg = new ERROR((short)0, ERROR.MSG[0]);
		}
		else if(TFTP.nameList.containsKey(message.userName)){
			msg = new ERROR((short)7, ERROR.MSG[7]);
		}
		else{
			msg = new ACK((short)0);
			TFTP.nameList.put((message).userName,id);
		}
		return msg;
	}

	private Messaging delrq(DELRQ message){
		Messaging msg = null;
		boolean err = false;
		File toDel = checkIfEx(((DELRQ)message).Filename);
		if(toDel == null){
			msg = new ERROR((short)1, ERROR.MSG[1]);
		}
		else{
			err = !toDel.delete();
			if(err){
				msg = new ERROR((short)2, ERROR.MSG[2]);
			}
			else{
				msg = new ACK((short)0);
			}
		}
		return msg;
	}

	private Messaging handler(ACK message){
		Messaging msg = null;
		if(((ACK)message).block != 0 ){
			if(LastOPER instanceof RRQ){
				msg = DATApacket();	
			}
			else if(LastOPER instanceof DIRQ){

				msg = msgPacket();	
			}
		}
		return msg;
	}

	private Messaging disc(DISC message){
		Messaging msg = new ACK((short)0);
		nameList.values().remove(id);
		shouldTerminate = true;
		return msg;
	}

	private Messaging dirq(DIRQ message){

		Messaging msg = null;
		LastOPER = message;
		DIREQ = "";
		File dirc = new File(FILEPATH.toString());
		File[] listfile = dirc.listFiles();
		for (File file : listfile){
			if(!file.isHidden()){
				String name = file.getName();
				if (!name.endsWith(".tmp")){
					DIREQ += name + '\0';
				}
			}
		} 
		numBlcoks = (DIREQ.length() / DATASIZE) + 1;
		theLastBlock = DIREQ.length() % DATASIZE;
		NUMBERblock = 0;

		msg = msgPacket();

		return msg;
	}
	private Messaging msgPacket(){
		Messaging msg = null;
		if(numBlcoks >= 1){
			NUMBERblock++;		
			int size = DATASIZE;
			if(numBlcoks == 1){

				size = (int)theLastBlock;
			}

			byte[] buffer;
			try {
				if(DIREQ.length()>1){
					buffer = DIREQ.substring(0, size).getBytes("UTF-8");
					DIREQ = DIREQ.substring(size, DIREQ.length());				
					msg = new DATA((short)size , NUMBERblock , buffer);
				}
				else{
					msg = new DATA((short)0 , NUMBERblock , new byte[0]);
				}

			} catch (UnsupportedEncodingException e) {
				msg = new ERROR((short)2, ERROR.MSG[2]);
			}		
			numBlcoks--;
		}
		return msg;
	}

	private File WRQexist(String filename){
		File dirc = new File(FILEPATH.toString());
		File[] listfile = dirc.listFiles();
		for (File file : listfile){
			if(!file.isHidden()){
				if (file.getName().equals(filename) || file.getName().equals(filename+".tmp")){
					return file;
				}
			}
		}
		return null;
	}


	private Messaging rrq(RRQ message){
		Messaging msg = null;
		boolean search = false;
		File dirc = new File(FILEPATH.toString());
		File[] listfile = dirc.listFiles();
		for (File file : listfile){
			if(!file.isHidden()){
				String name = file.getName();
				if (!name.endsWith(".tmp") && name.equals(message.fileName)){
					search = true;
					break;
				}
			}
		}
		if(!search){
			msg = new ERROR((short)1 , ERROR.MSG[1]);
		}
		else {
			File toReadFrom = new File(FILEPATH+File.separator+message.fileName);
			try {

				this.NUMBERblock = 0;
				this.numBlcoks = (toReadFrom.length() / DATASIZE) + 1;
				this.theLastBlock = toReadFrom.length() % DATASIZE;
				this.fileinput = new FileInputStream(toReadFrom);
				this.NUMBERblock++;
				msg = DATApacket();
				this.LastOPER = message;
			} 
			catch (IOException e) {
				terminateRead();
				msg = new ERROR((short)2, ERROR.MSG[2]);
			}
			catch(OutOfMemoryError e){
				terminateRead();
				msg = new ERROR((short)3, ERROR.MSG[3]);
			}
		}
		return msg;
	}

	private boolean isFree(long length){
		return length < Runtime.getRuntime().freeMemory();
	}

	private File checkIfEx(String filename){
		File dirc = new File(FILEPATH.toString());
		File[] listfile = dirc.listFiles();
		for (File file : listfile){
			if(!file.isHidden()){
				if (file.getName().equals(filename)){
					return file;
				}
			}
		}
		return null;
	}
	private void terminateWrite(){
		cell = 0;
		LastOPER = null;
		try{
			fileout.close();
			(new File((FILEPATH+"/"+fileName))).delete();
		}
		catch(IOException e){
			e.printStackTrace();
		}

	}

	private void terminateRead(){
		NUMBERblock = 0;
		LastOPER = null;
		try {
			fileinput.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}
	@Override
	public boolean shouldTerminate() {
		return shouldTerminate;
	}

}
