#include <stdlib.h>
#include <connectionHandler.h>

#include "../include/Run.h"
using namespace  std;

int main (int argc, char *argv[]) {

    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    ConnectionHandler* connectionHandler = new ConnectionHandler(host, port);
    if (!connectionHandler->connect()) {
        std::cerr <<"connection field"<< host<<" : "<< port<< std::endl;
        return 1;
    }
    EncoderDecoder *encoderDecoderfirst=new EncoderDecoder();
    EncoderDecoder *encoderDecodersecond=new EncoderDecoder();
    boost::mutex mutex;
    Protocol *protocol=new Protocol();
    connectionHandler->setMyProt(protocol);
    Server serverTask(*encoderDecodersecond,connectionHandler);
    fromKeyBoard keyBoard(*encoderDecoderfirst,connectionHandler);
    boost::thread thread1(boost::bind(&fromKeyBoard::run,&keyBoard));
    boost::thread thread2(boost::bind(&Server::run,&serverTask));
     thread2.join();
     thread1.join();
    return 0;
}
