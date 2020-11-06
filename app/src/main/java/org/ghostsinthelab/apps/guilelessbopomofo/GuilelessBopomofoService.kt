package org.ghostsinthelab.apps.guilelessbopomofo

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo

class GuilelessBopomofoService : InputMethodService(), View.OnClickListener {
    val LOGTAG = "Service"
    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateCandidatesView(): View {
        val myCandidatesView: View = layoutInflater.inflate(R.layout.candidates_layout, null)
        return myCandidatesView
    }

    override fun onCreateInputView(): View {
        Log.d(LOGTAG, "onCreateInputView()")
        val myKeyboardView: View = layoutInflater.inflate(R.layout.keyboard_layout, null)
        return myKeyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}