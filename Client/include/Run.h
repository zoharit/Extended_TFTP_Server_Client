
#ifndef TASKS_H
#define TASKS_H

#include <iostream>
#include <boost/thread.hpp>
#include "protocol.h"
#include "encoderDecoder.h"
#include "connectionHandler.h"
#include "SameField.h"

class Run  {
public:
	Run() ;
    virtual void run()=0;
    virtual ~Run();
};

class Server: public Run{
private:
    EncoderDecoder encoderDecoder;
    Protocol myProtocol;
    ConnectionHandler* connectionHandler;
public:
    Server();
    Server(EncoderDecoder,ConnectionHandler*);
    void run();

};

class fromKeyBoard: public Run{
private:
    EncoderDecoder encoderDecoder;
    Protocol myProtocol;
    ConnectionHandler* connectionHandler;


public:
    fromKeyBoard();
    fromKeyBoard(EncoderDecoder,ConnectionHandler*);
    void run();
};


#endif
