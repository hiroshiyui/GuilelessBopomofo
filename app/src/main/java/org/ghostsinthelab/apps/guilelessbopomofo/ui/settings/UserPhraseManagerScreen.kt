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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import org.ghostsinthelab.apps.guilelessbopomofo.BopomofoLookup
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingBridge
import org.ghostsinthelab.apps.guilelessbopomofo.ChewingUtil
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.UserPhrase
import org.ghostsinthelab.apps.guilelessbopomofo.UserPhraseCsv
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

private const val LOG_TAG = "UserPhraseManager"
private const val BACKUP_FILE_NAME = "guileless_bopomofo_user_phrases.csv"
private const val BACKUP_MIME_TYPE = "text/csv"
private const val RESTORE_MIME_TYPE = "text/*"
private val ListBottomPadding = 72.dp

/** The dialogs this screen can show; only one at a time. */
private sealed interface UserPhraseDialog {
    data class ConfirmDelete(val userPhrase: UserPhrase) : UserPhraseDialog
    data class ConfirmRestore(val phrases: List<UserPhrase>) : UserPhraseDialog
    data object ConfirmResetFirst : UserPhraseDialog
    data object ConfirmResetSecond : UserPhraseDialog
    data object AddPhrase : UserPhraseDialog
}

@Composable
fun UserPhraseManagerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var allPhrases by remember { mutableStateOf(emptyList<UserPhrase>()) }
    var query by rememberSaveable { mutableStateOf("") }
    var dialog by remember { mutableStateOf<UserPhraseDialog?>(null) }

    fun reload() {
        allPhrases = ChewingUtil.enumerateUserPhrases()
    }

    LifecycleResumeEffect(Unit) {
        ChewingUtil.ensureChewingConnected(context)
        reload()
        onPauseOrDispose {}
    }

    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BACKUP_MIME_TYPE)
    ) { uri -> uri?.let { exportUserPhrases(context, it) } }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importUserPhrases(context, it) }?.let { phrases ->
            if (phrases.isEmpty()) {
                context.toast(R.string.restore_no_data)
            } else {
                dialog = UserPhraseDialog.ConfirmRestore(phrases)
            }
        }
    }

    val trimmedQuery = query.trim()
    val shownPhrases = remember(allPhrases, trimmedQuery) {
        if (trimmedQuery.isEmpty()) {
            allPhrases
        } else {
            allPhrases.filter { it.phrase.contains(trimmedQuery) || it.bopomofo.contains(trimmedQuery) }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                SettingsButton(stringResource(R.string.backup_user_phrases), { backupLauncher.launch(BACKUP_FILE_NAME) })
                SettingsButton(stringResource(R.string.restore_user_phrases), { restoreLauncher.launch(arrayOf(RESTORE_MIME_TYPE)) })
                SettingsButton(stringResource(R.string.reset_user_phrase_data), { dialog = UserPhraseDialog.ConfirmResetFirst })
            }
            HorizontalDivider()
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                placeholder = { Text(stringResource(R.string.search_user_phrases_hint)) },
                leadingIcon = { Icon(painterResource(android.R.drawable.ic_menu_search), contentDescription = null) },
                singleLine = true,
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (shownPhrases.isEmpty()) {
                    Text(
                        text = stringResource(
                            if (trimmedQuery.isNotEmpty()) R.string.search_user_phrases_no_results else R.string.no_user_phrases
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = ListBottomPadding),
                    ) {
                        items(shownPhrases, key = { "${it.phrase}\u0000${it.bopomofo}" }) { userPhrase ->
                            UserPhraseItem(userPhrase) { dialog = UserPhraseDialog.ConfirmDelete(userPhrase) }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { dialog = UserPhraseDialog.AddPhrase },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            Icon(painterResource(android.R.drawable.ic_input_add), contentDescription = stringResource(R.string.add_user_phrase))
        }
    }

    val dismiss = { dialog = null }
    when (val current = dialog) {
        null -> Unit
        is UserPhraseDialog.ConfirmDelete -> ConfirmDialog(
            title = stringResource(R.string.delete_user_phrase_title),
            message = stringResource(R.string.delete_user_phrase_message, current.userPhrase.phrase),
            confirmLabel = stringResource(R.string.delete_user_phrase),
            onDismiss = dismiss,
        ) {
            dismiss()
            ChewingBridge.chewing.userphraseRemove(current.userPhrase.phrase, current.userPhrase.bopomofo)
            ChewingUtil.flushContext(context)
            reload()
        }

        is UserPhraseDialog.ConfirmRestore -> ConfirmDialog(
            title = stringResource(R.string.restore_user_phrases),
            message = stringResource(R.string.restore_confirm_message, current.phrases.size),
            confirmLabel = stringResource(R.string.restore_user_phrases),
            onDismiss = dismiss,
        ) {
            dismiss()
            val added = current.phrases.count { ChewingBridge.chewing.userphraseAdd(it.phrase, it.bopomofo) > 0 }
            ChewingUtil.flushContext(context)
            reload()
            context.toast(context.getString(R.string.restore_success, added))
        }

        // Asked twice on purpose: there is no undo for this one.
        UserPhraseDialog.ConfirmResetFirst -> ConfirmDialog(
            title = stringResource(R.string.reset_user_phrase_data),
            message = stringResource(R.string.reset_user_phrase_data_confirm_first),
            confirmLabel = stringResource(R.string.reset_user_phrase_data),
            onDismiss = dismiss,
        ) { dialog = UserPhraseDialog.ConfirmResetSecond }

        UserPhraseDialog.ConfirmResetSecond -> ConfirmDialog(
            title = stringResource(R.string.reset_user_phrase_data),
            message = stringResource(R.string.reset_user_phrase_data_confirm_second),
            confirmLabel = stringResource(R.string.reset_user_phrase_data),
            onDismiss = dismiss,
        ) {
            dismiss()
            ChewingUtil.resetUserPhraseData(context)
            reload()
            context.toast(R.string.reset_user_phrase_data_done)
        }

        UserPhraseDialog.AddPhrase -> AddUserPhraseDialog(onDismiss = dismiss) { phrase, bopomofo ->
            if (phrase.isEmpty() || bopomofo.isEmpty()) {
                context.toast(R.string.user_phrase_input_empty)
                return@AddUserPhraseDialog
            }
            dismiss()
            if (ChewingBridge.chewing.userphraseAdd(phrase, bopomofo) > 0) {
                ChewingUtil.flushContext(context)
                reload()
            } else {
                context.toast(R.string.user_phrase_add_failed)
            }
        }
    }
}

@Composable
private fun UserPhraseItem(userPhrase: UserPhrase, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = userPhrase.phrase, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = userPhrase.bopomofo,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                painterResource(android.R.drawable.ic_menu_delete),
                contentDescription = stringResource(R.string.delete_user_phrase),
            )
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddUserPhraseDialog(onDismiss: () -> Unit, onAdd: (phrase: String, bopomofo: String) -> Unit) {
    val context = LocalContext.current
    var phrase by rememberSaveable { mutableStateOf("") }
    var manualBopomofo by rememberSaveable { mutableStateOf("") }
    // Once analyzed, the dropdown of suggested readings replaces the manual field.
    var combinations by remember { mutableStateOf(emptyList<String>()) }
    var chosenBopomofo by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_user_phrase)) },
        text = {
            Column {
                OutlinedTextField(
                    value = phrase,
                    onValueChange = { phrase = it },
                    label = { Text(stringResource(R.string.user_phrase_input_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                FilledTonalButton(
                    onClick = {
                        val trimmed = phrase.trim()
                        if (trimmed.isEmpty()) return@FilledTonalButton
                        BopomofoLookup.init(context)
                        val found = BopomofoLookup.generateCombinations(trimmed)
                        if (found.isEmpty()) {
                            context.toast(R.string.analyze_bopomofo_no_results)
                        } else {
                            combinations = found
                            chosenBopomofo = found.first()
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(stringResource(R.string.analyze_bopomofo))
                }
                if (combinations.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it },
                        modifier = Modifier.padding(top = 8.dp),
                    ) {
                        OutlinedTextField(
                            value = chosenBopomofo,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            label = { Text(stringResource(R.string.bopomofo_input_hint)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                        )
                        ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                            combinations.forEach { combination ->
                                DropdownMenuItem(
                                    text = { Text(combination) },
                                    onClick = {
                                        chosenBopomofo = combination
                                        dropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = manualBopomofo,
                        onValueChange = { manualBopomofo = it },
                        label = { Text(stringResource(R.string.bopomofo_input_hint)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.analyze_bopomofo_hint),
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val bopomofo = if (combinations.isNotEmpty()) chosenBopomofo else manualBopomofo
                onAdd(phrase.trim(), bopomofo.trim())
            }) {
                Text(stringResource(R.string.add_user_phrase))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) } },
    )
}

private fun Context.toast(@StringRes message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

private fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

private fun exportUserPhrases(context: Context, uri: Uri) {
    val phrases = ChewingUtil.enumerateUserPhrases()
    if (phrases.isEmpty()) {
        context.toast(R.string.no_user_phrases)
        return
    }

    try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                writer.write(UserPhraseCsv.format(phrases))
            }
        }
        context.toast(context.getString(R.string.backup_success, phrases.size))
    } catch (e: Exception) {
        Log.e(LOG_TAG, "Failed to export user phrases", e)
        context.toast(R.string.backup_failed)
    }
}

/** The phrases in the file at [uri], or null (after telling the user why) if it can not be used. */
private fun importUserPhrases(context: Context, uri: Uri): List<UserPhrase>? {
    return try {
        val lines = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readLines() }
        } ?: emptyList()

        when (val result = UserPhraseCsv.parse(lines)) {
            is UserPhraseCsv.ParseResult.Success -> result.phrases
            else -> {
                val message = describeParseFailure(context, result)
                Log.w(LOG_TAG, "CSV validation failed: $message")
                context.toast(message, Toast.LENGTH_LONG)
                null
            }
        }
    } catch (e: Exception) {
        Log.e(LOG_TAG, "Failed to import user phrases", e)
        context.toast(R.string.restore_failed)
        null
    }
}

private fun describeParseFailure(context: Context, result: UserPhraseCsv.ParseResult): String = when (result) {
    is UserPhraseCsv.ParseResult.UnreadableFile -> context.getString(R.string.restore_invalid_file)
    is UserPhraseCsv.ParseResult.UnexpectedHeader -> context.getString(R.string.restore_invalid_header)
    is UserPhraseCsv.ParseResult.MalformedRow ->
        context.getString(R.string.restore_malformed_row, result.lineNumber)

    is UserPhraseCsv.ParseResult.InvalidBopomofo ->
        context.getString(R.string.restore_invalid_bopomofo, result.lineNumber)

    is UserPhraseCsv.ParseResult.Success -> ""
}
