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
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityEngineeringModeBinding
import java.io.File

class EngineeringModeActivity : AppCompatActivity() {
    // ViewBinding
    private lateinit var viewBinding: ActivityEngineeringModeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EmojiCompat.init(BundledEmojiCompatConfig(this@EngineeringModeActivity.applicationContext))

        viewBinding = ActivityEngineeringModeBinding.inflate(this.layoutInflater)
        viewBinding.chewingDataFilesStatus.text = checkChewingDateFiles().toString()

        viewBinding.testTextInputEditText.setOnLongClickListener {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showInputMethodPicker()
            return@setOnLongClickListener true
        }

        setContentView(viewBinding.root)
    }

    private fun checkChewingDateFiles(): Boolean {
        val dataPath =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(this.packageName, 0).applicationInfo.dataDir
            } else {
                packageManager.getPackageInfo(this.packageName, PackageManager.PackageInfoFlags.of(0)).applicationInfo.dataDir
            }

        val chewingDataDir = File(dataPath)

        val chewingDataFiles =
            listOf("dictionary.dat", "index_tree.dat", "swkb.dat", "symbols.dat")

        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            if (!destinationFile.exists()) {
                return false
            }
        }
        return true
    }
}