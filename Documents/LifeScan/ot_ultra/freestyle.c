/*
 *  AUTHOR: William Cheung (mymailbox_blg@sina.com)
 *
 *
 *  Sep. 2013: First version
 *  Sep. 2013: First release
 *
 * ------------------------------------------------------------------------- *
 * last modified by $Author: William Cheung
 *                  $Revision: 0.1
 * $Date: 2013/09/26
 * ------------------------------------------------------------------------- *
 */

#define _POSIX_SOURCE	1

# define CRTSCTS  020000000000		/* flow control */

#include <stdarg.h>
#include <unistd.h>
#include <sys/time.h>
#include <time.h>
#include <termio.h>
#include <stdio.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/signal.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <stdlib.h>
#include <string.h>
#include "chars.h"
#include "diabetes_plugin.h"

#define BAUDRATE	B9600
#define DEBUG	FALSE
#define SILENT

/* HackHack Freestyle Lite won't respond if chars are sent at once */
int	sec = 100;		/* Time in microseconds to wait between sending of chars */
int	fd_ComX;		    /* file handle for serial IO */
int	ComX;
static int port;
struct termios oldtio, newtio;

void SendCommand(const char * );
char *ReadLine();
int  freestyle_poweron( void );
struct gluco_entries freestyle_get_entries( void );
void freestyle_get_serial( char** );
void freestyle_get_software( char ** );
long freestyle_get_clock( void );
void freestyle_set_clock( long );
static int freestyle_get_unit( void );
void freestyle_set_unit( int );
void freestyle_zero_gluc( void );
void freestyle_cleanup( void );
void freestyle_init ( void );
Plugin *return_plugin( void );
Gluco_Plugin_1 *return_module_spec( void );

static Plugin freestyle_p;
static Gluco_Plugin_1 freestyle_gp;

static Plugin freestyle_p =
{
	1,
	1,
	"Abbott Freestyle"
};

static Gluco_Plugin_1 freestyle_gp =
{
	"Abbott Freestyle",
	"Abbott Freestyle enabled glucometers",
	"0.1",
	"William Cheung",
	&(port),
	freestyle_init,
	freestyle_poweron,
	freestyle_get_serial,
	freestyle_get_software,
	freestyle_get_clock,
	freestyle_set_clock,
	freestyle_get_unit,
	freestyle_set_unit,
	freestyle_get_entries,
	freestyle_zero_gluc,
	freestyle_cleanup
};

void freestyle_init ( void ) {
	const char *SerialDevice[] = {
		"/dev/ttyS0",
		"/dev/ttyS1",
		"/dev/ttyS2",
		"/dev/ttyS3"
	};


	ComX = port;

#ifndef SILENT
	printf( "\n\t used Port COM%d: \n\n", ComX);
#endif

   /*
    Open modem device for reading and writing and not as controlling tty
    because we don't want to get killed if linenoise sends CTRL-C.
   */

	switch (ComX) {
		case 1:
		case 2:
		case 3:
		case 4:
			fd_ComX = open(SerialDevice[ComX-1], O_RDWR | O_NOCTTY);
			if( fd_ComX <0 ) {
				perror(SerialDevice[ComX-1]);
				exit(-1);
			}
			break;

		default:
		/* message and exit	*/
			fd_ComX = -1;
			break;
	}

	tcgetattr(fd_ComX,&oldtio);	/* save current modem settings */

  /*
    Set bps rate and hardware flow control and 8n1 (8bit,no parity,1 stopbit).
    Also don't hangup automatically and ignore modem status.
    Finally enable receiving characters.
   */
	newtio.c_cflag = BAUDRATE | CRTSCTS | CS8 | CLOCAL | CREAD;

   /*
    Ignore bytes with parity errors and make terminal raw and dumb.
    */
	newtio.c_iflag = IGNPAR;

   /*
     Raw output.
    */
	newtio.c_oflag = 0;

   /*
    Don't echo characters because if you connect to a host it or your
    modem will echo characters for you. Don't generate signals.
    */
	newtio.c_lflag = 0;

   /* blocking read until 1 char arrives */
	newtio.c_cc[VMIN]=0;		/* No. of chars: > 0 for wait	*/
	newtio.c_cc[VTIME]=0;

	tcflush(fd_ComX, TCIFLUSH);
	tcsetattr(fd_ComX, TCSANOW, &newtio);

	return;
}

void SendCommand(const char *command) {
	int x;
	char c;

	x = strlen(command);
	c = *command;
	if (c == 'D') {
		while (x > 0) {
			c = *command;
			write (fd_ComX, &c, 1);
			usleep(sec);
			x--;
			command++;
		}
	}
	else printf("Invalid Command: %s", command);
	return;
}

char *ReadLine(void) {
	int nBytes, x=0;
	char cComm, *line;
	struct timeval last, now;
	line = malloc(80);
	gettimeofday(&last, NULL);

	while ( 1 ) {
		nBytes = read(fd_ComX, &cComm, 1);
          	if ( (nBytes>0) && (cComm!=EOF) ) {
			*line = cComm;
			line++;
			x++;
			gettimeofday(&last, NULL);
			if (cComm == '\n') break;
		}
		gettimeofday(&now, NULL);
		if ((last.tv_sec + 2) < now.tv_sec) break;
	}
	line = line - x;
	return line;
}

