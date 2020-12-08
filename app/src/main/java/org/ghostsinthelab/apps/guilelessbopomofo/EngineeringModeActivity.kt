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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.ActivityEngineeringModeBinding
import java.io.File

class EngineeringModeActivity : AppCompatActivity() {

    private val LOGTAG: String = "EngineeringMode"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityEngineeringModeBinding: ActivityEngineeringModeBinding = ActivityEngineeringModeBinding.inflate(this.layoutInflater)
        activityEngineeringModeBinding.chewingDataFilesStatus.text = checkChewingDateFiles().toString()
        setContentView(activityEngineeringModeBinding.root)
    }

    private fun checkChewingDateFiles(): Boolean {
        val dataPath =
            packageManager.getPackageInfo(this.packageName, 0).applicationInfo.dataDir

        val chewingDataDir = File(dataPath)

        val chewingDataFiles =
            listOf("dictionary.dat", "index_tree.dat", "pinyin.tab", "swkb.dat", "symbols.dat")

        for (file in chewingDataFiles) {
            val destinationFile = File(String.format("%s/%s", chewingDataDir.absolutePath, file))
            if (!destinationFile.exists()) {
                return false
            }
        }
        return true
    }
}