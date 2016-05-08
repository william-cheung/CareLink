/* Test Program */

#include <stdio.h>
#include <time.h>
#include <dlfcn.h>
#include "diabetes_plugin.h"

int main ( void ) {
	void *handle;
	Plugin module, *(*r_module)( void );
	Gluco_Plugin_1 module_gluco, *(*r_spec)( void );
	struct gluco_entries read_out;
	char *error;

	handle = dlopen("./ot_ultra.so", RTLD_LAZY);
	if (!handle) {
        	fputs (dlerror(), stderr);
		exit(1);
	}

	r_module = dlsym(handle, "return_plugin");
	if ((error = dlerror()) != NULL)  {
		fprintf (stderr, "%s\n", error);
		exit(1);
	}


	printf ("Version of Plugin: %d\nType of Plugin: %d\nPlugin: %s\n\n",
		(*r_module)()->type_version, (*r_module)()->type_of_plugin, (*r_module)()->plugin_name);
	if ((*r_module)()->type_version == 1 && (*r_module)()->type_of_plugin == 1)
		printf ("Plugin Type/Version correct... loading and initalizing module\n");

	r_spec = dlsym(handle, "return_module_spec");
	if ((error = dlerror()) != NULL)  {
		fprintf (stderr, "%s\n", error);
		exit(1);
	}

	printf ("%s v%s by %s\n",
		(*r_spec)()->family_name, (*r_spec)()->version_string, (*r_spec)()->author);

	*(*r_spec)()->port = 1;
	(*r_spec)()->init();
	if ((*r_spec)()->poweron() != 1) {
		printf ("Unable to turn on %s\n", (*r_spec)()->plugin_name);
		exit(1);
	}

	read_out = (*r_spec)()->get_entries();

	printf ("Got back %d test results\n", read_out.num_entries);

	printf ("Newest: %d @ %s\n", read_out.gluc[0], ctime(&read_out.date[0]));
	printf ("Oldest: %d @ %s\n", read_out.gluc[read_out.num_entries-1],
		ctime(&read_out.date[read_out.num_entries-1]));

	printf ("Time %d \n", (*r_spec)()->get_clock());
	(*r_spec)()->cleanup();
	printf ("Cleaned up and exiting...\n");
	dlclose(handle);
	return 1;
}
