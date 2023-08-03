package org.ghostsinthelab.apps.guilelessbopomofo.events

import org.ghostsinthelab.apps.guilelessbopomofo.KeyboardPanel

class Events {
    class UpdateBuffers
    class SwitchToKeyboardLayout(val keyboardLayout: KeyboardPanel.KeyboardLayout)
    class ExitKeyboardSubLayouts
    class CommitTextInChewingCommitBuffer
    class SwitchToNextInputMethod
    class ListCandidatesForCurrentCursor
    class SendDownUpKeyEvents(val keycode: Int)
}