Lifescan OneTouch Ultra Module, Test & Specifications extended to be used for the FastTake Adapter Series too.

by David Weisgerber <tnt@md.2y.net>


=====================
====================== COMPILATION ====================================
====================



Just type "make" to build ot_ultra.so (the Module)
and test (the test program for the module)


I didn't test it out on any other system than mine
but I am sure it will compile on every system where
 gcc is 
installed. I am using gcc v2.96



========================================
=== INSTALLATION ===================================
====================



Just type "make install" and it will be installed 
to /usr/lib/glucomodul/ where KPumpe will find the
module.



==========================================
=== USING ==========================================
==================



After compilation you can type "./test" to test the
module. "test" is compiled to use the 1st COM-Port
 
for your OneTouch Ultra or FastTake Adapter,
so be sure to have it plugged in the 1st COM-Port
before using the 
test program.

You can also change the port used by changing the code
of "test.c".

However, test is only a sample 
program to show how to
 use the OneTouch Ultra Module.



========================================
=== SPECIFICATIONS =================================
====================



For building further modules for other Glucose Meters
 I've made up some specifications how to build modules.

The most definitions are given in diabetes_plugin.h 
but I will also describe some here:

 

First there is a struct called "Plugin" which will hold
 some basic information. This struct will be given back
 
by the function "return_plugin". So when a program loads
 the module it will first fetch this struct and look at 
the 
members "type_version" and "type_of_plugin" to check whether
 compatibility is given or not. 

When it is 
compatible, it will fetch struct "Gluco_Plugin_x"
where x is the version returned by type_version. This struct 

is returned by "return_module_spec"; it contains all pointers 
to all functions which you need to control your 
glucometer.

You only have to load 2 symbols from the module, so I think 
that's quite easy enough to have a 
powerful plugin system.



*********
ATTENTION
*********
 
This only the first test version, further enhancements will
follow.



===========================================
=== LICENSE ========================================
==================


It's all GPL'd (www.gnu.org). I hope everybody has a 
copy of the License on his computer. If not go and 
fetch one.
