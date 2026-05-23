/*
 * Guileless Bopomofo
 * Copyright (C) 2026.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
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

package org.ghostsinthelab.apps.guilelessbopomofo.announcements

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import org.ghostsinthelab.apps.guilelessbopomofo.BuildConfig
import org.ghostsinthelab.apps.guilelessbopomofo.GuilelessBopomofoEnv
import org.json.JSONObject

object AnnouncementRepository {
    private const val LOG_TAG = "AnnouncementRepository"
    private const val ASSET_DIR = "announcements"
    private const val INDEX_FILE = "$ASSET_DIR/index.json"

    fun loadAll(context: Context): List<Announcement> {
        return try {
            val json = context.assets.open(INDEX_FILE).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            val arr = root.getJSONArray("announcements")
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        Announcement(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            date = obj.optString("date", ""),
                            file = obj.getString("file"),
                            minVersionCode = obj.optInt("minVersionCode", 0),
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to load announcements index", e)
            emptyList()
        }
    }

    fun loadMarkdown(context: Context, announcement: Announcement): String {
        return try {
            context.assets.open("$ASSET_DIR/${announcement.file}")
                .bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to load announcement body ${announcement.file}", e)
            ""
        }
    }

    fun unread(context: Context): List<Announcement> {
        val read = readIds(context)
        val versionCode = BuildConfig.VERSION_CODE
        return loadAll(context)
            .filter { it.id !in read && it.minVersionCode <= versionCode }
            .sortedBy { it.date }
    }

    fun isRead(context: Context, id: String): Boolean = id in readIds(context)

    fun markRead(context: Context, id: String) {
        val prefs = context.getSharedPreferences(
            GuilelessBopomofoEnv.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE
        )
        val current = prefs.getStringSet(GuilelessBopomofoEnv.READ_ANNOUNCEMENT_IDS, emptySet())
            ?: emptySet()
        if (id in current) return
        prefs.edit {
            putStringSet(GuilelessBopomofoEnv.READ_ANNOUNCEMENT_IDS, current + id)
        }
    }

    private fun readIds(context: Context): Set<String> {
        return context.getSharedPreferences(
            GuilelessBopomofoEnv.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE
        ).getStringSet(GuilelessBopomofoEnv.READ_ANNOUNCEMENT_IDS, emptySet()) ?: emptySet()
    }
}
