#include <boost/thread/mutex.hpp>

#ifndef BOOST_ECHO_CLIENT_SHARE_H
#define BOOST_ECHO_CLIENT_SHARE_H
using namespace std;
static boost::mutex LOCK;

extern ofstream* _file;
extern string fileName;
extern bool dirq;
extern bool ToUpload;
extern bool finish;
#endif
