#include <jni.h>
#include <string>
#include <chewing.h>
#include <android/log.h>

#define LOGTAG "GBNativeLib"

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

/*
    Chewing JNI functions
*/

/* chewing_new2() */
extern "C" JNIEXPORT jlong JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingNew(
        JNIEnv *env,
        jobject,
        jstring data_path) {
    ChewingContext *ctx;

    /* build native_data_path */
    const char *native_data_path;
    native_data_path = env->GetStringUTFChars(data_path, JNI_FALSE);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "native_data_path: %s", native_data_path);

    /* build native_user_data_path */
    char user_db[] = "user.sqlite3";
    /* this first 1 is for '/', second 1 is for zero terminator */
    unsigned int native_user_data_path_len = strlen(native_data_path) + strlen(user_db) + 1 + 1;
    char native_user_data_path[native_user_data_path_len];
    memset(native_user_data_path, '\0', native_user_data_path_len);

    strcat(native_user_data_path, native_data_path);
    strcat(native_user_data_path, "/");
    strcat(native_user_data_path, user_db);

    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "native_user_data_path: %s",
                        native_user_data_path);

    /* create chewing context */
    ctx = chewing_new2(native_data_path, native_user_data_path, nullptr, 0);
    return (jlong) ctx;
}

/* chewing_delete() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingDelete(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "delete chewing context: %lld", (jlong) ctx);
    chewing_delete(ctx);
}

/* chewing_get_ChiEngMode() */
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingGetChiEngMode(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Get chewing Chinese/English mode from context ptr: %lld", (jlong) ctx);
    return chewing_get_ChiEngMode(ctx);
}

/* chewing_set_selKey() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingSetSelKey(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr,
        jobject selkeys,
        jint len) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const int *keys = reinterpret_cast<const int *>(selkeys);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Set chewing selection keys from context ptr: %lld", (jlong) ctx);
    chewing_set_selKey(ctx, keys, len);
}

/* chewing_get_selKey() */
extern "C" JNIEXPORT jlong JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingGetSelKey(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Get chewing selection keys from context ptr: %lld", (jlong) ctx);
    return (jlong) chewing_get_selKey(ctx);
}

/* chewing_free() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingFree(
        JNIEnv *env,
        jobject,
        jlong res_ptr) {
    auto *res = reinterpret_cast<void *>(res_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Free chewing resource (ptr): %lld",
                        (jlong) res);
    chewing_free(res);
}

/* chewing_set_maxChiSymbolLen() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingSetMaxChiSymbolLen(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr,
        jint len) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Set chewing max Chinese symbol length to %d from context ptr: %lld",
                        (jint) len, (jlong) ctx);
    chewing_set_maxChiSymbolLen(ctx, len);
}

/* chewing_set_candPerPage() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingSetCandPerPage(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr,
        jint candidates) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Set chewing candidates per page to %d from context ptr: %lld",
                        (jint) candidates, (jlong) ctx);
    chewing_set_candPerPage(ctx, candidates);
}

/* chewing_handle_Default() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingHandleDefault(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr,
        jint key) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    char k = key;
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Handle default input '%c' from context ptr: %lld", k, (jlong) ctx);
    chewing_handle_Default(ctx, key);
}

/* chewing_handle_Enter() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingHandleEnter(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Handle enter input from context ptr: %lld",
                        (jlong) ctx);
    chewing_handle_Enter(ctx);
}

/* chewing_commit_String() */
extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingCommitString(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Commit string from context ptr: %lld",
                        (jlong) ctx);
    char *commit_string = chewing_commit_String(ctx);
    jstring ret_jstring = env->NewStringUTF(commit_string);
    chewing_free(commit_string);
    return ret_jstring;
}

/* chewing_commit_preedit_buf() */
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_MainActivity_chewingCommitPreeditBuf(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Commit pre-edit buffer from context ptr: %lld", (jlong) ctx);
    jint ret_jint;
    ret_jint = chewing_commit_preedit_buf(ctx);
    return ret_jint;
}