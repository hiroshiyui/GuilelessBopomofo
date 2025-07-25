/*
 *     libchewing_android_jni: libchewing Android JNI
 *     Copyright (C) 2025.  YOU, Hui-Hong
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

#include <jni.h>
#include <string>
#include <android/log.h>
#include "libs/libchewing/capi/include/chewing.h"

#define LOGTAG "ChewingAndroidJni"

/*
    Chewing JNI functions
*/

/* chewing_new2() */
extern "C" JNIEXPORT jlong JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_chewingNew(
        JNIEnv *env,
        jobject,
        jstring data_path) {
    ChewingContext *ctx;

    /* build native_data_path */
    const char *native_data_path;
    native_data_path = env->GetStringUTFChars(data_path, JNI_FALSE);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "native_data_path: %s", native_data_path);

    /* build native_user_data_path */
    char user_hash_dat[] = "userhash.dat";
    /* this first 1 is for '/', the second 1 is for zero terminator */
    unsigned int native_user_data_path_len =
            strlen(native_data_path) + strlen(user_hash_dat) + 1 + 1;
    char native_user_data_path[native_user_data_path_len];
    memset(native_user_data_path, '\0', native_user_data_path_len);

    strcat(native_user_data_path, native_data_path);
    strcat(native_user_data_path, "/");
    strcat(native_user_data_path, user_hash_dat);

    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "native_user_data_path: %s",
                        native_user_data_path);

    /* create chewing context */
    ctx = chewing_new2(native_data_path, native_user_data_path, nullptr, 0);
    /* check if we do chewing_new2() successfully */
    if (chewing_Reset(ctx) == -1) {
        jclass Exception = env->FindClass("java/lang/Exception");
        env->ThrowNew(Exception, "Unable to initialize Chewing engine.");
    }
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "New Chewing context: %lld",
                        (long long) ctx);
    return (jlong) (intptr_t) ctx;
}

