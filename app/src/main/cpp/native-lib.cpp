#include <jni.h>
#include <string>

#define LOGTAG "GBNativeLib"

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

