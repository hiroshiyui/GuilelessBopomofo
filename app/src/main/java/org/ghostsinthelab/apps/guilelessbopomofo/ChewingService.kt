package org.ghostsinthelab.apps.guilelessbopomofo

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo

class ChewingService : InputMethodService() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateCandidatesView(): View {
        return super.onCreateCandidatesView()
    }

    override fun onCreateInputView(): View {
        return super.onCreateInputView()
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
}