package org.ghostsinthelab.apps.guilelessbopomofo.events

import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.keys.CharacterKey

class Events {
    class UpdateBuffers
    class SwitchToLayout(val layout: Layout)
    class ExitKeyboardSubLayouts
    class CommitTextInChewingCommitBuffer
    class SwitchToNextInputMethod
    class ListCandidatesForCurrentCursor
    class SendDownUpKeyEvents(val keycode: Int)
    class CandidateSelectionDone(val index: Int)
    class PrintingKeyDown(val characterKey: CharacterKey)
    class ToggleKeyboardMainLayoutMode
    class EnterKeyDownWhenBufferIsEmpty
    class ShowKeyButtonPopup(val characterKey: CharacterKey)
    class DismissKeyButtonPopup
    class DirectionKeyDown(val direction: DirectionKey)
}