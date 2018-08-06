#ifndef PACKETS_H
#define PACKETS_H
#include <fstream>
#include <iostream>
#include <vector>
class Protocol;
using namespace std;

class Packets {

private:
    short opcode;

public:
    virtual ~Packets();
    Packets();
    void setOpcode(short opcode);
    Packets(short);
    virtual string process(vector<char>& , Protocol*)=0;
    short getOpcode() const;
    void shortToBytes(short, char*);
    char* mergeArr(char*, char*,unsigned, unsigned);
    char* fromVecToChar(vector<char>);
};

class DATA : public Packets{
private:
    short size;
    short blockNum;
    vector<char> datafile ;

public:
    DATA();
    DATA(short,short, short, vector<char>);
    void ziro(vector<char>&);
    string process(vector<char>&,Protocol*);
};

class BCAST: public Packets {
private:
    string DelORadd;
    string filename;

public:
    BCAST();
    BCAST(short, string, string);
    string process(vector<char>&,Protocol*);
};


class ACK : public Packets{
private:
    short numBlock;

public:
    ACK();
    void setNumBloct(short numblock);
    ACK&operator=(const ACK&);
    ACK(short, short);
    string process(vector<char>&,Protocol*);

};

class ERROR: public Packets {
private:
    short errMsg;

public:
    ERROR();
    ERROR(short, short);
    string process(vector<char>&,Protocol*);
};




#endif
