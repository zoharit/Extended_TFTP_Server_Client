
#include "../include/protocol.h"

Protocol::Protocol():terminated(false) ,finish(false), filename(""), file(),Upload(false), Dirq(false) {

}

string Protocol::process(Packets* msg) {
    string ans;
    if(!isTerminate()) {
        if((msg->getOpcode()) == 3){
        	ans = dynamic_cast<DATA*>(msg)->process(file,this);
        }
        if((msg->getOpcode())== 4){
        	ans = dynamic_cast<ACK*>(msg)->process(file,this);
        }
        if((msg->getOpcode()) == 5){
        	ans = dynamic_cast<ERROR*>(msg)->process(file,this);
        }
        if((msg->getOpcode())== 9){
        	ans = dynamic_cast<BCAST*>(msg)->process(file,this);
        }
    }
    else{
        if((msg-> getOpcode())== 4){
            ans = msg-> process(file,this);
            this->finish=true;
        }


    }
    return ans;
}

void Protocol::setTerminate() {
	this->terminated=true;
}

bool Protocol::isDone() {
	return this->finish;
}

bool Protocol::isTerminate() {
    return this->terminated;
}

bool Protocol::ToUpload(){
    return this->Upload;
}

void Protocol::setUpLoad(bool toUP) {
	this->Upload=toUP;
}

bool Protocol::DIRQ()  {
    return this->Dirq;
}

void Protocol::setDIRQ(bool isDIRQ) {
    Protocol:: Dirq= isDIRQ;
}


void Protocol::setFileName(string filenam) {
    this->filename = filenam;
}

string Protocol::getFileName() {
    return this->filename;
}
