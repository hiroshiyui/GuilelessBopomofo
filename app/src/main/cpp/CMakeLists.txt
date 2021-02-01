# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_C_FLAGS "-g -O2 -Wall -fPIC ${CMAKE_C_FLAGS}")

# locate libchewing
set(libchewing_src_DIR ../../../libs/libchewing)

# SQLITE
set(WITH_SQLITE3 true)
set(WITH_INTERNAL_SQLITE3 true)
set(SQLITE3_SRC_DIR ${libchewing_src_DIR}/thirdparty/sqlite-amalgamation)
include_directories(
        ${SQLITE3_SRC_DIR}
)

# Essential configurations
add_definitions(-DUNDER_POSIX -DPIC)
add_definitions(-DHAVE_CONFIG_H=1)
add_definitions(-DCHEWING_DATADIR="assets/chewing")

include(CheckCCompilerFlag)

check_c_compiler_flag(-fvisibility=hidden FVISIBILITY_HIDDEN)
if (${FVISIBILITY_HIDDEN})
    set(CMAKE_C_FLAGS "-fvisibility=hidden ${CMAKE_C_FLAGS}")
endif ()

include(CheckTypeSize)
check_type_size(uint16_t UINT16_T)

find_library(log-lib
        log
        )

include_directories(
        ${libchewing_src_DIR}/include/
        ${libchewing_src_DIR}/include/internal/
        ${libchewing_src_DIR}/src/
        ${libchewing_src_DIR}/src/porting_layer/include/
        ${libchewing_src_DIR}/src/porting_layer/include/sys/
)

add_library(common STATIC
        ${libchewing_src_DIR}/src/common/chewing-utf8-util.c
        ${libchewing_src_DIR}/src/common/key2pho.c
        )

add_library(porting_layer STATIC
        ${libchewing_src_DIR}/src/porting_layer/src/plat_mmap_posix.c
        ${libchewing_src_DIR}/src/porting_layer/src/plat_mmap_windows.c
        ${libchewing_src_DIR}/src/porting_layer/src/plat_path.c
        ${libchewing_src_DIR}/src/porting_layer/src/rpl_malloc.c
        )

add_library(chewing STATIC
        ${libchewing_src_DIR}/src/compat.c
        ${libchewing_src_DIR}/src/chewingio.c
        ${libchewing_src_DIR}/src/chewingutil.c
        ${libchewing_src_DIR}/src/choice.c
        ${libchewing_src_DIR}/src/dict.c
        ${libchewing_src_DIR}/src/mod_aux.c
        ${libchewing_src_DIR}/src/pinyin.c
        ${libchewing_src_DIR}/src/tree.c
        ${libchewing_src_DIR}/src/userphrase.c
        ${libchewing_src_DIR}/src/bopomofo.c
        )

# build main JNI library
add_library(chewing-jni SHARED
        chewing-jni.cpp
        )

# build userphrase (& sqlite) static library
if (WITH_SQLITE3)
    add_library(userphrase STATIC
            ${libchewing_src_DIR}/src/chewing-sql.c
            ${libchewing_src_DIR}/src/userphrase-sql.c
            )

    if (WITH_INTERNAL_SQLITE3)
        find_package(Threads)
        add_library(sqlite3_library STATIC
                ${SQLITE3_SRC_DIR}/sqlite3.c
                )
        target_link_libraries(sqlite3_library ${CMAKE_DL_LIBS} ${CMAKE_THREAD_LIBS_INIT})
        target_link_libraries(userphrase sqlite3_library)
    else ()
        target_link_libraries(testhelper ${SQLITE3_LIBRARY})
    endif ()
else ()
    add_library(userphrase STATIC
            ${libchewing_src_DIR}/src/hash.c
            ${libchewing_src_DIR}/src/userphrase-hash.c
            )
endif ()

# build target
target_link_libraries(chewing-jni chewing common porting_layer userphrase ${log-lib})