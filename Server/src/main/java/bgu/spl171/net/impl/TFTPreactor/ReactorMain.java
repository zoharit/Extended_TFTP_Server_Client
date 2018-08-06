package bgu.spl171.net.impl.TFTPreactor;

import bgu.spl171.net.Packet.EncoderDecoderImpl;
import bgu.spl171.net.api.bidi.TFTP;
import bgu.spl171.net.srv.Server;
import bgu.spl171.net.Packet.Messaging;
public class ReactorMain {
    public static void main(String[] args) {
        Server.reactor(
        		5,
        		 Integer.parseInt(args[0]), //port
                () -> new TFTP(),//protocol factory
                () -> new EncoderDecoderImpl()//message encoder decoder factory            
         ).serve();



    }
}
