package org.ghostsinthelab.apps.guilelessbopomofo

import android.view.inputmethod.EditorInfo
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EnterKeyBehavior
import org.ghostsinthelab.apps.guilelessbopomofo.utils.EnterKeyBehaviorResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class EnterKeyBehaviorResolverTest {

    /** A physical Enter: a multiple line field gets its declared action. */
    private fun resolve(
        imeOptions: Int,
        actionId: Int = EditorInfo.IME_ACTION_UNSPECIFIED,
        hasCustomActionLabel: Boolean = false,
        lineBreakInMultiLineFields: Boolean = false,
    ): EnterKeyBehavior = EnterKeyBehaviorResolver.resolve(
        imeOptions, actionId, hasCustomActionLabel, lineBreakInMultiLineFields
    )

    /** The on-screen Enter: a multiple line field gets a line break. */
    private fun resolveForSoftKey(
        imeOptions: Int,
        actionId: Int = EditorInfo.IME_ACTION_UNSPECIFIED,
        hasCustomActionLabel: Boolean = false,
    ): EnterKeyBehavior = resolve(imeOptions, actionId, hasCustomActionLabel, lineBreakInMultiLineFields = true)

    @Test
    fun noImeOptions_isAPlainEnterKey() {
        assertEquals(EnterKeyBehavior.NewLine, resolve(EditorInfo.IME_ACTION_UNSPECIFIED))
    }

    @Test
    fun actionNone_isAPlainEnterKey() {
        assertEquals(EnterKeyBehavior.NewLine, resolve(EditorInfo.IME_ACTION_NONE))
    }

    @Test
    fun noEnterActionFlag_doesNotHideADeclaredAction_fromAPhysicalEnter() {
        assertEquals(
            EnterKeyBehavior.EditorAction(EditorInfo.IME_ACTION_SEND),
            resolve(EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION)
        )
    }

    @Test
    fun noEnterActionFlag_doesNotHideACustomActionLabel_fromAPhysicalEnter() {
        assertEquals(
            EnterKeyBehavior.EditorAction(42),
            resolve(
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION,
                actionId = 42,
                hasCustomActionLabel = true,
            )
        )
    }

    @Test
    fun noEnterActionFlag_winsOverAnyDeclaredAction_onTheSoftKey() {
        assertEquals(
            EnterKeyBehavior.NewLine,
            resolveForSoftKey(EditorInfo.IME_ACTION_SEND or EditorInfo.IME_FLAG_NO_ENTER_ACTION)
        )
    }

    @Test
    fun noEnterActionFlag_winsOverACustomActionLabel_onTheSoftKey() {
        assertEquals(
            EnterKeyBehavior.NewLine,
            resolveForSoftKey(
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION,
                actionId = 42,
                hasCustomActionLabel = true,
            )
        )
    }

    @Test
    fun withoutTheFlag_theSoftKeyPerformsTheDeclaredAction() {
        assertEquals(
            EnterKeyBehavior.EditorAction(EditorInfo.IME_ACTION_SEARCH),
            resolveForSoftKey(EditorInfo.IME_ACTION_SEARCH)
        )
    }

    @Test
    fun declaredActions_areAllPerformed() {
        val actions = listOf(
            EditorInfo.IME_ACTION_GO,
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_NEXT,
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_PREVIOUS,
        )

        actions.forEach { action ->
            assertEquals(
                "action $action should be performed",
                EnterKeyBehavior.EditorAction(action),
                resolve(action)
            )
        }
    }

    @Test
    fun unrelatedImeFlags_doNotHideTheAction() {
        val imeOptions = EditorInfo.IME_ACTION_SEARCH or
                EditorInfo.IME_FLAG_NO_EXTRACT_UI or
                EditorInfo.IME_FLAG_NO_FULLSCREEN or
                EditorInfo.IME_FLAG_NAVIGATE_NEXT

        assertEquals(
            EnterKeyBehavior.EditorAction(EditorInfo.IME_ACTION_SEARCH),
            resolve(imeOptions)
        )
    }

    @Test
    fun customActionLabel_usesItsOwnActionId() {
        assertEquals(
            EnterKeyBehavior.EditorAction(1234),
            resolve(
                imeOptions = EditorInfo.IME_ACTION_UNSPECIFIED,
                actionId = 1234,
                hasCustomActionLabel = true,
            )
        )
    }

    @Test
    fun customActionLabel_winsOverTheImeOptionsAction() {
        assertEquals(
            EnterKeyBehavior.EditorAction(1234),
            resolve(
                imeOptions = EditorInfo.IME_ACTION_DONE,
                actionId = 1234,
                hasCustomActionLabel = true,
            )
        )
    }

    @Test
    fun customActionLabel_withoutAnActionId_fallsBackToTheImeOptionsAction() {
        assertEquals(
            EnterKeyBehavior.EditorAction(EditorInfo.IME_ACTION_SEND),
            resolve(
                imeOptions = EditorInfo.IME_ACTION_SEND,
                actionId = EditorInfo.IME_ACTION_UNSPECIFIED,
                hasCustomActionLabel = true,
            )
        )
    }

    @Test
    fun actionIdWithoutACustomLabel_isIgnored() {
        // actionId is only meaningful together with actionLabel.
        assertEquals(
            EnterKeyBehavior.NewLine,
            resolve(
                imeOptions = EditorInfo.IME_ACTION_UNSPECIFIED,
                actionId = 1234,
                hasCustomActionLabel = false,
            )
        )
    }
}

