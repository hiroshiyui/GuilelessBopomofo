#include <jni.h>
#include <string>
#include <chewing.h>

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

//Chewing JNI functions
extern "C" JNIEXPORT jlong JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingInit(
        JNIEnv* env,
        jobject) {
    ChewingContext *ctx;
    ctx = chewing_new2("assets/chewing", "." "/test.sqlite3", NULL, 0);
    return (long)ctx;
}