all: ot_ultra.so test

ot_ultra.so: ot_ultra.c diabetes_plugin.h chars.h
	gcc -g -shared ot_ultra.c -o ot_ultra.so

test: test.c diabetes_plugin.h
	gcc -rdynamic -g test.c -o test -ldl

install: ot_ultra.so
	mkdir -p /usr/lib/glucomodul/
	/usr/bin/install -c -p ot_ultra.so /usr/lib/glucomodul/ot_ultra.so
