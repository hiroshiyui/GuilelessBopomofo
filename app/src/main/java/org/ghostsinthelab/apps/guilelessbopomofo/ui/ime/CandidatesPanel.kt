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

package org.ghostsinthelab.apps.guilelessbopomofo.ui.ime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.ghostsinthelab.apps.guilelessbopomofo.Candidate
import org.ghostsinthelab.apps.guilelessbopomofo.R
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.BackToMainFunctionKey
import org.ghostsinthelab.apps.guilelessbopomofo.keys.virtual.CandidateButton
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessDimens
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.GuilelessFonts
import org.ghostsinthelab.apps.guilelessbopomofo.ui.theme.LocalGuilelessThemeExtras

/** Test tag of every candidate, for counting them. */
const val CANDIDATE_TEST_TAG = "candidate"

// What AppCompat's light theme draws text in, which the paged candidates used to inherit.
private val PagedCandidateTextLight = Color(0xDE000000)
private val SelectionKeyShape = RoundedCornerShape(4.dp)

/** The candidates to choose from, and a key back to the keyboard. */
@Composable
fun CandidatesPanel(uiState: ImeUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        when (uiState.candidateLayoutStyle) {
            ImeUiState.CandidateLayoutStyle.GRID -> key(uiState.candidatesGeneration) {
                CandidatesGrid(uiState.candidates)
            }

            ImeUiState.CandidateLayoutStyle.LIST -> PagedCandidates(uiState.candidates)
        }
        BackToMainKey()
    }
}

/**
 * Every candidate, in columns of [GuilelessDimens.CandidateGridRows], scrolling sideways.
 * As the RecyclerView grid did, every candidate in a column is as wide as the widest one
 * in it, so a column of labels of different lengths still lines up.
 */
@Composable
private fun CandidatesGrid(candidates: List<Candidate>) {
    val rows = GuilelessDimens.CandidateGridRows
    LazyRow(
        state = rememberLazyListState(),
        modifier = Modifier
            .fillMaxWidth()
            .height(GuilelessDimens.CandidateGridRowHeight * rows),
    ) {
        // Columns by index, so that only the candidates of columns on screen are looked at.
        items(count = (candidates.size + rows - 1) / rows) { column ->
            Column(modifier = Modifier.width(IntrinsicSize.Max)) {
                val first = column * rows
                for (index in first until minOf(first + rows, candidates.size)) {
                    CandidateItem(candidates[index])
                }
            }
        }
    }
}

@Composable
private fun CandidateItem(candidate: Candidate) {
    val virtualKey = remember(candidate) { CandidateButton(candidate) }
    KeySurface(
        key = virtualKey,
        minHeight = GuilelessDimens.CandidateMinHeight,
        background = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier
            .fillMaxWidth()
            .height(GuilelessDimens.CandidateGridRowHeight)
            .padding(horizontal = GuilelessDimens.CandidateHorizontalInset),
        testTag = CANDIDATE_TEST_TAG,
    ) {
        Text(
            text = candidate.candidateString,
            modifier = Modifier
                .widthIn(min = GuilelessDimens.CandidateMinHeight)
                .padding(horizontal = GuilelessDimens.CandidateHorizontalPadding),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = GuilelessFonts.Candidate,
            fontSize = GuilelessDimens.CandidateTextSize,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * One page of candidates, each after the selection key that picks it on a physical
 * keyboard. Only shown with one attached, so they are read, not touched.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PagedCandidates(candidates: List<Candidate>) {
    val textColor = if (LocalGuilelessThemeExtras.current.isDark) Color.White else PagedCandidateTextLight
    FlowRow(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
        candidates.forEach { candidate ->
            Row(
                modifier = Modifier.testTag(CANDIDATE_TEST_TAG),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = candidate.selectionKey.toString(),
                    modifier = Modifier
                        .padding(start = 4.dp, end = 2.dp)
                        .background(Color.Black, SelectionKeyShape)
                        .padding(horizontal = 4.dp),
                    color = Color.White,
                    fontFamily = GuilelessFonts.SelectionKey,
                    fontSize = GuilelessDimens.SelectionKeyTextSize,
                )
                Text(
                    text = candidate.candidateString,
                    modifier = Modifier.padding(end = 4.dp),
                    color = textColor,
                    fontFamily = GuilelessFonts.Candidate,
                    fontSize = GuilelessDimens.PagedCandidateTextSize,
                )
            }
        }
    }
}

@Composable
private fun BackToMainKey() {
    KeySurface(
        key = BackToMainFunctionKey,
        minHeight = GuilelessDimens.TextKeyMinHeight,
        background = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = GuilelessDimens.TextKeyHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.back_to_main_layout),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = GuilelessFonts.KeyLabel,
                fontWeight = GuilelessFonts.KeyLabelWeight,
            )
        }
    }
}
