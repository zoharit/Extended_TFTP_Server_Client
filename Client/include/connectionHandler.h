#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__
                                           
#include <string>
#include <iostream>
#include <boost/asio.hpp>
#include "protocol.h"
#include "encoderDecoder.h"
#include <boost/system/error_code.hpp>
using boost::asio::ip::tcp;

class ConnectionHandler {
private:
	const std::string host_;    void insertToMyVec(vector<char>*,char[],int);
    void insertToMyVecStr(vector<char>*,string);
	const short port_;
	boost::asio::io_service io_service_;   // Provides core I/O functionality
	tcp::socket socket_;
    vector<char> myVec;
    EncoderDecoder *EncDec;
    Protocol* protocolPort;
    short opCode=0;

public:

    ConnectionHandler(std::string host, short port);
    ConnectionHandler(ConnectionHandler&);
    const string &getHost_() const;

    const short getPort_() const;

    void setMyProt(Protocol* myProt);

    const boost::asio::io_service &getIo_service_() const;

    void setIo_service_(const boost::asio::io_service &io_service_);

    const tcp::socket &getSocket_() const;

    void setSocket_(const tcp::socket &socket_);


    virtual ~ConnectionHandler();
 
    // Connect to the remote machine
    bool connect();
 
    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    //bool getBytes(char bytes[], unsigned int bytesToRead);
    bool getBytes(char bytes[], unsigned int bytesToRead);
 
	// Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);


    // Get Ascii data from the server until the delimiter character
    // Returns false in case connection closed before null can be read.
    string getPacket(Packets& frame);
 
    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendPacket(vector<char>*);
	
    // Close down the connection properly.
    void close();
 
}; //class ConnectionHandler
 
#endif
