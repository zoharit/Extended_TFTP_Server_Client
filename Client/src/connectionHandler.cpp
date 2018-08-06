
#include <iostream>
#include <boost/asio.hpp>
#include "../include/connectionHandler.h"
using boost::asio::ip::tcp;
using namespace std;
using std::cerr;
using std::cin;
using std::cout;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port)
        : host_(host), port_(port), io_service_(), socket_(io_service_),opCode(0),EncDec(),myVec(),protocolPort(){}
    
ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to " 
        << host_ << ":" << port_ << std::endl;
    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
        {
            std::cout << "failed connecting"<<endl;
            throw boost::system::system_error(error);
        }
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getBytes(char* bytes, unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }


		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

string ConnectionHandler::getPacket(Packets &frame) {
    char opcode[2];
    try {
        getBytes(opcode, 2);
        opCode=EncDec->bytesToShort(opcode);
        if(opCode==3){
            getBytes(opcode,2);
            short pacetSize=EncDec->bytesToShort(opcode);
            getBytes(opcode, 2);
            short blockNumber = EncDec->bytesToShort(opcode);
            char tempdata[pacetSize];
            getBytes(tempdata,pacetSize);
            vector<char> data(tempdata,tempdata+pacetSize);
            DATA *ans = new DATA(opCode,pacetSize,blockNumber,data);
            return protocolPort->process(ans);

        }
        if(opCode==4){
            getBytes(opcode, 2);
            short block=EncDec->bytesToShort(opcode);
            ACK *ans = new ACK(4,block);
            return protocolPort->process(ans);
        }

        if(opCode==5){
            getBytes(opcode, 2);
            short error=EncDec->bytesToShort(opcode);
            char nextb;
            getBytes(&nextb,1);
            while(nextb!='\0'){
                getBytes(&nextb,1);
            }
            ERROR *ans= new ERROR(opCode,error);
            return protocolPort->process(ans);
        }

        if (opCode==9){
            getBytes(opcode, 1);
            char a=opcode[0];
            string ind;
            if(a==1){
                ind="add";
            }
            else{
                ind="del";
            }
            char nextb;
            getBytes(&nextb,1);
            while(nextb!='\0'){
            	myVec.push_back(nextb);
                getBytes(&nextb,1);
            }
            string filename(myVec.begin(),myVec.end());
            BCAST *ans= new BCAST(opCode,ind,filename);
            myVec.clear();
            return protocolPort->process(ans);
        }
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return "";
    }
    return "";
}

bool ConnectionHandler::sendPacket(vector<char>* vec) {
    char op[2];
    op[0]=vec->at(0);
    op[1]=vec->at(1);
    char byteToSend[1];
    sendBytes(op,2);

    if((op[0]==0) && ((op[1]==1)||(op[1]==2)||(op[1]==7)||(op[1]==5)||(op[1]==8)) ){
        int i=2;
        while(vec->at(i)!='\0'){
            byteToSend[0]=vec->at(i);
            sendBytes(byteToSend,1);
            i++;
        }
        byteToSend[0]='\0';
        sendBytes(byteToSend,1);
    }
    if((op[1]==3) && (op[0]==0)){
        unsigned packetSize=vec->size();
        for(unsigned i=2;i<packetSize;i++){
            byteToSend[0]=vec->at(i);
            sendBytes(byteToSend,1);
        }
    }
    if((op[0]==0)&&(op[1]==4)){
        char block[2];
        block[0]=vec->at(2);
        block[1]=vec->at(3);
        sendBytes(block,2);
    }
    return true;
}

ConnectionHandler::ConnectionHandler(ConnectionHandler &conn): host_(conn.getHost_()), port_(conn.getPort_()), io_service_(), socket_(io_service_){//copy constructor
setMyProt(conn.protocolPort);
}



const string &ConnectionHandler::getHost_() const {
    return this->host_;
}

const short ConnectionHandler::getPort_() const {
    return this->port_;
}
void ConnectionHandler::close() {
    try{
        this->socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

void ConnectionHandler::setMyProt( Protocol *myProtocol) {
	this->protocolPort=myProtocol;
}




