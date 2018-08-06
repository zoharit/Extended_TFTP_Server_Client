#include "../include/SameField.h"
#include "protocol.h"

Packets::Packets():opcode(0) {}

void Packets::setOpcode(short opcode) {
    Packets::opcode = opcode;
}
Packets::Packets(short op):opcode(op) {}

short Packets::getOpcode() const { return opcode;}

ACK::ACK():Packets(0),numBlock(0) {}

ACK::ACK(short op, short block):Packets(op),numBlock(block) {}

string ACK :: process(vector<char>& buffer,Protocol *prot) {
    if (ToUpload) {
         if(numBlock==0){
             try {
                 ifstream file;
                 file.open(fileName,fstream::binary|fstream::ate);
                 file.seekg(0, file.end);
                 unsigned size = file.tellg();
                 file.close();
                 file.open(fileName,fstream::binary);
                 char tempBuffer[size];
                 file.readsome(tempBuffer, size);
                 for (unsigned i =0 ; i < size; i++) {
                     buffer.push_back(tempBuffer[i]);
                 }
             }catch (exception){
                 return "Access violation - File cannot be written, read or deleted\0";
             }
         }
        if(buffer.size()<512) {
            ToUpload = false;
            char myOP[2];
            char myBlock[2];
            char myPacketSize[2];
            shortToBytes(3, myOP);
            shortToBytes(numBlock+1, myBlock);
            shortToBytes(buffer.size(), myPacketSize);
            string ans;
            char nextb=myOP[0];
            ans.append(1,nextb);
            nextb=myOP[1];
            ans.append(1,nextb);
            nextb=myPacketSize[0];
            ans.append(1,nextb);
            nextb=myPacketSize[1];
            ans.append(1,nextb);
            nextb=myBlock[0];
            ans.append(1,nextb);
            nextb=myBlock[1];
            ans.append(1,nextb);
            for(unsigned j=0;j<buffer.size();j++){
            	nextb=buffer.at(j);
            	ans.append(1,nextb);
            }
            buffer.clear();
            cout << "ACK " <<numBlock<< endl;
            return ans;
        }
        else {

            char myOP[2];
            char myBlock[2];
            char myPacketSize[2];
            shortToBytes(3, myOP);
            shortToBytes(numBlock + 1, myBlock);
            shortToBytes(512, myPacketSize);
            string ans;
            char nextb=myOP[0];
            ans.append(1,nextb);
            nextb=myOP[1];
            ans.append(1,nextb);
            nextb=myPacketSize[0];
            ans.append(1,nextb);
            nextb=myPacketSize[1];
            ans.append(1,nextb);
            nextb=myBlock[0];
            ans.append(1,nextb);
            nextb=myBlock[1];
            ans.append(1,nextb);
            for(unsigned j=0;j<512;j++){
            	nextb=buffer.at(j);
            	ans.append(1,nextb);
            }
            buffer.erase(buffer.begin(), buffer.begin() + 512);
            cout << "ACK " << numBlock << endl;
            return ans;
        }
    }
        cout << "ACK " << numBlock<< endl;
    return "";

}

ACK &ACK::operator=(const ACK &other) {
	setNumBloct(other.numBlock);
    Packets::setOpcode(other.getOpcode());
    return  *this;
}

void ACK::setNumBloct(short blockNo) {
	ACK::numBlock = blockNo;
}


ERROR::ERROR():Packets(0),errMsg(0) {}

ERROR::ERROR(short op, short errorType)
        :Packets(op),errMsg(errorType){}

string ERROR::process(vector<char>& buffer,Protocol* prot) {
    cout <<"ERROR "<<errMsg<<endl;
    return "";
}

BCAST::BCAST():Packets(0),DelORadd(""),filename("") {}

BCAST::BCAST(short op, string indc, string file)
        :Packets(op), DelORadd(indc),filename(file) {}

string BCAST::process(vector<char>& buffer,Protocol *prot) {
    if(DelORadd.compare("add")==0) {
        cout << "WRQ " << filename << " complete" << endl;
    }
    cout <<"BCAST "<< DelORadd << " " << filename << endl;
    this->filename="";
    return "";
}

DATA::DATA():Packets(0),size(0),blockNum(0), datafile() {

}

DATA::DATA(short op, short siz,short bloc ,vector<char> dat):Packets(op),size(siz),blockNum(bloc), datafile() {
    while(dat.size()!=0){
        char c=dat.at(0);
        datafile.push_back(c);
        dat.erase(dat.begin());
    }
}

string DATA::process(vector<char>& buffer,Protocol *prot) {
    if(size < 512){
        buffer.insert(buffer.end(),datafile.begin(),datafile.end());
        if(dirq){
            if(size==0){
                cout<<endl;
                return "";
            }
            ziro(buffer);
            dirq= false;
            buffer.clear();
        }else{

            vector<char>::iterator it= buffer.begin();
            while(it!=buffer.end()) {
                _file->put(*it);
                it++;
            }
            _file->flush();
            _file->close();
            delete _file;
            buffer.clear();
            cout << "RRQ "<<fileName <<" complete" <<endl;
        }
        buffer.clear();
        return "";
    }
    else {
        if (dirq) {
            buffer.insert(buffer.end(), datafile.begin(), datafile.end());

        }else{
            buffer.insert(buffer.begin(), datafile.begin(), datafile.end());
            vector<char>::iterator it= buffer.begin();
            while(it!=buffer.end()) {
                _file->put(*it);
                it++;
            }
            buffer.clear();

        }
        string ans="";
        char myOP [2];
        char myBlock [2];
        shortToBytes(4, myOP);
        shortToBytes(blockNum, myBlock);
        char nextb=myOP[0];
        ans.append(1,nextb);
        nextb=myOP[1];
        ans.append(1,nextb);
        nextb=myBlock[0];
        ans.append(1,nextb);
        nextb=myBlock[1];
        ans.append(1,nextb);
        return ans;
    }
}

void DATA::ziro(vector<char> &vec) {
    unsigned i=0;
    string file;
    while(i<vec.size()-1){
        while(vec[i]!='\0'){
            file+=vec[i];
            i++;
         }
        i++;
        cout<<file<<endl;
        file="";
    }
}

void Packets:: shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

char* Packets::mergeArr(char* first, char* second,unsigned sizeF, unsigned sizeS){
    char* result= new char [sizeF+sizeS];
    for(unsigned i=0;i<sizeF;i++){
        result[i]=first[i];
    }
    for(unsigned i=0; i<sizeS;i++){
        result[i+sizeF]=second[i];
    }
    return result;
}

char* Packets::fromVecToChar(vector<char> myVec){
    char* ans= new char[myVec.size()];
    int i=0;
    for(vector<char>::iterator it=myVec.begin();it!=myVec.end();it++){
    	ans[i]=*it;
        cout<<"*"<<ans[i]<<endl;
        i++;
    }
    return ans;
}

Packets::~Packets() {}
