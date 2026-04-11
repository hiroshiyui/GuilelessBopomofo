/*
 * Guileless Bopomofo
 * Copyright (C) 2025.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.ghostsinthelab.apps.guilelessbopomofo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val logTag: String = "MainActivity"

    // ViewBinding
    private lateinit var viewBinding: ActivityMainBinding

    private var engineeringModeEnterCount: Int = 0
    private val engineeringModeEnterClicks: Int = 5
    private var engineeringModeEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(logTag, "onCreate()")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        viewBinding.apply {
            textViewAppVersion.text = getString(
                R.string.app_version, BuildConfig.VERSION_NAME, ChewingBridge.chewing.version()
            )

            imageViewAppIcon.setOnClickListener {
                if (engineeringModeEnterCount >= engineeringModeEnterClicks || engineeringModeEnabled) {
                    engineeringModeEnabled = true
                    val engineeringModeIntent = Intent(this@MainActivity, EngineeringModeActivity::class.java)
                    startActivity(engineeringModeIntent)
                } else {
                    engineeringModeEnterCount += 1
                }

                return@setOnClickListener
            }

            bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_general -> {
                        switchFragment(GeneralSettingsFragment())
                        true
                    }
                    R.id.nav_user_interface -> {
                        switchFragment(UserInterfaceSettingsFragment())
                        true
                    }
                    R.id.nav_physical_keyboard -> {
                        switchFragment(PhysicalKeyboardSettingsFragment())
                        true
                    }
                    R.id.nav_user_phrases -> {
                        switchFragment(UserPhraseManagerFragment())
                        true
                    }
                    else -> false
                }
            }
        }

        setContentView(viewBinding.root)

        // Apply system-bar and cutout insets as internal padding on the header and
        // bottom navigation so the window itself can still draw edge-to-edge while
        // the contents stay clear of the status bar, navigation bar, and cutouts.
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            viewBinding.headerLayout.updatePadding(
                left = insets.left, top = insets.top, right = insets.right
            )
            viewBinding.bottomNavigation.updatePadding(
                left = insets.left, right = insets.right, bottom = insets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        // Show general settings tab by default
        if (savedInstanceState == null) {
            switchFragment(GeneralSettingsFragment())
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
