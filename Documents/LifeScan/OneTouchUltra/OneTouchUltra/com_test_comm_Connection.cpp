#include "com_test_comm_Connection.h"
#include "ot_ultra_protocol.h"

JNIEXPORT jint JNICALL Java_com_test_comm_Connection_init_1com_1port
  (JNIEnv *env, jclass obj, jint portno)
{
	return (jint)init_com_port((int)portno);
}

JNIEXPORT jint JNICALL Java_com_test_comm_Connection_close_1com_1port
  (JNIEnv *, jclass)
{
	return (jint)close_com_port();
}