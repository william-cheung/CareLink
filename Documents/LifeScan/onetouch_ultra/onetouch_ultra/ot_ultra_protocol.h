/**
 * --------------------------------------------------------- *
 *
 * $Author: William Cheung
 *
 * $Date Modified: 2013/10/22
 *
 * --------------------------------------------------------- *
 */

#ifndef __OT_ULTRA_PROTOCOL__
#define __OT_ULTRA_PROTOCOL__

typedef unsigned char	 BYTE;
typedef unsigned short	 WORD;

#define UNIT_MG_DL	0
#define UNIT_MMOL_L	1

#define MAX_STRSIZ	32

typedef struct record {
	long value;
	long time;
} RECORD;	// Glucose Record

// Timing
void		wait		(int millisec);
char*	ctime	(long time);

// Operations on serial port
int		init_com_port		(int portno);
int		close_com_port	(void);

// Commands that OneTouch UltraMini / UltraEasy supports 
int		ot_ultra_poweron			(void);
int		ot_ultra_disconnect		(void);
void		ot_ultra_read_software_info(char* info);
void		ot_ultra_read_serial_number(char* serial);
long		ot_ultra_get_date			(void);
int		ot_ultra_set_date			(long time);
int		ot_ultra_get_unit			(void);
int		ot_ultra_get_nrecords		(void);
void		ot_ultra_read_records		(RECORD* records, int n);
int		ot_ultra_delete_all_records(void); 

// Unit Conversion
float	mg_dL_to_mmol_L	(int value);

#endif	// end ot_ultra_protocol.h