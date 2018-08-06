
#ifndef CLIENT_PROTOCOL_H
#define CLIENT_PROTOCOL_H

#include <fstream>
#include "Message.h"

class Protocol {
private:
    bool terminated;
    bool finish;
    string filename;
    vector<char> file;
    bool Upload;
    bool Dirq;

public:
    Protocol();
    bool DIRQ() ;
    void setDIRQ(bool DIRQ);
    bool isDone();
    bool ToUpload();
    string getFileName() ;
    void setUpLoad(bool toUP);
    void setFileName(string);
    string process(Packets*);
    bool isTerminate();
    void setTerminate();
};


#endif
