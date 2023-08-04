package org.ghostsinthelab.apps.guilelessbopomofo.events

import org.ghostsinthelab.apps.guilelessbopomofo.KeyboardPanel
import org.ghostsinthelab.apps.guilelessbopomofo.keys.CharacterKey

class Events {
    class UpdateBuffers
    class SwitchToKeyboardLayout(val keyboardLayout: KeyboardPanel.KeyboardLayout)
    class ExitKeyboardSubLayouts
    class CommitTextInChewingCommitBuffer
    class SwitchToNextInputMethod
    class ListCandidatesForCurrentCursor
    class SendDownUpKeyEvents(val keycode: Int)
    class CandidateSelectionDone(val index: Int)
    class PrintingKeyDown(val characterKey: CharacterKey)
}