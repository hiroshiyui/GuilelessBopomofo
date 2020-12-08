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
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var engineeringModeEnterCount: Int = 0
    private val engineeringModeEnterClicks: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.textViewAppVersion?.text = applicationContext.packageManager.getPackageInfo(this.packageName, 0).versionName ?: ""
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

        setContentView(view)
    }

}
