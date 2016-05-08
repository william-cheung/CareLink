#include <stdio.h>
#include <time.h>

#include "ot_ultra_protocol.h"

int main() 
{
	if (!init_com_port(3))
		printf("Failed to open serial port!\n");
	else 
	{
		if(!ot_ultra_poweron())
			printf("Failed to turn on the meter!\n");
		else
		{
			char* info = new char[32];
			int n;
			printf("Read Software Version String and Software Creation Date: ");
			ot_ultra_read_software_info(info);
			printf("%s\n", info);
			printf("Read Serial Number: ");
			ot_ultra_read_serial_number(info);
			printf("%s\n", info);
			printf("Read Date: ");
			printf("%s", ctime(ot_ultra_get_date()));
			printf("Read Unit: ");
			int unit = ot_ultra_get_unit(); 
			if (unit == 0)
				printf("mg/dL\n");
			else if (unit == 1) 
				printf("mmol/L\n");
			else
				printf("Failed!\n");
			printf("Set Date: ");
			time_t t;
			if (ot_ultra_set_date((long)time(&t)))
				printf("%s\n", ctime(ot_ultra_get_date()));
			else
				printf("Failed to set date!\n");
			delete[] info;

			printf("Read Number of Glucose Records: ");
			printf("%d\n", n = ot_ultra_get_nrecords());
			printf("Read All Records:\n");
			RECORD* records = new RECORD[n];
			ot_ultra_read_records(records, n);
			for (int i = 0; i < n; i++)
				printf("value: %4.1f mmol/L, time: %s", 
					mg_dL_to_mmol_L(records[i].value), ctime(records[i].time));
			delete[] records;
		}
		close_com_port();
	}
	return 0;
}

