#! /bin/bash

gcc -shared -O3 -D_FILE_OFFSET_BITS=64 -D_LARGEFILE64_SOURCE -D_LARGEFILE_SOURCE -I/usr/java/jdk1.8.0/include -I/usr/java/jdk1.8.0/include/linux unix.c -o libunix.so

# MACHINE_BYTE_ORDER=1 corresponds to 'little endian'
gcc -shared -O3 -fPIC -DMACHINE_BYTE_ORDER=1 copy*.c -I/usr/java/jdk1.8.0/include -I/usr/java/jdk1.8.0/include/linux -o libnativedata.so
