#include <Windows.h>
#include <stdio.h>

static HANDLE hCom;  

int init_com_port(int portno)
{
	char filename[16];
	sprintf(filename, "COM%d", portno);

	hCom = CreateFile(filename, // COM port
		GENERIC_READ | GENERIC_WRITE, 
		0, 
		NULL,
		OPEN_EXISTING, 
		0, 
		NULL);
	if(hCom == (HANDLE)-1)
		return 0;

	SetupComm(hCom, 1024, 1024);
	
	COMMTIMEOUTS TimeOuts;
	TimeOuts.ReadIntervalTimeout = 500;
	TimeOuts.ReadTotalTimeoutMultiplier = 500;
	TimeOuts.ReadTotalTimeoutConstant = 1000;
	TimeOuts.WriteTotalTimeoutMultiplier = 500;
	TimeOuts.WriteTotalTimeoutConstant = 1000;
	SetCommTimeouts(hCom,&TimeOuts); 

	DCB dcb;
	GetCommState(hCom, &dcb);
	dcb.BaudRate = 9600; 
	dcb.ByteSize = 8; 
	dcb.Parity = NOPARITY; 
	dcb.StopBits = ONESTOPBIT; 
	SetCommState(hCom, &dcb);

	PurgeComm(hCom, PURGE_TXCLEAR | PURGE_RXCLEAR);

	return 1;
}

int close_com_port()
{
	return CloseHandle(hCom);
}

int receive_bytes(BYTE* buffer, int offset, int nbytes)
{
	DWORD dwCount;
	BOOL bReadStat;
	bReadStat = ReadFile(hCom, buffer + offset, nbytes, &dwCount, NULL);
	if(!bReadStat)
		return -1;
	return (int)dwCount;
}

BYTE receive_byte()
{
	BYTE data;
	receive_bytes(&data, 0, 1);
	return data;
}

int send_bytes(BYTE* data, int len)
{
	extern void wait(int);

	COMSTAT ComStat;
	DWORD dwErrorFlags, dwCount;
	BOOL bWriteStat;

	wait(40);
	
	ClearCommError(hCom, &dwErrorFlags, &ComStat);
	bWriteStat = WriteFile(hCom, data, len, &dwCount, NULL);
	if(!bWriteStat)
		return -1;
	PurgeComm(hCom, PURGE_TXABORT |
		PURGE_RXABORT | PURGE_TXCLEAR | PURGE_RXCLEAR);
	return (int)dwCount;
}

int send_byte(BYTE data)
{
	return send_bytes(&data, 1) == 1;
}
