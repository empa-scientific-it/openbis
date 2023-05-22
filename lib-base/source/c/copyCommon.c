/****************************************************************************
 * NCSA HDF                                                                 *
 * National Comptational Science Alliance                                   *
 * University of Illinois at Urbana-Champaign                               *
 * 605 E. Springfield, Champaign IL 61820                                   *
 *                                                                          *
 * For conditions of distribution and use, see the accompanying             *
 * COPYING file.                                                            *
 *                                                                          *
 ****************************************************************************/

/*
 *  This is a utility program used by native code to generate Java exceptions.
 *
 */
#ifdef __cplusplus
extern "C" {
#endif

#include <stdio.h>
#include "jni.h"

/*
 *   public static native boolean isLittleEndian();
 */
JNIEXPORT jboolean JNICALL Java_ch_systemsx_cisd_base_convert_NativeData_isLittleEndian
(JNIEnv *env,
  jclass clss
  )  
{
    return MACHINE_BYTE_ORDER == 1;
}

/*
 *  A fatal error in a JNI call
 *  Create and throw an 'InternalError'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5JNIFatalError( JNIEnv *env, char *functName)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass("java/lang/InternalError");
#else
    jc = (*env)->FindClass(env, "java/lang/InternalError");
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = env->NewStringUTF(functName);
#else
    str = (*env)->NewStringUTF(env,functName);
#endif
    args[0] = (char *)str;
    args[1] = 0;
#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, (jvalue *)args );

    rval = env->Throw( (jthrowable) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  JNIFatal: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 *  A NULL argument in an HDF5 call
 *  Create and throw an 'NullPointerException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5nullArgument( JNIEnv *env, char *functName)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass("java/lang/NullPointerException");
#else
    jc = (*env)->FindClass(env, "java/lang/NullPointerException");
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = env->NewStringUTF(functName);
#else
    str = (*env)->NewStringUTF(env,functName);
#endif
    args[0] = (char *)str;
    args[1] = 0;
#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, (jvalue *)args );

    rval = env->Throw((jthrowable) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif

    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  NullPointer: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 *  A bad argument in an HDF5 call
 *  Create and throw an 'IllegalArgumentException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5badArgument( JNIEnv *env, char *functName)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass("java/lang/IllegalArgumentException");
#else
    jc = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = env->NewStringUTF(functName);
#else
    str = (*env)->NewStringUTF(env,functName);
#endif
    args[0] = (char *)str;
    args[1] = 0;
#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, (jvalue *)args );

    rval = env->Throw((jthrowable) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  BadArgument: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 *  An Index-out-of-bounds error argument in an HDF5 call
 *  Create and throw an 'IllegalArgumentException'
 *
 *  Note:  This routine never returns from the 'throw',
 *  and the Java native method immediately raises the
 *  exception.
 */
jboolean h5indexOutOfBounds( JNIEnv *env, char *functName)
{
    jmethodID jm;
    jclass jc;
    char * args[2];
    jobject ex;
    jstring str;
    int rval;

#ifdef __cplusplus
    jc = env->FindClass("java/lang/IndexOutOfBoundsException");
#else
    jc = (*env)->FindClass(env, "java/lang/IndexOutOfBoundsException");
#endif
    if (jc == NULL) {
        return JNI_FALSE;
    }
#ifdef __cplusplus
    jm = env->GetMethodID(jc, "<init>", "(Ljava/lang/String;)V");
#else
    jm = (*env)->GetMethodID(env, jc, "<init>", "(Ljava/lang/String;)V");
#endif
    if (jm == NULL) {
        return JNI_FALSE;
    }

#ifdef __cplusplus
    str = env->NewStringUTF(functName);
#else
    str = (*env)->NewStringUTF(env,functName);
#endif
    args[0] = (char *)str;
    args[1] = 0;
#ifdef __cplusplus
    ex = env->NewObjectA ( jc, jm, (jvalue *)args );

    rval = env->Throw((jthrowable) ex );
#else
    ex = (*env)->NewObjectA ( env, jc, jm, (jvalue *)args );

    rval = (*env)->Throw(env, ex );
#endif
    if (rval < 0) {
        fprintf(stderr, "FATAL ERROR:  BadArgument: Throw failed\n");
        return JNI_FALSE;
    }

    return JNI_TRUE;
}
#ifdef __cplusplus
}
#endif
