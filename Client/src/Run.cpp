
#include "../include/Run.h"

#include <thread>

using namespace std;

Run::Run() {}

Run::~Run() {

}


//******************************************************************

Server::Server() :encoderDecoder(),connectionHandler(){}
Server::Server(EncoderDecoder myED,ConnectionHandler* handler)
        :encoderDecoder(myED),myProtocol(),connectionHandler(handler) {}

void Server::run() {

    while (!finish) {
        Packets *answer;
        std::string line;
        line =connectionHandler->getPacket(*answer);
        vector<char> ans(line.begin(), line.end());
        if (!line.empty()) {
            {
                boost::mutex::scoped_lock lock(LOCK);
                if (!connectionHandler->sendPacket(&ans)) {///sync
                    std::cout << "Disconnected. Exiting...\n" << std::endl;
                    break;
                }
            }

        }

    }
}

//*********************************************************************

fromKeyBoard::fromKeyBoard():encoderDecoder(),connectionHandler(){}
fromKeyBoard::fromKeyBoard(EncoderDecoder myED,ConnectionHandler* handler)
        :encoderDecoder(myED),myProtocol(),connectionHandler(handler) {

}
void fromKeyBoard::run() {
    while (1) {
        const short bufsize = 512;
        char buf[bufsize];
        cin.getline(buf, bufsize);
        string line(buf);
        vector<char> *temp=encoderDecoder.encoder(line);
        char opcode1 = temp->at(0);
        char opcode2=temp->at(1);
        if((opcode1==0)&&(opcode2==10)){
            {
                finish= true;
                boost::mutex::scoped_lock lock(LOCK);
                if (!connectionHandler->sendPacket(temp)) {
                    break;
                }
            }
            break;
        }
        else {
            if((opcode1==0)&&(opcode2==1)){
                string temp2;
                char c;
                for(unsigned int i=4;i<line.size();i++){
                    c=line.at(i);
                    temp2.append(1,c);
                }
                fileName=temp2;
                _file = new ofstream(fileName,fstream::out|fstream::binary|fstream::trunc|fstream::ate);
            }
            if((opcode1==0)&&(opcode2==5)){
                string temp2;
                char c;
                for(unsigned int i=4;i<line.size();i++){
                    c=line.at(i);
                    temp2.append(1,c);
                }
                fileName=temp2;
            }
            if((opcode1==0)&&(opcode2==2)){
                string temp2;
                char c;
                for(unsigned int i=4;i<line.size();i++){
                    c=line.at(i);
                    temp2.append(1,c);
                }
                ToUpload = true;
                fileName=temp2;
            }
            if((opcode1==0)&&(opcode2==6)){
                dirq=true;
            }
            boost::mutex::scoped_lock lock(LOCK);///sync
            if (!connectionHandler->sendPacket(temp)) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
                break;
            }
        }
    }
}
