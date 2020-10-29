#include <jni.h>
#include <string>
#include <chewing.h>
#include <android/log.h>

#define LOGTAG "GBNativeLib"
/*
    Chewing JNI functions
*/

/* chewing_new2() */
extern "C" JNIEXPORT jlong JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_chewingNew(
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
    return (jlong) (intptr_t) ctx;
}

/* chewing_delete() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_delete(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Delete chewing context: %lld",
                        (long long) ctx);
    chewing_delete(ctx);
}

/* chewing_get_ChiEngMode() */
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_getChiEngMode(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Get chewing Chinese/English mode from context ptr: %lld", (long long) ctx);
    return chewing_get_ChiEngMode(ctx);
}

/* chewing_set_selKey() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_setSelKey(
        JNIEnv *env,
        jobject,
        jobject selkeys,
        jint len,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const int *keys = reinterpret_cast<const int *>(selkeys);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Set chewing selection keys from context ptr: %lld", (long long) ctx);
    chewing_set_selKey(ctx, keys, len);
}

/* chewing_get_selKey() */
extern "C" JNIEXPORT jlong JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_getSelKey(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Get chewing selection keys from context ptr: %lld", (long long) ctx);
    return (jlong) chewing_get_selKey(ctx);
}

/* chewing_free() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_free(
        JNIEnv *env,
        jobject,
        jlong res_ptr) {
    auto *res = reinterpret_cast<void *>(res_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Free chewing resource (ptr): %lld",
                        (long long) res);
    chewing_free(res);
}

/* chewing_set_maxChiSymbolLen() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_setMaxChiSymbolLen(
        JNIEnv *env,
        jobject,
        jint len,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Set chewing max Chinese symbol length to %d from context ptr: %lld",
                        (jint) len, (long long) ctx);
    chewing_set_maxChiSymbolLen(ctx, len);
}

/* chewing_set_candPerPage() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_setCandPerPage(
        JNIEnv *env,
        jobject,
        jint candidates,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Set chewing candidates per page to %d from context ptr: %lld",
                        (jint) candidates, (long long) ctx);
    chewing_set_candPerPage(ctx, candidates);
}

/* chewing_set_phraseChoiceRearward() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_setPhraseChoiceRearward(
        JNIEnv *env,
        jobject,
        jboolean boolean,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Set phrase choice rearward to (%d) from context ptr: %lld", (jint) boolean,
                        (long long) ctx);
    chewing_set_phraseChoiceRearward(ctx, boolean);
}

/* chewing_handle_Default() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_handleDefault(
        JNIEnv *env,
        jobject,
        jchar key,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Handle default input '%c' from context ptr: %lld", key, (long long) ctx);
    chewing_handle_Default(ctx, (int) key);
}

/* chewing_handle_Enter() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_handleEnter(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Handle enter input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_Enter(ctx);
}

/* chewing_handle_Space() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_handleSpace(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Handle space input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_Space(ctx);
}

/* chewing_handle_Left() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_handleLeft(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Handle left input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_Left(ctx);
}

/* chewing_handle_Right() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_handleRight(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Handle right input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_Right(ctx);
}

/* chewing_commit_String() */
extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_commitString(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Commit string from context ptr: %lld",
                        (long long) ctx);
    char *commit_string = chewing_commit_String(ctx);
    jstring ret_jstring = env->NewStringUTF(commit_string);
    chewing_free(commit_string);
    return ret_jstring;
}

/* chewing_commit_preedit_buf() */
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_commitPreeditBuf(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Commit pre-edit buffer from context ptr: %lld", (long long) ctx);
    jint ret_jint;
    ret_jint = chewing_commit_preedit_buf(ctx);
    return ret_jint;
}

/* chewing_cand_open() */
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_candOpen(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Open candidates from context ptr: %lld", (long long) ctx);
    jint ret_jint;
    ret_jint = chewing_cand_open(ctx);
    return ret_jint;
}

/* chewing_cand_TotalChoice() */
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_candTotalChoice(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint total_choice;
    total_choice = chewing_cand_TotalChoice(ctx);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Count of total candidates (%d) from context ptr: %lld",
                        (jint) total_choice, (long long) ctx);
    return total_choice;
}

/* chewing_cand_choose_by_index() */
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_candChooseByIndex(
        JNIEnv *env,
        jobject,
        jint index,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Choose candidates by index (%d) from context ptr: %lld", (jint) index,
                        (long long) ctx);
    jint ret_jint;
    ret_jint = chewing_cand_choose_by_index(ctx, index);
    return ret_jint;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_candListHasNext(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jboolean has_next_bool;
    has_next_bool = chewing_cand_hasNext(ctx);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Checks whether or not (%d) there is a next (shorter) candidate list from context ptr: %lld",
                        (jint) has_next_bool,
                        (long long) ctx);
    return has_next_bool;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_ChewingEngine_candListNext(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jboolean success_next_bool;
    success_next_bool = chewing_cand_list_next(ctx);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "changes current candidate list to next candidate list (result: %d) from context ptr: %lld",
                        (jint) success_next_bool,
                        (long long) ctx);
    return success_next_bool;
}