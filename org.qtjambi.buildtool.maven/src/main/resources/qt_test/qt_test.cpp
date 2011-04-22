/*
 *
 *
 *
 */

#include <time.h>

#include <QtCore>

#include "qt_test.h"

int
my_function(int a, int b)
{
	int r;
	r = (a * b) + 42;
	return r;
}

int
main(int argc, char *argv[])
{
	Q_INIT_RESOURCE(qt_test);
	int retval = 1;

	if(argc > 0) {
		const char *v;
		v = argv[1];
		printf("argv[1]=%s\n", v);

		retval = 0;
	}

	MyApplication myApp(argc, argv);
	myApp.setApplicationName("qt_test");
	return myApp.exec();
}
