#! /bin/bash

JAVA_INCLUDE=/usr/lib/jvm/java-1.8.0/include 
gcc -shared -O3 -fPIC unix.c -I$JAVA_INCLUDE -I$JAVA_INCLUDE/linux -o libunix.so

# MACHINE_BYTE_ORDER=1 corresponds to 'little endian'
gcc -shared -O3 -fPIC -DMACHINE_BYTE_ORDER=1 copy*.c -I$JAVA_INCLUDE -I$JAVA_INCLUDE/linux -o libnativedata.so
