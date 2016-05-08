/*
 *  AUTHOR: David Weisgerber (tnt@md.2y.net)
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *
 *  Feb. 2002: First version
 *  Mar. 2002: First release
 *
 * ------------------------------------------------------------------------- *
 * last modified by $Author: David Weisgerber
 *                  $Revision: 0.4
 * $Date: 2002/04/23
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

/* Only Tested with OneTouch Ultra, not with the FastTake Adapter
Series but it seems to be the same */

/* HackHack OneTouch Ultra won't respond if chars are sent at once */
int	sec=100;		/* Time in microseconds to wait between sending of chars */
int	fd_ComX;		/* file handle for serial IO	*/
int		ComX;
static int port;
struct	termios oldtio, newtio;

void SendCommand(const char * );
char *ReadLine();
int onetouch_ultra_poweron( void );
struct gluco_entries onetouch_ultra_get_entries( void );
void onetouch_ultra_get_serial( char** );
void onetouch_ultra_get_software( char ** );
long onetouch_ultra_get_clock( void );
void onetouch_ultra_set_clock( long );
static int onetouch_ultra_get_unit( void );
void onetouch_ultra_set_unit( int );
void onetouch_ultra_zero_gluc( void );
void onetouch_ultra_cleanup( void );
void onetouch_ultra_init ( void );
Plugin *return_plugin( void );
Gluco_Plugin_1 *return_module_spec( void );

static Plugin onetouch_ultra_p;
static Gluco_Plugin_1 onetouch_ultra_gp;

static Plugin onetouch_ultra_p =
{
	1,
	1,
	"OneTouch Fasttake & Ultra"
};

static Gluco_Plugin_1 onetouch_ultra_gp =
{
	"OneTouch Fasttake & Ultra",
	"OneTouch Ultra and FastTake enabled glucometers",
	"0.5",
	"David Weisgerber",
	&(port),
	onetouch_ultra_init,
	onetouch_ultra_poweron,
	onetouch_ultra_get_serial,
	onetouch_ultra_get_software,
	onetouch_ultra_get_clock,
	onetouch_ultra_set_clock,
	onetouch_ultra_get_unit,
	onetouch_ultra_set_unit,
	onetouch_ultra_get_entries,
	onetouch_ultra_zero_gluc,
	onetouch_ultra_cleanup
};

void onetouch_ultra_init ( void ) {
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

int onetouch_ultra_poweron( void ) {
	int x = 0;

	SendCommand("DM?");
	while (strstr(ReadLine(), "?") == NULL) {
		SendCommand("DM?");
		x++;
		if (x == 5) return 0;
	}
	return 1;
}

struct gluco_entries onetouch_ultra_get_entries( void ) {
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

void onetouch_ultra_get_serial( char **serial ) {
	char *got;

	SendCommand("DM@");
	got = ReadLine();
	got = got + 3;
	snprintf (*serial, 10, "%s", got);
	return;
}

void onetouch_ultra_get_software( char **software ) {
	char *got;

	SendCommand("DM?");
	got = ReadLine();
	got++;
	snprintf (*software, 18, "%s", got);
	return;
}

long onetouch_ultra_get_clock( void ) {
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

void onetouch_ultra_set_clock( long seconds ) {
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

int onetouch_ultra_get_unit( void ) {
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

void onetouch_ultra_set_unit( int unit ) {
	char *got;

	got = malloc(80);
	if (unit == 1) SendCommand("DMSU0");
	else SendCommand("DMSU1");
	got = ReadLine();
	free (got);
	return;
}

/* Untested ;-) */
void onetouch_ultra_zero_gluc( void ) {

	SendCommand("DMZ");
	return;
}

void onetouch_ultra_cleanup( void ) {
	tcsetattr(fd_ComX, TCSANOW, &oldtio);	/* restore old serial setings	*/

	close(fd_ComX);

	return;
}

Plugin *return_plugin( void ) {
	return &onetouch_ultra_p;
}

Gluco_Plugin_1 *return_module_spec( void ) {
	return &onetouch_ultra_gp;
}
