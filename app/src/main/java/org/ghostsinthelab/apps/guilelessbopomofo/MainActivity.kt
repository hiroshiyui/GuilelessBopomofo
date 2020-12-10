/*
 * Guileless Bopomofo
 * Copyright (C) 2020 YOU, HUI-HONG
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

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val LOGTAG: String = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private var engineeringModeEnterCount: Int = 0
    private val engineeringModeEnterClicks: Int = 5
    private val imeSettingsRequestCode: Int = 254
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(LOGTAG, "onCreate()")
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("GuilelessBopomofoService", MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.textViewAppVersion.text =
            applicationContext.packageManager.getPackageInfo(this.packageName, 0).versionName
        val view = binding.root

        binding.imageViewAppIcon.setOnClickListener {
            engineeringModeEnterCount += 1
            if (engineeringModeEnterCount < engineeringModeEnterClicks) {
                val engineeringModeHint: String = getString(
                    R.string.engineering_mode_hint,
                    (engineeringModeEnterClicks - engineeringModeEnterCount)
                )
                val engineeringModeHintToast: Toast =
                    Toast.makeText(this, engineeringModeHint, Toast.LENGTH_SHORT)
                engineeringModeHintToast.show()
            } else {
                val engineeringModeIntent = Intent(this, EngineeringModeActivity::class.java)
                startActivity(engineeringModeIntent)
            }

            return@setOnClickListener
        }

        updateGuilelessBopomofoStatus()

        binding.buttonLaunchImeSystemSettings?.setOnClickListener {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            startActivityForResult(intent, imeSettingsRequestCode)
        }

        for ((button, layout) in
        mapOf<RadioButton, String>(
            binding.radioButtonLayoutDaChen to "KB_DEFAULT",
            binding.radioButtonLayoutETen26 to "KB_ET26",
            binding.radioButtonLayoutHsu to "KB_HSU"
        )) {
            button.setOnClickListener {
                sharedPreferences.edit().putString("user_keyboard_layout", layout).apply()
            }
        }

        setContentView(view)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imeSettingsRequestCode) {
            updateGuilelessBopomofoStatus()
        }
    }

    private fun enabledInputMethodList(): List<InputMethodInfo> {
        val inputMethodManager: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        return inputMethodManager.enabledInputMethodList
    }

    private fun isGuilelessBopomofoEnabled(): Boolean {
        enabledInputMethodList().forEach {
            if (it.serviceName == "org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoService") {
                return true
            }
        }
        return false
    }

    private fun updateGuilelessBopomofoStatus() {
        if (isGuilelessBopomofoEnabled()) {
            binding.textViewServiceStatus?.text = getString(R.string.service_is_enabled)
        } else {
            binding.textViewServiceStatus?.text = getString(R.string.service_is_disabled)
        }
    }
}
