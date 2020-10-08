package org.ghostsinthelab.apps.guilelessbopomofo

class ChewingEngine constructor(dataPath: String) {
    // Chewing context pointer, represent its address as a Long
    val context: Long

    init {
        System.loadLibrary("chewing")
        context = chewingNew(dataPath)
    }

    // Chewing API JNIs
    private external fun chewingNew(dataPath: String): Long
    external fun delete(chewingCtx: Long = context)
    external fun free(resourcePtr: Long)
    external fun getChiEngMode(chewingCtx: Long = context): Int
    external fun setSelKey(selKeys: List<Int>, length: Int, chewingCtx: Long = context)
    external fun getSelKey(chewingCtx: Long = context): Long
    external fun setMaxChiSymbolLen(length: Int, chewingCtx: Long = context)
    external fun setCandPerPage(candidates: Int, chewingCtx: Long = context)
    external fun setPhraseChoiceRearward(boolean: Boolean, chewingCtx: Long = context)
    external fun handleDefault(key: Char, chewingCtx: Long = context)
    external fun handleEnter(chewingCtx: Long = context)
    external fun handleSpace(chewingCtx: Long = context)
    external fun handleLeft(chewingCtx: Long = context)
    external fun handleRight(chewingCtx: Long = context)
    external fun commitString(chewingCtx: Long = context): String
    external fun commitPreeditBuf(chewingCtx: Long = context): Int
    external fun candOpen(chewingCtx: Long = context): Int
    external fun candTotalChoice(chewingCtx: Long = context): Int
    external fun candChooseByIndex(index: Int, chewingCtx: Long = context): Int
    external fun candListHasNext(chewingCtx: Long = context): Boolean
    external fun candListNext(chewingCtx: Long = context): Boolean
}