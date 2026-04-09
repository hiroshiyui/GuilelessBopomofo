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

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.DialogAddUserPhraseBinding
import org.ghostsinthelab.apps.guilelessbopomofo.databinding.FragmentUserPhraseManagerBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class UserPhraseManagerFragment : Fragment() {
    private val logTag = "UserPhraseManager"

    private var _binding: FragmentUserPhraseManagerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: UserPhraseAdapter

    private val backupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { exportUserPhrases(it) }
    }

    private val restoreLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importUserPhrases(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserPhraseManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ChewingUtil.ensureChewingConnected(requireContext())

        adapter = UserPhraseAdapter(mutableListOf()) { userPhrase ->
            confirmDeletePhrase(userPhrase)
        }
        binding.recyclerViewUserPhrases.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewUserPhrases.adapter = adapter

        binding.fabAddPhrase.setOnClickListener { showAddPhraseDialog() }
        binding.buttonBackup.setOnClickListener { backupLauncher.launch("guileless_bopomofo_user_phrases.csv") }
        binding.buttonRestore.setOnClickListener { restoreLauncher.launch(arrayOf("text/*")) }

        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                adapter.filter(query)
                updateEmptyState(query)
            }
        })

        loadUserPhrases()
    }

    override fun onResume() {
        super.onResume()
        if (this::adapter.isInitialized) {
            loadUserPhrases()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUserPhrases() {
        val phrases = ChewingUtil.enumerateUserPhrases()
        adapter.setData(phrases)
        val query = binding.editTextSearch.text?.toString()?.trim() ?: ""
        if (query.isNotEmpty()) {
            adapter.filter(query)
        }
        updateEmptyState(query)
    }

    private fun updateEmptyState(query: String) {
        val isEmpty = adapter.itemCount == 0
        binding.textViewEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewUserPhrases.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.textViewEmptyState.text = if (query.isNotEmpty()) {
            getString(R.string.search_user_phrases_no_results)
        } else {
            getString(R.string.no_user_phrases)
        }
    }

    private fun confirmDeletePhrase(userPhrase: UserPhrase) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_user_phrase_title)
            .setMessage(getString(R.string.delete_user_phrase_message, userPhrase.phrase))
            .setPositiveButton(R.string.delete_user_phrase) { _, _ ->
                ChewingBridge.chewing.userphraseRemove(userPhrase.phrase, userPhrase.bopomofo)
                ChewingUtil.flushContext(requireContext())
                loadUserPhrases()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun exportUserPhrases(uri: Uri) {
        val phrases = ChewingUtil.enumerateUserPhrases()
        if (phrases.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_user_phrases, Toast.LENGTH_SHORT).show()
            return
        }

        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write("\"phrase\",\"bopomofo\"\n")
                    for (phrase in phrases) {
                        writer.write("\"${escapeCsvField(phrase.phrase)}\",\"${escapeCsvField(phrase.bopomofo)}\"\n")
                    }
                }
            }
            Toast.makeText(
                requireContext(),
                getString(R.string.backup_success, phrases.size),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e(logTag, "Failed to export user phrases", e)
            Toast.makeText(requireContext(), R.string.backup_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun importUserPhrases(uri: Uri) {
        try {
            val lines = mutableListOf<String>()
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    lines.addAll(reader.readLines())
                }
            }

            val result = validateAndParseCsv(lines)
            if (result.error != null) {
                Log.w(logTag, "CSV validation failed: ${result.error}")
                Toast.makeText(requireContext(), result.error, Toast.LENGTH_LONG).show()
                return
            }

            val phrases = result.phrases
            if (phrases.isEmpty()) {
                Toast.makeText(requireContext(), R.string.restore_no_data, Toast.LENGTH_SHORT).show()
                return
            }

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.restore_user_phrases)
                .setMessage(getString(R.string.restore_confirm_message, phrases.size))
                .setPositiveButton(R.string.restore_user_phrases) { _, _ ->
                    var added = 0
                    for (phrase in phrases) {
                        val result = ChewingBridge.chewing.userphraseAdd(phrase.phrase, phrase.bopomofo)
                        if (result > 0) added++
                    }
                    ChewingUtil.flushContext(requireContext())
                    loadUserPhrases()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.restore_success, added),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } catch (e: Exception) {
            Log.e(logTag, "Failed to import user phrases", e)
            Toast.makeText(requireContext(), R.string.restore_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private data class CsvParseResult(
        val phrases: List<UserPhrase> = emptyList(),
        val error: String? = null,
    )

    private fun validateAndParseCsv(lines: List<String>): CsvParseResult {
        if (lines.isEmpty()) {
            return CsvParseResult(error = getString(R.string.restore_invalid_file))
        }

        // File size sanity check (reject files over 1MB worth of lines)
        val totalLength = lines.sumOf { it.length }
        if (totalLength > 1_000_000) {
            return CsvParseResult(error = getString(R.string.restore_invalid_file))
        }

        // Validate header row
        val headerLine = lines.first().trim()
        if (headerLine != "\"phrase\",\"bopomofo\"") {
            return CsvParseResult(error = getString(R.string.restore_invalid_header))
        }

        val phrases = mutableListOf<UserPhrase>()
        for ((index, line) in lines.withIndex()) {
            if (index == 0) continue // skip header
            if (line.isBlank()) continue

            val parsed = parseCsvLine(line)

            // Each row must have exactly 2 fields
            if (parsed.size != 2) {
                return CsvParseResult(
                    error = getString(R.string.restore_malformed_row, index + 1)
                )
            }

            val phrase = parsed[0]
            val bopomofo = parsed[1]

            // Both fields must be non-empty
            if (phrase.isEmpty() || bopomofo.isEmpty()) {
                return CsvParseResult(
                    error = getString(R.string.restore_malformed_row, index + 1)
                )
            }

            // Reject fields with control characters (null bytes, etc.)
            if (phrase.any { it.isISOControl() } || bopomofo.any { it.isISOControl() }) {
                return CsvParseResult(
                    error = getString(R.string.restore_malformed_row, index + 1)
                )
            }

            // Reject excessively long fields
            if (phrase.length > 100 || bopomofo.length > 200) {
                return CsvParseResult(
                    error = getString(R.string.restore_malformed_row, index + 1)
                )
            }

            // Validate bopomofo contains only valid bopomofo characters, tones, and spaces
            if (!isValidBopomofo(bopomofo)) {
                return CsvParseResult(
                    error = getString(R.string.restore_invalid_bopomofo, index + 1)
                )
            }

            phrases.add(UserPhrase(phrase, bopomofo))
        }

        return CsvParseResult(phrases = phrases)
    }

    private fun isValidBopomofo(bopomofo: String): Boolean {
        return bopomofo.all { char ->
            char == ' '
                    || char in '\u3100'..'\u312F' // Bopomofo block
                    || char in '\u31A0'..'\u31BF' // Bopomofo Extended block
                    || char == 'ˊ' || char == 'ˇ' || char == 'ˋ' || char == '˙' // tone marks
        }
    }

    private fun escapeCsvField(field: String): String {
        return field.replace("\"", "\"\"")
    }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                c == ',' && !inQuotes -> {
                    fields.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        fields.add(current.toString())
        return fields
    }

    private fun showAddPhraseDialog() {
        BopomofoLookup.init(requireContext())
        val dialogBinding = DialogAddUserPhraseBinding.inflate(layoutInflater)

        dialogBinding.buttonAnalyzeBopomofo.setOnClickListener {
            val phrase = dialogBinding.editTextPhrase.text?.toString()?.trim() ?: ""
            if (phrase.isEmpty()) return@setOnClickListener

            val combinations = BopomofoLookup.generateCombinations(phrase)
            if (combinations.isEmpty()) {
                Toast.makeText(requireContext(), R.string.analyze_bopomofo_no_results, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                combinations
            )
            dialogBinding.autoCompleteBopomofo.setAdapter(adapter)
            dialogBinding.autoCompleteBopomofo.setText(combinations[0], false)
            dialogBinding.textInputLayoutBopomofoDropdown.isVisible = true
            dialogBinding.textInputLayoutBopomofoManual.isVisible = false
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_user_phrase)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.add_user_phrase) { _, _ ->
                val phrase = dialogBinding.editTextPhrase.text?.toString()?.trim() ?: ""
                val bopomofo = if (dialogBinding.textInputLayoutBopomofoDropdown.isVisible) {
                    dialogBinding.autoCompleteBopomofo.text?.toString()?.trim() ?: ""
                } else {
                    dialogBinding.editTextBopomofo.text?.toString()?.trim() ?: ""
                }

                if (phrase.isEmpty() || bopomofo.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.user_phrase_input_empty, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val result = ChewingBridge.chewing.userphraseAdd(phrase, bopomofo)
                if (result > 0) {
                    ChewingUtil.flushContext(requireContext())
                    loadUserPhrases()
                } else {
                    Toast.makeText(requireContext(), R.string.user_phrase_add_failed, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
