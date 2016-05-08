/**
 * ------------------------------------------------------------------------- *
 *																	
 * $Author: William Cheung
 *
 * $Date Modified: 2013/10/22
 *
 * ------------------------------------------------------------------------- *
 */


#include "ot_ultra_protocol.h"

#include <stdio.h>

// Basic Communication Functions
extern int	send_bytes(BYTE* data, int len);
extern int	receive_bytes(BYTE* buffer, int offset, int nbytes);

#define byte0(word)	((BYTE)word)
#define byte1(word)	((BYTE)(word >> 8))

union U_LONG			// A union used to partion a long integer into bytes
{
	BYTE byte[4];
	long lval;
};

static void array_copy(BYTE* src, int offset1, char* dst , int offset2, int length)
{
	int i = offset1, j = offset2, k = 0;
	for (k = 0; k < length; k++)
		dst[j + k] = src[i + k];
}

static int array_equal(BYTE* arr1, BYTE* arr2, int n)
{
	int i;
	for (i = 0; i < n; i++)
		if (arr1[i] != arr2[i])
			return 0;
	return 1;
}

static void array_print(BYTE* arr, int n)
{
	int i;
	for (i = 0; i < n; i++)
		printf("%02X ", arr[i]);
	printf("\n");
}


float mg_dL_to_mmol_L(int value) // Unit conversion
{
	return (float)value / 18.0;
}

static WORD crc_calculate_crc(WORD initial_crc, const BYTE *buffer, WORD length)
{	// Calculating the CRC(CCITT-CRC). In this program, initial_crc = 0xffff
	WORD index = 0;
	WORD crc = initial_crc;
	if (buffer != NULL)
	{
		for (index = 0; index < length; index++)
		{
			crc = (WORD)((BYTE)(crc >> 8) | (WORD)(crc << 8));
			crc ^= buffer [index];
			crc ^= (BYTE)(crc & 0xff) >> 4;
			crc ^= (WORD)((WORD)(crc << 8) << 4);
			crc ^= (WORD)((WORD)((crc & 0xff) << 4) << 1);
		}
	}
	return (crc);
}

static BYTE pc_ack1[] = {0x02, 0x06, 0x07, 0x03, 0xFC, 0x72};
static BYTE pc_ack2[] = {0x02, 0x06, 0x04, 0x03, 0xAF, 0x27};
static BYTE meter_ack1[] = {0x02, 0x06, 0x06, 0x03, 0xCD, 0x41};
static BYTE meter_ack2[] = {0x02, 0x06, 0x05, 0x03, 0x9E, 0x14};

int ot_ultra_disconnect() 
{
	BYTE data_out[] = {0x02, 0x06, 0x08, 0x03, 0xC2, 0x62}; // command from PC: Disconnect
	BYTE data_in[6];

	send_bytes(data_out, sizeof(data_out)); 
	receive_bytes(data_in, 0, sizeof(data_in));

	if (data_in[2] == 0x0C)
		return 1;
	return 0;
}

static int _ot_ultra_poweron()
{
	BYTE data_out[] = {0x02, 0x12, 0x00, 0x05, 0x0B, 0x02, 
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x19, 0xE7};
	BYTE data_in1[6], data_in2[17];
	
	send_bytes(data_out, sizeof(data_out));
	receive_bytes(data_in1, 0, 6);
	receive_bytes(data_in2, 0, 17);
	send_bytes(data_in1, 6);

	if (data_in2[3] == 0x05 && data_in2[4] == 0x06)
		return 1;
	return 0;
}

static int _ot_ultra_unknown()
{
	BYTE data_out[] = {0x02, 0x06, 0x0A, 0x03,0xA0, 0x04};
	BYTE data_in[6];
	
	send_bytes(data_out, sizeof(data_out));
	receive_bytes(data_in, 0, sizeof(data_in));

	if (data_in[2] == 0x0C)
		return 1;
	return 0;
}

int ot_ultra_poweron() // Turn on the meter
{
	return _ot_ultra_poweron();
}

void ot_ultra_read_software_info(char* info) 
{
	BYTE data_out[] = {0x02, 0x09, 0x00, 0x05, 0x0D, 0x02, 0x03, 0xDA, 0x71};
	BYTE data_in1[6], data_in2[26];
	
	// command from PC: read software version string and software creation date
	send_bytes(data_out, sizeof(data_out));
	// reply from meter: ack
	receive_bytes(data_in1, 0, sizeof(data_in1));
	// reply message from meter: S/W version string and creation date. (P02.00.0025/05/07)
	receive_bytes(data_in2, 0, sizeof(data_in2));
	// reply from PC: ack 
	send_bytes(pc_ack1, sizeof(pc_ack1));
	
	array_copy(data_in2, 6, info, 0, 17); 
	info[17] = '\0';
}

void ot_ultra_read_serial_number(char* serial) 
{
	BYTE data_out[] = {0x02, 0x12, 0x03, 0x05, 0x0B, 0x02, 
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
		0x03, 0xBA, 0x6A};
	BYTE data_in1[6], data_in2[17];

	// send command message: read serial number
	send_bytes(data_out, sizeof(data_out));
	// reply from meter: ack
	receive_bytes(data_in1, 0, sizeof(data_in1));
	// reply from meter: serial number (C176SA0O0)
	receive_bytes(data_in2, 0, sizeof(data_in2));
	// reply from pc: ack
	send_bytes(pc_ack2, sizeof(pc_ack2));

	array_copy(data_in2, 5, serial, 0, 9);
	serial[9] = '\0';
}

