#include "com_test_protocol_OneTouchUltra.h"
#include "ot_ultra_protocol.h"

#include <string.h>
#include <time.h>

JNIEXPORT jint JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1disconnect
  (JNIEnv *env, jclass cls)
{
	return (jint)ot_ultra_disconnect();
}

JNIEXPORT jint JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1poweron
  (JNIEnv *env, jclass cls)
{
	return (jint)ot_ultra_poweron();
}

JNIEXPORT jstring JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1read_1software_1info
  (JNIEnv *env, jclass cls)
{
	 char info[MAX_STRSIZ];
	 ot_ultra_read_software_info(info);
	 return env->NewStringUTF(info);
}

JNIEXPORT jstring JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1read_1serial_1number
  (JNIEnv *env, jclass cls)
{
	char serial[MAX_STRSIZ];
	ot_ultra_read_serial_number(serial);
	return env->NewStringUTF(serial);
}

JNIEXPORT jstring JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1get_1date
  (JNIEnv *env, jclass cls)
{
	char* time = ctime(ot_ultra_get_date());
	int len = strlen(time);
	if (time != NULL)
		time[len-1] = '\0';
	return env->NewStringUTF(time);
}

JNIEXPORT jint JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1set_1date
  (JNIEnv *env, jclass cls)
{
	return ot_ultra_set_date((long)time(NULL));
}

JNIEXPORT jint JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1get_1unit
  (JNIEnv *env, jclass cls)
{
	return (jint)ot_ultra_get_unit();
}

JNIEXPORT jint JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1get_1nrecords
  (JNIEnv *env, jclass cls)
{
	return (jint)ot_ultra_get_nrecords();
}

JNIEXPORT jfloat JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1read_1record_1value
  (JNIEnv *env, jclass cls, jint offset)
{
	RECORD record;
	ot_ultra_read_record((WORD)offset, &record);
	return mg_dL_to_mmol_L((int)record.value);
}

JNIEXPORT jstring JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1read_1record_1date
  (JNIEnv *env, jclass cls, jint offset)
{
	RECORD record;
	ot_ultra_read_record((WORD)offset, &record);
	char* time = ctime(record.time);
	int len = strlen(time);
	if (time != NULL)
		time[len-1] = '\0';
	return env->NewStringUTF(time);
}


JNIEXPORT jint JNICALL Java_com_test_protocol_OneTouchUltra_ot_1ultra_1delete_1all_1records
  (JNIEnv *env, jclass cls)
{
	return (jint)ot_ultra_delete_all_records();
}