int freestyle_poweron( void ) {
	int x = 0;

	SendCommand("DM?");
	while (strstr(ReadLine(), "?") == NULL) {
		SendCommand("DM?");
		x++;
		if (x == 5) return 0;
	}
	return 1;
}

struct gluco_entries freestyle_get_entries( void ) {
	char *got, *bet;
	struct gluco_entries save_to;
	int x = 0;
	struct tm timeptr;

	got = malloc(80);
	bet = malloc(7);
	SendCommand("DMP");
	got = ReadLine();
	got = got + 2;
	snprintf(bet, 4, "%s", got);
	save_to.num_entries = atoi(bet);
	save_to.unit_type = 1; /* OneTouch Ultra always
				  returns mg/dl */
	while (strcmp(got = ReadLine(), "")) {
		got = got + 9;
		snprintf (bet, 3, "%s", got);
		timeptr.tm_mon = atoi(bet) - 1;
		got = got + 3;
		snprintf (bet, 3, "%s", got);
		timeptr.tm_mday = atoi(bet);
		got = got + 3;
		snprintf (bet, 3, "%s", got);
		timeptr.tm_year = atoi(bet) + 100;
		got = got + 5;
		snprintf (bet, 3, "%s", got);
		timeptr.tm_hour = atoi(bet);
		got = got + 3;
		snprintf (bet, 3, "%s", got);
		timeptr.tm_min = atoi(bet);
		got = got + 3;
		snprintf (bet, 3, "%s", got);
		timeptr.tm_sec = atoi(bet);
		timeptr.tm_isdst = -1;
		save_to.date[x] = mktime(&timeptr);
		got = got + 8;
		snprintf (bet, 6, "%s", got);
		if (!strcmp(bet, " HIGH"))
			save_to.gluc[x] = 1002; /* Untested */
		else
		{
			if (*bet == 'C')
				save_to.gluc[x] = 1003; /* Untested */
			else
				save_to.gluc[x] = atoi(bet);
		}
		got = got - 34;
		x++;
		if (x == 150 ) break; /* HackHack
		something strange with KPumpe */
	}
	free(got);
	free(bet);
	return save_to;
}

void freestyle_get_serial( char **serial ) {
	char *got;

	SendCommand("DM@");
	got = ReadLine();
	got = got + 3;
	snprintf (*serial, 10, "%s", got);
	return;
}

void freestyle_get_software( char **software ) {
	char *got;

	SendCommand("DM?");
	got = ReadLine();
	got++;
	snprintf (*software, 18, "%s", got);
	return;
}

long freestyle_get_clock( void ) {
	char *got, *bet;
	struct tm timeptr;
	long seconds;

	bet = malloc(4);
	SendCommand("DMF");
	got = ReadLine();
	got = got + 9;
	snprintf (bet, 3, "%s", got);
	timeptr.tm_mon = atoi(bet) - 1;
	got = got + 3;
	snprintf (bet, 3, "%s", got);
	timeptr.tm_mday = atoi(bet);
	got = got + 3;
	snprintf (bet, 3, "%s", got);
	timeptr.tm_year = atoi(bet) + 100;
	got = got + 5;
	snprintf (bet, 3, "%s", got);
	timeptr.tm_hour = atoi(bet);
	got = got + 3;
	snprintf (bet, 3, "%s", got);
	timeptr.tm_min = atoi(bet);
	got = got + 3;
	snprintf (bet, 3, "%s", got);
	timeptr.tm_sec = atoi(bet);
	timeptr.tm_isdst = -1;
	seconds = mktime(&timeptr);
	free(bet);
	return seconds;
}

void freestyle_set_clock( long seconds ) {
	char *got, *send;
	struct tm *timeptr;

	send = malloc(21);
	timeptr = localtime(&seconds);
	snprintf(send, 21, "DMT%02i/%02i/%02i %02i:%02i\r",
		timeptr->tm_mon + 1, timeptr->tm_mday, timeptr->tm_year - 100,
		timeptr->tm_hour, timeptr->tm_min);
	SendCommand(send);
	got = ReadLine();
	free(send);
	return;
}

int freestyle_get_unit( void ) {
	char *got;

	got = malloc(80);
	SendCommand("DMSU?");
	got = ReadLine();
	if (strstr(got, "MG/DL") != NULL) {
		free(got);
		return 1;
	}
	else {
		return 2;
		free(got);
	}
}

void freestyle_set_unit( int unit ) {
	char *got;

	got = malloc(80);
	if (unit == 1) SendCommand("DMSU0");
	else SendCommand("DMSU1");
	got = ReadLine();
	free (got);
	return;
}

/* Untested ;-) */
void freestyle_zero_gluc( void ) {

	SendCommand("DMZ");
	return;
}

void freestyle_cleanup( void ) {
	tcsetattr(fd_ComX, TCSANOW, &oldtio);	/* restore old serial setings	*/

	close(fd_ComX);

	return;
}

Plugin *return_plugin( void ) {
	return &freestyle_p;
}

Gluco_Plugin_1 *return_module_spec( void ) {
	return &freestyle_gp;
}
