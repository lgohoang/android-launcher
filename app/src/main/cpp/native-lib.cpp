#include <jni.h>
#include <string>
#include <iostream>

using namespace std;

extern "C" JNIEXPORT jstring JNICALL Java_com_lgo_launcher_MainActivity_Hello(JNIEnv *env, jobject thiz){
    return env->NewStringUTF("hello");
}
