#include <time.h>

char* ctime(long time)		// Convert time_t(long) value to string
{
	long long lltime = time;
	return ctime(&lltime);
}

void wait(int millisec)	// Wait millisec ms
{
	clock_t start = clock();
	while (clock() - start < millisec)
		;
	return;
}