/* chewing_delete() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_delete(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Delete chewing context: %lld",
                        (long long) ctx);
    chewing_delete(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_setChiEngMode(
        JNIEnv *env,
        jobject,
        jint mode,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    chewing_set_ChiEngMode(ctx, mode);
}

/* chewing_get_ChiEngMode() */
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_getChiEngMode(
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
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_setSelKey(
        JNIEnv *env,
        jobject,
        jintArray selkeys,
        jint len,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint buf[len];
    (*env).GetIntArrayRegion(selkeys, 0, len, buf);

    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Set chewing selection keys from context ptr: %lld", (long long) ctx);
    chewing_set_selKey(ctx, reinterpret_cast<const int *>(buf), len);
}

/* chewing_get_selKey() */
extern "C" JNIEXPORT jintArray JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_getSelKey(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Get chewing selection keys from context ptr: %lld", (long long) ctx);
    int *get_selkey_ptr = chewing_get_selKey(ctx);
    int len = chewing_get_candPerPage(ctx);

    jint buf[len];
    jintArray ret_jintArray = env->NewIntArray(len);

    for (int i = 0; i < len; i++) {
        buf[i] = get_selkey_ptr[i];
    }
    env->SetIntArrayRegion(ret_jintArray, 0, len, buf);
    chewing_free(get_selkey_ptr);
    return ret_jintArray;
}

/* chewing_free() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_free(
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
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_setMaxChiSymbolLen(
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

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_getMaxChiSymbolLen(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint max_chi_symbol_len;
    max_chi_symbol_len = chewing_get_maxChiSymbolLen(ctx);
    return max_chi_symbol_len;
}

/* chewing_set_candPerPage() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_setCandPerPage(
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

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_getCandPerPage(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint cand_per_page;
    cand_per_page = chewing_get_candPerPage(ctx);
    return cand_per_page;
}

/* chewing_set_phraseChoiceRearward() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_setPhraseChoiceRearward(
        JNIEnv *env,
        jobject,
        jint mode,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Set phrase choice rearward to (%d) from context ptr: %lld", (jint) mode,
                        (long long) ctx);
    chewing_set_phraseChoiceRearward(ctx, mode);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_getPhraseChoiceRearward(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint phrase_choice_rearward;
    phrase_choice_rearward = chewing_get_phraseChoiceRearward(ctx);
    return phrase_choice_rearward;
}

/* chewing_handle_Default() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handleDefault(
        JNIEnv *env,
        jobject,
        jchar key,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Handle default input from context ptr: %lld", (long long) ctx);
    chewing_handle_Default(ctx, (int) key);
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handleBackspace(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Handle backspace input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_Backspace(ctx);
}

/* chewing_handle_Enter() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handleEnter(
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
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handleSpace(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Handle space key input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_Space(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handleHome(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Handle Home key input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_Home(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handleEnd(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Handle End key input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_End(ctx);
}

/* chewing_handle_Left() */
extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handleLeft(
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
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handleRight(
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
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_commitString(
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

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_commitStringStatic(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *commit_string_static = chewing_commit_String_static(ctx);
    jstring ret_jstring = env->NewStringUTF(commit_string_static);
    return ret_jstring;
}

/* chewing_commit_preedit_buf() */
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_commitPreeditBuf(
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
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candOpen(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Open candidates 'window' from context ptr: %lld", (long long) ctx);
    jint ret_jint = chewing_cand_open(ctx);
    return ret_jint;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candClose(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Close candidates 'window' from context ptr: %lld", (long long) ctx);
    jint ret_jint = chewing_cand_close(ctx);
    return ret_jint;
}

/* chewing_cand_TotalChoice() */
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candTotalChoice(
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
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candChooseByIndex(
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
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candListHasPrev(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jboolean has_prev_bool = chewing_cand_list_has_prev(ctx);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Checks whether or not (%d) there is a previous (longer) candidate list from context ptr: %lld",
                        (jint) has_prev_bool,
                        (long long) ctx);
    return has_prev_bool;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candListHasNext(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jboolean has_next_bool = chewing_cand_list_has_next(ctx);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "Checks whether or not (%d) there is a next (shorter) candidate list from context ptr: %lld",
                        (jint) has_next_bool,
                        (long long) ctx);
    return has_next_bool;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candListPrev(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint result = chewing_cand_list_prev(ctx);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "changes current candidate list to previous candidate list (result: %d) from context ptr: %lld",
                        (jint) result,
                        (long long) ctx);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candListNext(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint result = chewing_cand_list_next(ctx);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "changes current candidate list to next candidate list (result: %d) from context ptr: %lld",
                        (jint) result,
                        (long long) ctx);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candListFirst(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint result = chewing_cand_list_first(ctx);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "changes current candidate list to first candidate list (result: %d) from context ptr: %lld",
                        (jint) result,
                        (long long) ctx);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candListLast(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint result = chewing_cand_list_last(ctx);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "changes current candidate list to last candidate list (result: %d) from context ptr: %lld",
                        (jint) result,
                        (long long) ctx);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_setKBType(
        JNIEnv *env, jobject,
        jint type,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint set_keyboard_type_result;
    set_keyboard_type_result = chewing_set_KBType(ctx, type);
    return set_keyboard_type_result;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_getKBType(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint current_keyboard_type;
    current_keyboard_type = chewing_get_KBType(ctx);
    return current_keyboard_type;
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_getKBString(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    char *current_keyboard_type = chewing_get_KBString(ctx);
    jstring ret_jstring = env->NewStringUTF(current_keyboard_type);
    chewing_free(current_keyboard_type);
    return ret_jstring;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_convKBStr2Num(
        JNIEnv *env,
        jobject,
        jstring keyboard_string) {
    const char *native_keyboard_string;
    native_keyboard_string = env->GetStringUTFChars(keyboard_string, JNI_FALSE);
    unsigned int native_keyboard_string_len = strlen(native_keyboard_string) + 1;
    char native_keyboard_string_char_array[native_keyboard_string_len];
    memset(native_keyboard_string_char_array, '\0', native_keyboard_string_len);
    strcat(native_keyboard_string_char_array, native_keyboard_string);

    return chewing_KBStr2Num(native_keyboard_string_char_array);
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_bufferString(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    char *native_buffer_string = chewing_buffer_String(ctx);
    jstring ret_jstring = env->NewStringUTF(native_buffer_string);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG,
                        "outputs current pre-edit buffer from context ptr: %lld",
                        (long long) ctx);
    chewing_free(native_buffer_string);
    return ret_jstring;
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_bufferStringStatic(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *native_buffer_string_static = chewing_buffer_String_static(ctx);
    jstring ret_jstring = env->NewStringUTF(native_buffer_string_static);
    return ret_jstring;
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_bopomofoStringStatic(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *bopomofo_string_static = chewing_bopomofo_String_static(ctx);
    jstring ret_jstring = env->NewStringUTF(bopomofo_string_static);
    return ret_jstring;
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candStringByIndexStatic(
        JNIEnv *env,
        jobject,
        jint index,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *cand_string_by_index_static = chewing_cand_string_by_index_static(ctx, index);
    jstring ret_jstring = env->NewStringUTF(cand_string_by_index_static);
    return ret_jstring;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_cleanPreeditBuf(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint result = chewing_clean_preedit_buf(ctx);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_cleanBopomofoBuf(
        JNIEnv *env,
        jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint result = chewing_clean_bopomofo_buf(ctx);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_bufferLen(
        JNIEnv *env, jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    jint result = chewing_buffer_Len(ctx);
    return result;
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_setEasySymbolInput(
        JNIEnv *env,
        jobject,
        jint mode,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    chewing_set_easySymbolInput(ctx, mode);
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_setSpaceAsSelection(
        JNIEnv *env,
        jobject,
        jint mode,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    chewing_set_spaceAsSelection(ctx, mode);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_commitCheck(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    return chewing_commit_Check(ctx);
}
extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_bufferCheck(
        JNIEnv *env, jobject,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    return chewing_buffer_Check(ctx);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_getSpaceAsSelection(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    return chewing_get_spaceAsSelection(ctx);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candTotalPage(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    return chewing_cand_TotalPage(ctx);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candCurrentPage(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    return chewing_cand_CurrentPage(ctx);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candChoicePerPage(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    return chewing_cand_ChoicePerPage(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candEnumerate(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    chewing_cand_Enumerate(ctx);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candHasNext(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    return chewing_cand_hasNext(ctx);
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candString(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    char *cand_string = chewing_cand_String(ctx);
    jstring ret_jstring = env->NewStringUTF(cand_string);
    chewing_free(cand_string);
    return ret_jstring;
}

extern "C" JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_candStringStatic(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *cand_string_static = chewing_cand_String_static(ctx);
    jstring ret_jstring = env->NewStringUTF(cand_string_static);
    return ret_jstring;
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_cursorCurrent(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    return chewing_cursor_Current(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handleEsc(
        JNIEnv *env, jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Handle Escape input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_Esc(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handlePageUp(
        JNIEnv *env, jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Handle PageUp input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_PageUp(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_handlePageDown(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Handle PageDown input from context ptr: %lld",
                        (long long) ctx);
    chewing_handle_PageDown(ctx);
}

extern "C" JNIEXPORT void JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_setShapeMode(
        JNIEnv *env,
        jobject,
        jint mode,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    chewing_set_ShapeMode(ctx, mode);
}

extern "C" JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_getShapeMode(
        JNIEnv *env,
        jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Get shape mode from context ptr: %lld",
                        (long long) ctx);
    return chewing_get_ShapeMode(ctx);
}

extern "C"
JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_ack(
        JNIEnv *env, jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    return chewing_ack(ctx);
}
extern "C"
JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_version(
        JNIEnv *env, jobject thiz,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *chewing_version_string = chewing_version();
    jstring ret_jstring = env->NewStringUTF(chewing_version_string);
    return ret_jstring;
}

extern "C"
JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_configHasOption(
        JNIEnv *env, jobject thiz,
        jstring option,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *native_option = env->GetStringUTFChars(option, JNI_FALSE);
    jint ret_jint = chewing_config_has_option(ctx, native_option);
    return ret_jint;
}

extern "C"
JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_configGetInt(
        JNIEnv *env, jobject thiz,
        jstring option,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *native_option = env->GetStringUTFChars(option, JNI_FALSE);
    jint ret_jint = chewing_config_get_int(ctx, native_option);
    return ret_jint;
}

extern "C"
JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_configSetInt(
        JNIEnv *env, jobject thiz,
        jstring option, jint value,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *native_option = env->GetStringUTFChars(option, JNI_FALSE);
    jint ret_jint = chewing_config_set_int(ctx, native_option, value);
    return ret_jint;
}

extern "C"
JNIEXPORT jint JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_configSetStr(
        JNIEnv *env, jobject thiz,
        jstring option, jstring value,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *native_option = env->GetStringUTFChars(option, JNI_FALSE);
    const char *native_value = env->GetStringUTFChars(value, JNI_FALSE);
    jint ret_jint = chewing_config_set_str(ctx, native_option, (char *) native_value);
    return ret_jint;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_ghostsinthelab_apps_guilelessbopomofo_Chewing_configGetStr(
        JNIEnv *env, jobject thiz,
        jstring option,
        jlong chewing_ctx_ptr) {
    auto *ctx = reinterpret_cast<ChewingContext *>(chewing_ctx_ptr);
    const char *native_option = env->GetStringUTFChars(option, JNI_FALSE);
    char *fetched_value;
    chewing_config_get_str(ctx, native_option, &fetched_value);
    __android_log_print(ANDROID_LOG_VERBOSE, LOGTAG, "Get config string from context ptr: %lld, value: %s",
                        (long long) ctx, fetched_value);
    jstring ret_jstring = env->NewStringUTF(fetched_value);
    chewing_free(fetched_value);
    return ret_jstring;
}