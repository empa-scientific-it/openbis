/****************************************************************************
 * NCSA HDF                                                                 *
 * National Computational Science Alliance                                  *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * Center for Information Sciences and Databases, ETH Zurich, Switzerland   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * COPYING file.                                                            *
 *                                                                          *
 ****************************************************************************/

/*
 *  This module contains the implementation of all the native methods
 *  used for number conversion.  This is represented by the Java
 *  class HDFNativeData.
 *
 *  These routines convert one dimensional arrays of bytes into
 *  one-D arrays of other types (int, float, etc) and vice versa.
 *
 *  These routines are called from the Java parts of the Java-C
 *  interface.
 *
 *  ***Important notes:
 *
 *     1.  These routines are designed to be portable--they use the
 *         C compiler to do the required native data manipulation.
 *     2.  These routines copy the data at least once -- a serious
 *         but unavoidable performance hit.
 */

#define TARGET jfloat
#define TARGET_ARRAY jfloatArray
#define METHODNAMETB "copyFloatToByte"
#define FUNCTIONNAMETB Java_ch_systemsx_cisd_base_convert_NativeData_copyFloatToByte___3FI_3BIII
#define METHODNAMEBT "copyByteToFloat"
#define FUNCTIONNAMEBT Java_ch_systemsx_cisd_base_convert_NativeData_copyByteToFloat___3BI_3FIII
#define COPY_FUNC GetFloatArrayRegion
#define CHANGE_BYTE_ORDER CHANGE_BYTE_ORDER_4

#include "copyByteTarget.ctempl"