/**
 * The [EditorInfo] overload only reads the three fields apart, but it is the one the service
 * actually calls, so pin down that it reads the right ones.
 */
class EnterKeyBehaviorResolverEditorInfoTest {

    private fun editorInfo(
        imeOptions: Int,
        actionId: Int = EditorInfo.IME_ACTION_UNSPECIFIED,
        actionLabel: CharSequence? = null,
    ): EditorInfo = EditorInfo().also {
        it.imeOptions = imeOptions
        it.actionId = actionId
        it.actionLabel = actionLabel
    }

    @Test
    fun aSearchField_performsTheSearch() {
        assertEquals(
            EnterKeyBehavior.EditorAction(EditorInfo.IME_ACTION_SEARCH),
            EnterKeyBehaviorResolver.resolve(editorInfo(EditorInfo.IME_ACTION_SEARCH), lineBreakInMultiLineFields = true)
        )
    }

    @Test
    fun aMultipleLineField_insertsALineBreak_fromTheSoftKey() {
        // What TextView hands us for android:inputType="textMultiLine".
        assertEquals(
            EnterKeyBehavior.NewLine,
            EnterKeyBehaviorResolver.resolve(
                editorInfo(EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_ENTER_ACTION),
                lineBreakInMultiLineFields = true,
            )
        )
    }

    @Test
    fun aMultipleLineField_performsItsDeclaredAction_fromAPhysicalEnter() {
        // A line break is Shift+Enter or Alt+Enter there.
        assertEquals(
            EnterKeyBehavior.EditorAction(EditorInfo.IME_ACTION_DONE),
            EnterKeyBehaviorResolver.resolve(
                editorInfo(EditorInfo.IME_ACTION_DONE or EditorInfo.IME_FLAG_NO_ENTER_ACTION),
                lineBreakInMultiLineFields = false,
            )
        )
    }

    @Test
    fun aCustomActionLabel_performsItsOwnActionId() {
        assertEquals(
            EnterKeyBehavior.EditorAction(7),
            EnterKeyBehaviorResolver.resolve(
                editorInfo(
                    imeOptions = EditorInfo.IME_ACTION_UNSPECIFIED,
                    actionId = 7,
                    actionLabel = "Send",
                ),
                lineBreakInMultiLineFields = true,
            )
        )
    }

    @Test
    fun anUntouchedEditorInfo_insertsALineBreak() {
        assertEquals(EnterKeyBehavior.NewLine, EnterKeyBehaviorResolver.resolve(EditorInfo(), lineBreakInMultiLineFields = false))
    }
}
