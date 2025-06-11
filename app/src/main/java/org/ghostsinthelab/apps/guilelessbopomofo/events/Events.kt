package org.ghostsinthelab.apps.guilelessbopomofo.events

import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.CharacterKey

class Events {
    class CandidateSelectionDone(val index: Int)
    class CommitTextInChewingCommitBuffer
    class DirectionKeyDown(val direction: DirectionKey)
    class DismissKeyButtonPopup
    class EnterKeyDownWhenBufferIsEmpty
    class ExitKeyboardSubLayouts
    class PrintingKeyDown(val characterKey: CharacterKey)
    class RequestHideIme
    class SendDownUpKeyEvents(val keycode: Int)
    class ShowKeyButtonPopup(val characterKey: CharacterKey)
    class SwitchToLayout(val layout: Layout)
    class SwitchToNextInputMethod
    class ToggleForceCompactLayout
    class ToggleFullOrHalfWidthMode
    class ToggleKeyboardMainLayoutMode
    class UpdateBuffers
    class UpdateShiftKeyState(val isActive: Boolean, val isLocked: Boolean)
}