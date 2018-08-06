#ifndef ENCODERDECODER_H
#define ENCODERDECODER_H
#include <iostream>
#include <vector>
#include <stdexcept>
#include "Message.h"

using namespace std;

class EncoderDecoder{

public:
    short bytesToShort(char* );
    void shortToBytes(short , char*);
    vector<char>* encoder(string);
    void insertToMyVec(vector<char>*,char[],int);
    void insertToMyVecStr(vector<char>*,string);
    Packets* decoder(string);
    string findSpace(string);
};






#endif
