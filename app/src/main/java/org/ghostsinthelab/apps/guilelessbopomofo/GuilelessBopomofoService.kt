/*
 * Guileless Bopomofo
 * Copyright (C) 2021 YOU, HUI-HONG
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_A
import android.view.KeyEvent.KEYCODE_ALT_LEFT
import android.view.KeyEvent.KEYCODE_BACK
import android.view.KeyEvent.KEYCODE_C
import android.view.KeyEvent.KEYCODE_DEL
import android.view.KeyEvent.KEYCODE_DPAD_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.KeyEvent.KEYCODE_ESCAPE
import android.view.KeyEvent.KEYCODE_GRAVE
import android.view.KeyEvent.KEYCODE_R
import android.view.KeyEvent.KEYCODE_SHIFT_LEFT
import android.view.KeyEvent.KEYCODE_SHIFT_RIGHT
import android.view.KeyEvent.KEYCODE_SPACE
import android.view.KeyEvent.KEYCODE_V
import android.view.KeyEvent.KEYCODE_X
import android.view.KeyEvent.KEYCODE_Z
import android.view.KeyEvent.META_SHIFT_ON
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.KeyboardLayoutBinding
import org.ghostsinthelab.apps.guilelessbopomofo.enums.SelectionKeys
import org.ghostsinthelab.apps.guilelessbopomofo.events.Events
import org.ghostsinthelab.apps.guilelessbopomofo.keys.BackspaceKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.DownKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.EnterKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.EscapeKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.LeftKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.RightKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.ShiftKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.SpaceKey
import org.ghostsinthelab.apps.guilelessbopomofo.utils.KeyEventExtension
import org.ghostsinthelab.apps.guilelessbopomofo.utils.Vibratable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.CoroutineContext

class GuilelessBopomofoService : InputMethodService(), CoroutineScope,
    SharedPreferences.OnSharedPreferenceChangeListener, KeyEventExtension {
    private val logTag = "GuilelessBopomofoSvc"
    private var imeWindowVisible: Boolean = true
    lateinit var viewBinding: KeyboardLayoutBinding

    private lateinit var sharedPreferences: SharedPreferences
    private val chewingDataFiles =
        listOf("dictionary.dat", "index_tree.dat", "pinyin.tab", "swkb.dat", "symbols.dat")

    companion object {
        val defaultHapticFeedbackStrength: Int =
            Vibratable.VibrationStrength.NORMAL.strength
        const val defaultKeyboardLayout: String = "KB_DEFAULT"
        var userHapticFeedbackStrength: Int = Vibratable.VibrationStrength.NORMAL.strength
    }

    override fun onCreate() {
        Log.d(logTag, "onCreate()")
        super.onCreate()

        EventBus.getDefault().register(this)

        // emoji2-bundled (fonts-embedded)
        EmojiCompat.init(BundledEmojiCompatConfig(this@GuilelessBopomofoService.applicationContext))

        sharedPreferences = getSharedPreferences("GuilelessBopomofoService", MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        // Initializing Chewing
        try {
            val dataPath =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageInfo(this.packageName, 0).applicationInfo.dataDir
                } else {
                    packageManager.getPackageInfo(
                        this.packageName,
                        PackageManager.PackageInfoFlags.of(0)
                    ).applicationInfo.dataDir
                }
            setupChewingData(dataPath)
            ChewingBridge.connect(dataPath)
            ChewingBridge.context.let {
                Log.d(logTag, "Chewing context ptr: $it")
            }

            if (sharedPreferences.getBoolean("user_enable_space_as_selection", true)) {
                ChewingBridge.setSpaceAsSelection(1)
            }

            if (sharedPreferences.getBoolean("user_phrase_choice_rearward", false)) {
                ChewingBridge.setPhraseChoiceRearward(true)
            }

            ChewingBridge.setChiEngMode(CHINESE_MODE)
            ChewingBridge.setCandPerPage(10)

            sharedPreferences.getString("user_candidate_selection_keys_option", "NUMBER_ROW")?.let {
                ChewingBridge.setSelKey(SelectionKeys.valueOf(it).keys, 10)
            }
        } catch (exception: Exception) {
            val exceptionDescription: String =
                getString(R.string.libchewing_init_fail, exception.message)
            Toast.makeText(applicationContext, exceptionDescription, Toast.LENGTH_LONG)
                .show()
            exception.let { e ->
                e.printStackTrace()
                e.message?.let { msg ->
                    Log.e(logTag, msg)
                }
            }
        }

        userHapticFeedbackStrength =
            sharedPreferences.getInt("user_haptic_feedback_strength", defaultHapticFeedbackStrength)

        GuilelessBopomofoServiceContext.service = this@GuilelessBopomofoService
    }


    override fun onCreateCandidatesView(): View? {
        // I want to implement my own candidate selection UI
        Log.d(logTag, "onCreateCandidatesView()")
        return null
    }

    // Disable fullscreen mode when device's orientation is landscape
    override fun onEvaluateFullscreenMode(): Boolean {
        Log.d(logTag, "onEvaluateFullscreenMode()")
        super.onEvaluateFullscreenMode()

        if (sharedPreferences.getBoolean(
                "user_fullscreen_when_in_landscape",
                true
            ) && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        ) {
            return true
        }

        if (sharedPreferences.getBoolean(
                "user_fullscreen_when_in_portrait",
                false
            ) && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        ) {
            return true
        }

        return false
    }

    override fun onCreateInputView(): View {
        Log.d(logTag, "onCreateInputView()")
        viewBinding = KeyboardLayoutBinding.inflate(this.layoutInflater)
        return viewBinding.root
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        Log.d(logTag, "onInitializeInterface()")
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        Log.d(logTag, "onEvaluateInputViewShown()")
        return true
    }

    override fun onBindInput() {
        super.onBindInput()
        Log.d(logTag, "onBindInput()")
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        Log.d(logTag, "onStartInput()")
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.d(logTag, "onStartInputView()")
        viewBinding.keyboardPanel.switchToLayout(KeyboardPanel.KeyboardLayout.MAIN)
        EventBus.getDefault().post(Events.UpdateBuffers())
    }

    override fun onFinishInput() {
        super.onFinishInput()
        Log.d(logTag, "onFinishInput()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(logTag, "onDestroy()")
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        ChewingBridge.delete()
        EventBus.getDefault().unregister(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(logTag, "onKeyDown()")

        if (!imeWindowVisible) {
            Log.d(logTag, "IME window is invisible, disable interrupt onKeyDown() events.")
            return false
        }

        // 實在是不懂既然 imeWindowVisible 又為何進到 onPrintingKeyDown() 後會發生
        // lateinit property viewBinding has not been initialized 例外？
        // 先在這邊擋掉：
        if (!this@GuilelessBopomofoService::viewBinding.isInitialized) {
            Log.d(
                logTag,
                "viewBinding has not been initialized, disable interrupt onKeyDown() events."
            )
            return false
        }

        event?.apply {
            if (this.isPrintingKey) {
                onPrintingKeyDown(this)
            } else {
                when (this.keyCode) {
                    KEYCODE_BACK -> {
                        // keep default behavior of Back key
                        return super.onKeyDown(keyCode, event)
                    }

                    KEYCODE_ALT_LEFT, KEYCODE_SHIFT_RIGHT -> {
                        // for onKeyLongPress()...
                        this.startTracking()
                        return true
                    }

                    KEYCODE_SPACE -> {
                        if (this.isShiftPressed) {
                            viewBinding.keyboardPanel.toggleMainLayoutMode()
                            return true
                        }
                        SpaceKey.performAction()
                    }

                    KEYCODE_DEL -> {
                        BackspaceKey.performAction()
                    }

                    KEYCODE_ENTER -> {
                        EnterKey.performAction()
                    }

                    KEYCODE_ESCAPE -> {
                        EscapeKey.performAction()
                    }

                    KEYCODE_DPAD_LEFT -> {
                        LeftKey.performAction()
                    }

                    KEYCODE_DPAD_RIGHT -> {
                        RightKey.performAction()
                    }

                    KEYCODE_DPAD_DOWN -> {
                        DownKey.performAction()
                    }

                    else -> {
                        return super.onKeyDown(keyCode, event)
                    }
                }
            }
        }

        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(logTag, "onKeyUp()")

        if (!imeWindowVisible) {
            Log.d(logTag, "IME window is invisible, disable interrupt onKeyUp() events.")
            return false
        }

        if (!this@GuilelessBopomofoService::viewBinding.isInitialized) {
            Log.d(
                logTag,
                "viewBinding has not been initialized, disable interrupt onKeyUp() events."
            )
            return false
        }

        event?.let {
            if (it.isPrintingKey) {
                onPrintingKeyUp()
            } else {
                return when (it.keyCode) {
                    KEYCODE_ENTER -> {
                        // DO NOTHING HERE, has been handled by EnterKey.performAction()
                        true
                    }

                    KEYCODE_SPACE -> {
                        // DO NOTHING HERE, has been handled by SpaceKey.performPhysicalAction()
                        true
                    }

                    else -> {
                        super.onKeyUp(keyCode, event)
                    }
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d(logTag, "onKeyLongPress()")

        if (!imeWindowVisible) {
            Log.d(logTag, "IME window is invisible, disable interrupt onKeyLongPress() events.")
            return false
        }

        if (!this@GuilelessBopomofoService::viewBinding.isInitialized) {
            Log.d(
                logTag,
                "viewBinding has not been initialized, disable interrupt onKeyLongPress() events."
            )
            return false
        }

        event?.let {
            when (it.keyCode) {
                KEYCODE_SHIFT_RIGHT -> {
                    if (ChewingBridge.getChiEngMode() == CHINESE_MODE) {
                        ChewingUtil.openPuncCandidates()
                        viewBinding.keyboardPanel.switchToLayout(KeyboardPanel.KeyboardLayout.CANDIDATES)
                        return true
                    } else {
                        return super.onKeyLongPress(keyCode, event)
                    }
                }

                KEYCODE_ALT_LEFT -> {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showInputMethodPicker()
                    return true
                }

                else -> {
                    return super.onKeyLongPress(keyCode, event)
                }
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        Log.d(logTag, "onWindowHidden()")
        imeWindowVisible = false
    }

    override fun onWindowShown() {
        super.onWindowShown()
        Log.d(logTag, "onWindowShown()")
        imeWindowVisible = true
    }

    fun onPrintingKeyDown(event: KeyEvent) {
        Log.d(logTag, "onPrintingKeyDown()")

        // Consider keys in NumPad
        if (event.isNumPadKey()) {
            currentInputConnection.sendKeyEvent(event)
            EventBus.getDefault().post(Events.UpdateBuffers())
            return
        }

        if (event.keyCode == KEYCODE_GRAVE && ChewingBridge.getChiEngMode() == CHINESE_MODE && !event.isShiftPressed) {
            viewBinding.keyboardPanel.switchToLayout(KeyboardPanel.KeyboardLayout.SYMBOLS)
            return
        }

        var keyPressed: Char = event.unicodeChar.toChar()

        val shiftKeyImageButton: ShiftKey? =
            viewBinding.keyboardPanel.findViewById(
                R.id.keyImageButtonShift
            )

        shiftKeyImageButton?.let {
            if (it.isActive) {
                Log.d(logTag, "Shift is active")
                currentInputConnection.sendKeyEvent(
                    KeyEvent(ACTION_DOWN, KEYCODE_SHIFT_LEFT)
                )
                keyPressed = event.getUnicodeChar(META_SHIFT_ON).toChar()
            }
        }

        if (event.isCtrlPressed) {
            currentInputConnection.apply {
                when (event.keyCode) {
                    KEYCODE_A -> {
                        performContextMenuAction(android.R.id.selectAll)
                    }

                    KEYCODE_Z -> {
                        performContextMenuAction(android.R.id.undo)
                    }

                    KEYCODE_X -> {
                        performContextMenuAction(android.R.id.cut)
                    }

                    KEYCODE_C -> {
                        performContextMenuAction(android.R.id.copy)
                    }

                    KEYCODE_V -> {
                        performContextMenuAction(android.R.id.paste)
                    }

                    KEYCODE_R -> {
                        performContextMenuAction(android.R.id.redo)
                    }
                }
            }
        } else {
            ChewingBridge.handleDefault(keyPressed)
        }

        EventBus.getDefault().post(Events.UpdateBuffers())

        shiftKeyImageButton?.let {
            if (it.isActive && !it.isLocked) {
                Log.d(logTag, "Release shift key")
                it.switchToState(ShiftKey.ShiftKeyState.RELEASED)
                currentInputConnection.sendKeyEvent(
                    KeyEvent(ACTION_UP, KEYCODE_SHIFT_LEFT)
                )
            }
        }
    }

    private fun onPrintingKeyUp() {
        Log.d(logTag, "onPrintingKeyUp()")
        // Detect if a candidate had been chosen by user
        viewBinding.keyboardPanel.let {
            if (it.currentKeyboardLayout == KeyboardPanel.KeyboardLayout.CANDIDATES) {
                if (ChewingUtil.candWindowClosed()) {
                    it.candidateSelectionDone()
                } else {
                    it.renderCandidatesLayout()
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateBuffers(event: Events.UpdateBuffers) {
        viewBinding.apply {
            launch { textViewPreEditBuffer.update() }
            launch { textViewBopomofoBuffer.update() }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSwitchToKeyboardLayout(event: Events.SwitchToKeyboardLayout) {
        viewBinding.apply {
            keyboardPanel.switchToLayout(event.keyboardLayout)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onExitKeyboardSubLayouts(event: Events.ExitKeyboardSubLayouts) {
        viewBinding.keyboardPanel.apply {
            if (this.currentKeyboardLayout in listOf(
                    KeyboardPanel.KeyboardLayout.SYMBOLS,
                    KeyboardPanel.KeyboardLayout.CANDIDATES
                )
            ) {
                this.switchToLayout(KeyboardPanel.KeyboardLayout.MAIN)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCommitTextInChewingCommitBuffer(event: Events.CommitTextInChewingCommitBuffer) {
        currentInputConnection.commitText(
            ChewingBridge.commitString(),
            1
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSwitchToNextInputMethod(event: Events.SwitchToNextInputMethod) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            switchToNextInputMethod(false)
        } else {
            // backward compatibility, support IME switch on legacy devices
            val imm =
                applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val imeToken: IBinder? = viewBinding.root.windowToken
            @Suppress("DEPRECATION")
            imm.switchToNextInputMethod(imeToken, false)
        }
    }

    private fun setupChewingData(dataPath: String) {
        // Get app data directory
        val chewingDataDir = File(dataPath)

        if (!checkChewingData(dataPath)) {
            Log.d(logTag, "Install Chewing data files.")
            installChewingData(dataPath)
        }

        // Save app version
        val chewingAppVersion =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(this.packageName, 0).versionName.toByteArray()
            } else {
                packageManager.getPackageInfo(
                    this.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                ).versionName.toByteArray()
            }
        val chewingDataAppVersionTxt =
            File(String.format("%s/%s", chewingDataDir.absolutePath, "data_appversion.txt"))

        // update Chewing data files by version
        if (!chewingDataAppVersionTxt.exists()) {
            chewingDataAppVersionTxt.appendBytes(chewingAppVersion)
        }

        if (!chewingDataAppVersionTxt.readBytes().contentEquals(chewingAppVersion)) {
            Log.d(logTag, "Here comes a new version.")
            installChewingData(dataPath)

            // refresh app version
            val chewingDataAppVersionTxtOutputStream = FileOutputStream(chewingDataAppVersionTxt)
            chewingDataAppVersionTxtOutputStream.write(chewingAppVersion)
            chewingDataAppVersionTxtOutputStream.close()
        }
    }

    private fun installChewingData(dataPath: String) {
        // Get app data directory
        val chewingDataDir = File(dataPath)

        // Copying data files
        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            Log.d(logTag, "Copying ${file}...")
            val dataInputStream = assets.open(file)
            val dataOutputStream = FileOutputStream(destinationFile)

            try {
                dataInputStream.copyTo(dataOutputStream)
            } catch (e: java.lang.Exception) {
                e.message?.let {
                    Log.e(logTag, it)
                }
            } finally {
                Log.d(logTag, "Closing data I/O streams")
                dataInputStream.close()
                dataOutputStream.close()
            }
        }
    }

    private fun checkChewingData(dataPath: String): Boolean {
        Log.d(logTag, "Checking Chewing data files...")
        val chewingDataDir = File(dataPath)

        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            if (!destinationFile.exists()) {
                return false
            }
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // toggle main layout automatically between physical keyboard being connected and disconnected
        if (this@GuilelessBopomofoService::viewBinding.isInitialized) {
            viewBinding.keyboardPanel.switchToLayout(KeyboardPanel.KeyboardLayout.MAIN)
        }
    }

    // triggered if any sharedPreference has been changed
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "user_keyboard_layout",
            "user_display_hsu_qwerty_layout",
            "user_display_eten26_qwerty_layout",
            "user_display_dvorak_hsu_both_layout",
            -> {
                // just 'reload' the main layout
                if (this@GuilelessBopomofoService::viewBinding.isInitialized) {
                    viewBinding.keyboardPanel.switchToLayout(KeyboardPanel.KeyboardLayout.MAIN)
                }
            }

            "user_enable_space_as_selection" -> {
                if (sharedPreferences.getBoolean("user_enable_space_as_selection", true)) {
                    ChewingBridge.setSpaceAsSelection(1)
                }
            }

            "user_phrase_choice_rearward" -> {
                if (sharedPreferences.getBoolean("user_phrase_choice_rearward", false)) {
                    ChewingBridge.setPhraseChoiceRearward(true)
                }
            }

            "user_haptic_feedback_strength" -> {
                // reload the value
                userHapticFeedbackStrength =
                    sharedPreferences.getInt(
                        "user_haptic_feedback_strength",
                        defaultHapticFeedbackStrength
                    )
            }

            "same_haptic_feedback_to_function_buttons" -> {
                // do nothing
            }

            "user_fullscreen_when_in_landscape",
            "user_fullscreen_when_in_portrait",
            -> {
                // do nothing (onEvaluateFullscreenMode() will handle it well)
            }

            "user_enable_button_elevation",
            "user_key_button_height",
            "user_enable_double_touch_ime_switch",
            -> {
                // just 'reload' the main layout
                if (this@GuilelessBopomofoService::viewBinding.isInitialized) {
                    viewBinding.keyboardPanel.switchToLayout(KeyboardPanel.KeyboardLayout.MAIN)
                }
            }

            "user_enable_physical_keyboard" -> {
                // do nothing (onEvaluateInputViewShown() will handle it well)
            }

            "user_candidate_selection_keys_option" -> {
                sharedPreferences.getString("user_candidate_selection_keys_option", "NUMBER_ROW")
                    ?.let {
                        ChewingBridge.setSelKey(
                            SelectionKeys.valueOf(it).keys,
                            10
                        )
                    }
            }
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}