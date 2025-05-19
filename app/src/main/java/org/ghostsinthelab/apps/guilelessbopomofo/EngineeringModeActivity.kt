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
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityEngineeringModeBinding
import java.io.File
import java.util.concurrent.Executor

class EngineeringModeActivity : AppCompatActivity() {
    private val logTag = "EngineeringModeActivity"
    private lateinit var sharedPreferences: SharedPreferences

    // ViewBinding
    private lateinit var viewBinding: ActivityEngineeringModeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fontLoadExecutor: Executor = Executor { }
        val emojiCompatConfig: EmojiCompat.Config = BundledEmojiCompatConfig(
            this@EngineeringModeActivity.applicationContext, fontLoadExecutor
        )
        EmojiCompat.init(emojiCompatConfig)

        sharedPreferences = getSharedPreferences("GuilelessBopomofoService", MODE_PRIVATE)

        viewBinding = ActivityEngineeringModeBinding.inflate(this.layoutInflater)

        // Chewing data files status
        val chewingDataFilesStatusText: String = if (checkChewingDateFiles()) {
            getString(R.string.chewing_data_files_status_ok)
        } else {
            getString(R.string.chewing_data_files_status_error)
        }
        viewBinding.chewingDataFilesStatus.text = chewingDataFilesStatusText

        // Hardware keyboard type
        val hardwareKeyboardTypeText: String = when (resources.configuration.keyboard) {
            Configuration.KEYBOARD_QWERTY -> getString(R.string.hardware_keyboard_type_qwerty)
            Configuration.KEYBOARD_12KEY -> getString(R.string.hardware_keyboard_type_12key)
            else -> getString(R.string.hardware_keyboard_type_unknown_or_missing)
        }
        viewBinding.hardwareKeyboardType.text = hardwareKeyboardTypeText

        // Hardware keyboard hidden status
        val hardwareKeyboardHiddenStatusText: String =
            when (resources.configuration.hardKeyboardHidden) {
                Configuration.HARDKEYBOARDHIDDEN_NO -> getString(R.string.hardware_keyboard_hidden_status_no)
                Configuration.HARDKEYBOARDHIDDEN_YES -> getString(R.string.hardware_keyboard_hidden_status_yes)
                Configuration.HARDKEYBOARDHIDDEN_UNDEFINED -> getString(R.string.hardware_keyboard_hidden_status_undefined)
                else -> getString(R.string.hardware_keyboard_hidden_status_unknown)
            }
        viewBinding.hardwareKeyboardHiddenStatus.text = hardwareKeyboardHiddenStatusText

        // User preferred hardware keyboard?
        val userPreferredHardwareKeyboardText: String =
            when (sharedPreferences.getBoolean("user_enable_physical_keyboard", false)) {
                true -> getString(R.string.user_preferred_hardware_keyboard_yes)
                false -> getString(R.string.user_preferred_hardware_keyboard_no)
                else -> {
                    getString(R.string.user_preferred_hardware_keyboard_unknown)
                }
            }
        viewBinding.userPreferredHardwareKeyboard.text = userPreferredHardwareKeyboardText

        viewBinding.testTextInputEditText.setOnLongClickListener {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showInputMethodPicker()
            return@setOnLongClickListener true
        }

        setContentView(viewBinding.root)
    }

    private fun checkChewingDateFiles(): Boolean {
        val dataPath = applicationInfo.dataDir
        val chewingDataDir = File(dataPath)
        val chewingDataFiles = ChewingUtil.listOfDataFiles()

        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            Log.d(logTag, "Destination file: $destinationFile")
            if (!destinationFile.exists()) {
                return false
            }
        }
        return true
    }
}