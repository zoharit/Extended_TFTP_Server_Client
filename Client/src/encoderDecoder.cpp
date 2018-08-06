
#include "../include/encoderDecoder.h"
using namespace std;
string EncoderDecoder::findSpace(string ss) {
    string space;
    int size=ss.length();
    int i =0;
    while((ss[i]!= ' ')&&(i!=size))
    {
    	space+=ss[i];
        i++;
    }
    return space;
}

void EncoderDecoder::insertToMyVec(vector<char> *vec, char bytearr[], int len) {
    for(int i=0;i< len;i++ ){
        vec->push_back(bytearr[i]);
    }

}

vector<char>* EncoderDecoder::encoder(string packet) {
    vector<char> *res=new vector<char>;
    std::string opCode=findSpace(packet);
    char op[2];
    char errCode[2];
    if(!((opCode.compare("WRQ")==0) || (opCode.compare("RRQ")==0)||(opCode.compare("DIRQ")==0) ||(opCode.compare("LOGRQ")==0)||(opCode.compare("DELRQ")==0)||(opCode.compare("DISC")==0))){
        shortToBytes(5,op);
        insertToMyVec(res,op,2);
        shortToBytes(4,errCode);
        insertToMyVec(res,errCode,2);
        string errMsgString="Illegal TFTP operation-Unknown Opcode";
        insertToMyVecStr(res,errMsgString);
        res->push_back('\0');
    }else {
        if (opCode.compare("RRQ")==0) {
            shortToBytes(1, op);
            insertToMyVec(res, op, 2);
            std:: string filename(packet.begin()+4, packet.begin() + (packet.length()));
            insertToMyVecStr(res, filename);
            res->push_back('\0');
        }
        if (opCode.compare("WRQ") == 0) {
            shortToBytes(2, op);
            insertToMyVec(res, op, 2);
            std::string filename(packet.begin() + 4, packet.begin() + (packet.length()));
            insertToMyVecStr(res, filename);
            res->push_back('\0');
        }
        if (opCode.compare("DIRQ") == 0) {
            shortToBytes(6, op);
            insertToMyVec(res, op, 2);
            if (packet.length() > 4) {
                throw std::invalid_argument("received invalid input");
            }
        }
        if (opCode.compare("DELRQ") == 0) {
            shortToBytes(8, op);
            insertToMyVec(res, op, 2);
            std::string filename(6+packet.begin(),packet.begin()+(packet.length()));
            insertToMyVecStr(res, filename);
            res->push_back('\0');
        }
        if (opCode.compare("LOGRQ") == 0) {
            shortToBytes(7, op);
            insertToMyVec(res, op, 2);
            std:: string username(packet.begin() + 6, packet.begin() + (packet.length()));
            insertToMyVecStr(res, username);
            res->push_back('\0');
        }
        if (opCode.compare("DISC") == 0) {
            shortToBytes(10, op);
            insertToMyVec(res, op, 2);
            if (packet.length()>4) {
                throw std::invalid_argument("received invalid input");
            }
        }
    }
    return res;
}


void EncoderDecoder::insertToMyVecStr(vector<char> *vec,string ss) {
    for(unsigned i = 0 ;i <ss.length(); i++){
        vec->push_back( ss[i] );
    }
}
Packets* EncoderDecoder::decoder(string packet) {

    char* opcode=new char[2];
    opcode[0]=packet[0];
    opcode[1]=packet[1];
    short opCodeShort=bytesToShort(opcode);
    if(opCodeShort==3){
    	opcode[0]=packet[2];
    	opcode[1]=packet[3];
        short size = bytesToShort(opcode);
        opcode[0]=packet[4];
        opcode[1]=packet[5];
        vector<char> data;
        short block = bytesToShort(opcode);
        copy(5+packet.begin(),packet.end(),back_inserter(data));
        DATA *ans = new DATA(opCodeShort,size,block,data);
        return ans;
    }
    if(opCodeShort==4){
    	opcode[0]=packet[2];
    	opcode[1]=packet[3];
        short block =bytesToShort(opcode);
        ACK *ans = new ACK(opCodeShort,block);
        return ans;
    }

    if(opCodeShort==5){
    	opcode[0]=packet[2];
    	opcode[1]=packet[3];
        short error=bytesToShort(opcode);
        ERROR *ans= new ERROR(opCodeShort,error);
        return ans;
    }

    if (opCodeShort==9){
        string delORadd;
        if(packet[3]=='0'){
        	delORadd="del";
        }
        else {
        	delORadd="add";
        }
        string fileName(4+packet.begin(),packet.begin()+packet.length()-2);
        BCAST *ans= new BCAST(opCodeShort,delORadd,fileName);
        return ans;
    }
    return nullptr;
}

short EncoderDecoder::bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}


void EncoderDecoder:: shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

