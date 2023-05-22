#============================================================================
#
#              Makefile to compile the 'nativedata' native library
#              Usage: nmake /f compile_windows_i386.mak
#
#============================================================================

# Visual C++ directory, for example
VCPPDIR=C:\Program Files (x86)\Microsoft Visual Studio\2017\Community\VC

# Directory where JDK is installed (We require JDK 1.6)
JAVADIR=C:\Program Files\Java\jdk1.8.0_181

# Common parent directory
PARENTDIR=F:\base

# Directory of the HDF Java Products, for example
SRCDIR=$(PARENTDIR)\c\

#===========================================================================
#   Do not make any change below this line unless you know what you do
#===========================================================================
PATH=$(PATH);$(VCPPDIR)\BIN
SRCDIR=$(SRCDIR)

VALID_PATH_SET=YES
#-------------------------------------------------------
# Test if all path is valid

!IF EXISTS("$(VCPPDIR)")
!ELSE
!MESSAGE ERROR: Visual C++ directory $(VCPPDIR) does not exist
VALID_PATH_SET=NO 
!ENDIF

!IF EXISTS("$(JAVADIR)")
!ELSE
!MESSAGE ERROR: JDK directory $(JAVADIR) does not exist
VALID_PATH_SET=NO 
!ENDIF

!IF EXISTS("$(SRCDIR)")
!ELSE
!MESSAGE ERROR: C source directory $(SRCDIR) does not exist
VALID_PATH_SET=NO 
!ENDIF

#-------------------------------------------------------


!IF "$(VALID_PATH_SET)" == "YES"

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

INTDIR=.\nativedata\Release
OUTDIR=$(SRCDIR)\lib\win

INCLUDES =  \
	"$(JAVADIR)\include\jni.h" \
	"$(JAVADIR)\include\win32\jni_md.h"


ALL : "$(OUTDIR)\nativedata.dll"

"$(INTDIR)" :
    if not exist "$(INTDIR)/$(NULL)" mkdir "$(INTDIR)"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /W3 /EHsc /O2 /I "$(JAVADIR)\include" /I "$(JAVADIR)\include\win32" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "MACHINE_BYTE_ORDER=1" /Fp"$(INTDIR)\nativedata.pch" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

MTL=midl.exe
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32 
RSC=rc.exe
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(INTDIR)\nativedata.bsc" 
BSC32_SBRS= \
	
LINK=link.exe
LINK_FLAGS=/nologo /dll /nodefaultlib:msvcrt /incremental:no /pdb:"$(INTDIR)\nativedata.pdb" /machine:x64 /out:"$(OUTDIR)\nativedata.dll" /implib:"$(INTDIR)\nativedata.lib" 
LINK_OBJS= \
	"$(INTDIR)\copyCommon.obj" \
	"$(INTDIR)\copyByteDouble.obj" \
	"$(INTDIR)\copyByteFloat.obj" \
	"$(INTDIR)\copyByteInt.obj" \
	"$(INTDIR)\copyByteLong.obj" \
	"$(INTDIR)\copyByteShort.obj" \
	"$(INTDIR)\copyByteChar.obj"


"$(OUTDIR)\nativedata.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK_OBJS)
    $(LINK) @<<
  $(LINK_FLAGS) $(LINK_OBJS)
<<


SOURCE=$(SRCDIR)\copyCommon.c

"$(INTDIR)\copyCommon.obj" : $(SOURCE) $(INCLUDES) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)


SOURCE=$(SRCDIR)\copyByteDouble.c

"$(INTDIR)\copyByteDouble.obj" : $(SOURCE) $(INCLUDES) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)

SOURCE=$(SRCDIR)\copyByteFloat.c

"$(INTDIR)\copyByteFloat.obj" : $(SOURCE) $(INCLUDES) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)

SOURCE=$(SRCDIR)\copyByteInt.c

"$(INTDIR)\copyByteInt.obj" : $(SOURCE) $(INCLUDES) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)

SOURCE=$(SRCDIR)\copyByteLong.c

"$(INTDIR)\copyByteLong.obj" : $(SOURCE) $(INCLUDES) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)

SOURCE=$(SRCDIR)\copyByteShort.c

"$(INTDIR)\copyByteShort.obj" : $(SOURCE) $(INCLUDES) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)

SOURCE=$(SRCDIR)\copyByteChar.c

"$(INTDIR)\copyByteChar.obj" : $(SOURCE) $(INCLUDES) "$(INTDIR)"
	$(CPP) $(CPP_PROJ) $(SOURCE)

CLEAN :
	-@erase "$(INTDIR)\copyCommon.obj"
	-@erase "$(INTDIR)\copyByteDouble.obj"
	-@erase "$(INTDIR)\copyByteFloat.obj"
	-@erase "$(INTDIR)\copyByteInt.obj"
	-@erase "$(INTDIR)\copyByteLong.obj"
	-@erase "$(INTDIR)\copyByteShort.obj"
	-@erase "$(INTDIR)\copyByteChar.obj"
	-@erase "$(INTDIR)\vc90.idb"
	-@erase "$(INTDIR)\nativedata.exp"
	-@erase "$(INTDIR)\nativedata.lib"
	-@erase "$(OUTDIR)\nativedata.dll"

!ENDIF
