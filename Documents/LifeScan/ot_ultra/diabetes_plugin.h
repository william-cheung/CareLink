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
 *                  $Revision: 0.1
 * $Date: 2002/03/03
 * ------------------------------------------------------------------------- *
 */

/* This struct will NEVER be changed */
typedef struct
{
	int type_version; 	/* Should return the internal Version of the Plugin
				Management used by the Plugin. Needed for backward
				compatibility */
	int type_of_plugin;    /* Should return the type of the plugin */
	const char *plugin_name;	/* Should return the name of the plugin if error
				occurs during load */
}
Plugin;

/* Structure for Glucometer Plugins of Version 1
	type_version = 1
	type_of_plugin = 1 */
typedef struct
{
	const char *plugin_name;		/* Same as above */
	const char *family_name;		/* Gives back a description which models are supported */
	const char *version_string;   	/* Version String */
	const char *author;			/* The Author of the Plugin */
	int *port;			/* Port where Glucometer is connected to
					0 = Auto (needn't to be able to!!!)
					1 - 4 = Serial Ports 1 to 4
					5 - 6 = Parallel Ports 1 and 2
					7 = USB
					8 = Special Port
					Filled by Main program not plugin*/
	void (*init) (void);    	/* Called when Plugin gets loaded */
	int (*poweron) (void); 	/* Powers the Glucometer on */
	void (*get_serial) (char **);	/* Returns the Serial of the Glucometer */
	void (*get_software) (char **);	/* Returns the Firmware Version of the Glucometer */
	long (*get_clock) (void);	/* Returns time in seconds since 01/01/1970 */
	void (*set_clock) (long);	/* Sets the clock. Argument is in seconds since 01/01/1970 */
	int (*get_unit) (void);		/* 1 = mg/dl && 2 = mmol/l */
	void (*set_unit) (int);		/* 1 = mg/dl && 2 = mmol/l */
	struct gluco_entries (*get_entries) (void); /* Returns the Glucometer entries */
	void (*zero_gluco) (void);	/* Deletes all entries of the Glucometer */
	void (*cleanup) (void);		/* Will be called when Module is unloaded */
}
Gluco_Plugin_1;

struct gluco_entries
{
	int unit_type; 		/* 1 = mg/dl && 2 = mmol/l */
	int num_entries; 	/* Number of entries stored */
	int gluc[1000]; 	/* Never store more than 1000 entries
				(don't know any glucometer which can save more)
				Range: 0 to 1000
				1001 = Low
				1002 = High
				1003 = Test Solution */
	long date[1000];	/* Stores time of the test in seconds since 01/01/1970 */
};
/* Todo: There must be some place where special items like 2 different Insulin, events and other
things are stored. But up to now I only have an ONETouch Ultra, so this is useless for me */
