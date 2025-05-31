package org.ghostsinthelab.apps.guilelessbopomofo.events

import org.ghostsinthelab.apps.guilelessbopomofo.enums.DirectionKey
import org.ghostsinthelab.apps.guilelessbopomofo.enums.Layout
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.CharacterKey

class Events {
    class UpdateBuffers
    class SwitchToLayout(val layout: Layout)
    class ExitKeyboardSubLayouts
    class RequestHideIme
    class CommitTextInChewingCommitBuffer
    class SwitchToNextInputMethod
    class SendDownUpKeyEvents(val keycode: Int)
    class CandidateSelectionDone(val index: Int)
    class PrintingKeyDown(val characterKey: CharacterKey)
    class ToggleKeyboardMainLayoutMode
    class EnterKeyDownWhenBufferIsEmpty
    class ShowKeyButtonPopup(val characterKey: CharacterKey)
    class DismissKeyButtonPopup
    class DirectionKeyDown(val direction: DirectionKey)
    class ToggleForceCompactLayout
    class UpdateShiftKeyState(val isActive: Boolean, val isLocked: Boolean)
}