int ot_ultra_get_unit() 
{
	BYTE data_out[] = {0x02, 0x0E, 0x00, 0x05, 0x09, 0x02, 0x09, 
		0x00, 0x00, 0x00, 0x00, 0x03, 0xCE, 0xE7};
	BYTE data_in[12];

	// send command: read current unit settings
	send_bytes(data_out, sizeof(data_out));
	// reply from meter: ack
	receive_bytes(data_in, 0, 6);
	// reply from meter: current unit settings
	receive_bytes(data_in, 0, 12);
	// reply from pc: ack
	send_bytes(pc_ack1, sizeof(pc_ack1));

	return data_in[5]; 
}

long ot_ultra_get_date() 
{
	BYTE data_out[] = {0x02, 0x0D, 0x00, 0x05, 0x20, 0x02, 
		0x00, 0x00, 0x00, 0x00, 0x03, 0xEC, 0x61};
	BYTE data_in[12];
	U_LONG date;
	
	// send command: read RTC
	send_bytes(data_out, sizeof(data_out));
	// reply from meter: ack
	receive_bytes(data_in, 0, 6);
	// reply from meter: RTC current settings
	receive_bytes(data_in, 0, 12);
	// reply from pc: ack
	send_bytes(pc_ack1, sizeof(pc_ack1));

	date.byte[0] = data_in[5];
	date.byte[1] = data_in[6];
	date.byte[2] = data_in[7];
	date.byte[3] = data_in[8];

	return date.lval;
}

int ot_ultra_set_date(long time) // Set meter date
{
	BYTE data_out[] = {0x02, 0x0D, 0x03, 0x05, 0x20, 0x01, 
		0x00, 0x00, 0x00, 0x00, 0x03, 0x14, 0x33};
	BYTE data_in[12];
	WORD chksum;
	U_LONG new_time, ack_time;

	new_time.lval = time;

	data_out[9] = new_time.byte[3];
	data_out[8] = new_time.byte[2];
	data_out[7] = new_time.byte[1];
	data_out[6] = new_time.byte[0];

	chksum = crc_calculate_crc(0xffff, data_out, 11);
	data_out[11] = byte0(chksum);
	data_out[12] = byte1(chksum);

	// send command: write RTC
	send_bytes(data_out, sizeof(data_out));
	// reply from meter: ack
	receive_bytes(data_in, 0, 6);
	// reply from meter: RTC current settings
	receive_bytes(data_in, 0, 12);
	// reply from pc: ack
	send_bytes(pc_ack2, sizeof(pc_ack2));

	ack_time.byte[0] = data_in[5];
	ack_time.byte[1] = data_in[6];
	ack_time.byte[2] = data_in[7];
	ack_time.byte[3] = data_in[8];

	if (ack_time.lval == new_time.lval)
		return 1;
	return 0;
}

int ot_ultra_get_nrecords() // Get number of glucose records in the meter memory
{
	BYTE data_out[] = {0x02, 0x0A, 0x00, 0x05, 0x1F, 
		0xF5, 0x01, 0x03, 0x38, 0xAA};
	BYTE data_in[10];
	// send command: get number of records
	send_bytes(data_out, sizeof(data_out));
	// reply from meter: ack
	receive_bytes(data_in, 0, 6);
	// reply from meter: number of records
	receive_bytes(data_in, 0, 10);
	// reply from pc: ack
	send_bytes(pc_ack1, sizeof(pc_ack1));

	return ((WORD)data_in[5] | ((WORD)data_in[6] << 8)); 
}

void ot_ultra_read_record(WORD offset, RECORD* precord) // Read the offset-th record
{
	BYTE data_out[] = {0x02, 0x0A, 0x03, 0x05, 0x1F, 
		0x00, 0x00, // offset
		0x03, 0x4B, 0x5F};
	BYTE data_in1[6], data_in2[16];
	WORD chksum;
	U_LONG time, value;

	data_out[5] = byte0(offset);
	data_out[6] = byte1(offset);

	chksum = crc_calculate_crc(0xffff, data_out, 8);
	data_out[8] = byte0(chksum);
	data_out[9] = byte1(chksum);

	// send command: read glucose record
	send_bytes(data_out, sizeof(data_out));
	// reply from meter: ack
	receive_bytes(data_in1, 0, sizeof(data_in1));
	// reply from meter: glucose record
	receive_bytes(data_in2, 0, sizeof(data_in2));
	// reply from pc: ack
	if (array_equal(data_in1, meter_ack1, sizeof(data_in1)))
		send_bytes(pc_ack1, sizeof(pc_ack1));
	else
		send_bytes(pc_ack2, sizeof(pc_ack2));

	time.byte[0] = data_in2[5];
	time.byte[1] = data_in2[6];
	time.byte[2] = data_in2[7];
	time.byte[3] = data_in2[8];
	value.byte[0] = data_in2[ 9];
	value.byte[1] = data_in2[10];
	value.byte[2] = data_in2[11];
	value.byte[3] = data_in2[12];
	
	precord->time = time.lval;
	precord->value = value.lval;
}

void ot_ultra_read_records(RECORD* records, int n) // Read glucose records
{
	int i;
	for (i = 0; i < n; i++)
		ot_ultra_read_record(i, &records[i]);
}

int ot_ultra_delete_all_records()  // Delete all glucose records in the meter
{
	BYTE data_out[] = {0x02, 0x08, 0x00, 0x05, 0x1A, 0x03, 0x56, 0xB0};
	BYTE data_in[8];

	// send command: delete all glucose records
	send_bytes(data_out, sizeof(data_out));
	// reply from meter: ack
	receive_bytes(data_in, 0, 6);
	// reply from meter: command executed 
	receive_bytes(data_in, 0, 8);
	// check
	if (data_in[3] == 0x05 && data_in[4] == 0x06) 
	{
		// if OK, ack
		send_bytes(pc_ack1, sizeof(pc_ack1));
		return 1;
	}
	return 0; // FAILED!
}