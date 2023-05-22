#! /bin/bash

gcc -m64 -mmacosx-version-min=10.11 -dynamiclib -D_DARWIN_USE_64_BIT_INODE -O3 unix.c -I/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -o libunix.jnilib

# MACHINE_BYTE_ORDER=1 corresponds to 'little endian'
gcc -m64 -mmacosx-version-min=10.11 -dynamiclib -O3 -DMACHINE_BYTE_ORDER=1 copy*.c -I/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers -o libnativedata.jnilib
