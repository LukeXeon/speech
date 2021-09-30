#include "webrtc/common_audio/vad/include/webrtc_vad.h"
#include "webrtc/common_audio/signal_processing/include/signal_processing_library.h"
#include <stdlib.h>
#include "webrtc/common_audio/include/typedefs.h"
#include <jni.h>

#define AGGRESSIVENESS 3

JNIEXPORT jint JNICALL
Java_open_source_speech_Vad_nStart(JNIEnv *env, jclass clazz, jlongArray ref) {
    int ret_state;
    VadInst *handle = NULL;
    ret_state = WebRtcVad_Create(&handle);
    if (ret_state == -1) return -1;
    ret_state = WebRtcVad_Init(handle);
    if (ret_state == -1) return -2;
    ret_state = WebRtcVad_set_mode(handle, AGGRESSIVENESS);
    if (ret_state == -1) return -3;
    (*env)->SetLongArrayRegion(env, ref, 0, 1, (jlong *) &handle);
    return ret_state;
}

JNIEXPORT void JNICALL
Java_open_source_speech_Vad_nFeed(JNIEnv *env, jclass clazz, jlong instance, jobject buffer) {
    jshort *arrayElements = (jshort *) (*env)->GetDirectBufferAddress(env, buffer);
    jlong size = (*env)->GetDirectBufferCapacity(env, buffer);
    WebRtcVad_Process((VadInst *) instance, 16000, arrayElements, size);
}

JNIEXPORT void JNICALL
Java_open_source_speech_Vad_nStop(JNIEnv *env, jclass clazz, jlong instance) {
    WebRtcVad_Free((VadInst *) instance);
}