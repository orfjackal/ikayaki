/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class jutil_JUtil */

#ifndef _Included_jutil_JUtil
#define _Included_jutil_JUtil
#ifdef __cplusplus
extern "C" {
#endif
#undef jutil_JUtil_DRIVE_UNKNOWN
#define jutil_JUtil_DRIVE_UNKNOWN 0L
#undef jutil_JUtil_DRIVE_NO_ROOT_DIR
#define jutil_JUtil_DRIVE_NO_ROOT_DIR 1L
#undef jutil_JUtil_DRIVE_REMOVABLE
#define jutil_JUtil_DRIVE_REMOVABLE 2L
#undef jutil_JUtil_DRIVE_FIXED
#define jutil_JUtil_DRIVE_FIXED 3L
#undef jutil_JUtil_DRIVE_REMOTE
#define jutil_JUtil_DRIVE_REMOTE 4L
#undef jutil_JUtil_DRIVE_CDROM
#define jutil_JUtil_DRIVE_CDROM 5L
#undef jutil_JUtil_DRIVE_RAMDISK
#define jutil_JUtil_DRIVE_RAMDISK 6L
/*
 * Class:     jutil_JUtil
 * Method:    getConsoleChar
 * Signature: ()C
 */
JNIEXPORT jchar JNICALL Java_jutil_JUtil_getConsoleChar
  (JNIEnv *, jclass);

/*
 * Class:     jutil_JUtil
 * Method:    getLogicalDrives
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_jutil_JUtil_getLogicalDrives
  (JNIEnv *, jclass);

/*
 * Class:     jutil_JUtil
 * Method:    getFreeDiskSpace
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_jutil_JUtil_getFreeDiskSpace
  (JNIEnv *, jclass, jstring);

/*
 * Class:     jutil_JUtil
 * Method:    getDriveType
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_jutil_JUtil_getDriveType
  (JNIEnv *, jclass, jstring);

/*
 * Class:     jutil_JUtil
 * Method:    getVolumeLabel
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jutil_JUtil_getVolumeLabel
  (JNIEnv *, jclass, jstring);

/*
 * Class:     jutil_JUtil
 * Method:    setVolumeLabel
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_jutil_JUtil_setVolumeLabel
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     jutil_JUtil
 * Method:    getCurrentDirectory
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_jutil_JUtil_getCurrentDirectory
  (JNIEnv *, jclass);

/*
 * Class:     jutil_JUtil
 * Method:    setCurrentDirectory
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_jutil_JUtil_setCurrentDirectory
  (JNIEnv *, jclass, jstring);

/*
 * Class:     jutil_JUtil
 * Method:    getHwnd
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_jutil_JUtil_getHwnd
  (JNIEnv *, jclass, jstring);

/*
 * Class:     jutil_JUtil
 * Method:    setWindowMinimized
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowMinimized
  (JNIEnv *, jclass, jint);

/*
 * Class:     jutil_JUtil
 * Method:    setWindowMaximized
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowMaximized
  (JNIEnv *, jclass, jint);

/*
 * Class:     jutil_JUtil
 * Method:    setWindowRestored
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowRestored
  (JNIEnv *, jclass, jint);

/*
 * Class:     jutil_JUtil
 * Method:    setWindowRestoreEnabled
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowRestoreEnabled
  (JNIEnv *, jclass, jint, jboolean);

/*
 * Class:     jutil_JUtil
 * Method:    setWindowMoveEnabled
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowMoveEnabled
  (JNIEnv *, jclass, jint, jboolean);

/*
 * Class:     jutil_JUtil
 * Method:    setWindowSizeEnabled
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowSizeEnabled
  (JNIEnv *, jclass, jint, jboolean);

/*
 * Class:     jutil_JUtil
 * Method:    setWindowMinimizeEnabled
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowMinimizeEnabled
  (JNIEnv *, jclass, jint, jboolean);

/*
 * Class:     jutil_JUtil
 * Method:    setWindowMaximizeEnabled
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowMaximizeEnabled
  (JNIEnv *, jclass, jint, jboolean);

/*
 * Class:     jutil_JUtil
 * Method:    setWindowAlwaysOnTop
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_jutil_JUtil_setWindowAlwaysOnTop
  (JNIEnv *, jclass, jint, jboolean);

#ifdef __cplusplus
}
#endif
#endif
