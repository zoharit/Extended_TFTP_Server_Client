package bgu.spl171.net.impl.TFTPtpc;

import bgu.spl171.net.Packet.Messaging;
import bgu.spl171.net.Packet.EncoderDecoderImpl;
import bgu.spl171.net.api.bidi.TFTP;
import bgu.spl171.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {
        Server.threadPerClient(
               Integer.parseInt(args[0]), //port
               () -> new TFTP(),//protocol factory
               () -> new EncoderDecoderImpl()//message encoder decoder factory            
        ).serve();
    }
}
