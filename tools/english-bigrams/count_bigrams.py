#!/usr/bin/env python3
#
# Guileless Bopomofo
# Copyright (C) 2025.  YOU, Hui-Hong <hiroshi@miyabi-hiroshi.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#
# Counts which word follows which in a MediaWiki pages-articles dump, for English next-word
# prediction. Standard library only, so it runs on any Python 3.
#
# Usage: count_bigrams.py <pages-articles.xml.bz2> <scowl-words.txt> <output>
#
# Output: one "previous<TAB>follower follower ..." line per previous word, sorted by the
# previous word, followers most frequent first. Both words have to be in the SCOWL word list,
# and followers are written the way SCOWL spells them, or capitalized where that pair is
# nearly always written so: "United States", but "the states".

import bz2
import re
import sys
from collections import Counter, defaultdict
from xml.etree.ElementTree import iterparse

MIN_COUNT = 3
MAX_FOLLOWERS = 16  # EnglishPrediction.MAX_SUGGESTIONS
# how much of a pair has to be written capitalized for the follower to be suggested so
CAPITALIZED_SHARE = 0.8
# the only single letters that are words on their own rather than initials, "U.S." say
SINGLE_LETTER_WORDS = {"a", "i"}

COMMENT = re.compile(r"<!--.*?-->", re.DOTALL)
REF = re.compile(r"<ref[^>/]*/>|<ref[^>]*>.*?</ref>", re.DOTALL | re.IGNORECASE)
# whole elements whose content is never running text
NON_TEXT = re.compile(r"<(math|gallery|code|pre|syntaxhighlight|source|timeline|score)[^>]*>.*?</\1>",
                      re.DOTALL | re.IGNORECASE)
TAG = re.compile(r"<[^>]+>")
TEMPLATE = re.compile(r"\{\{[^{}]*\}\}")
TABLE = re.compile(r"\{\|[^{}]*?\|\}", re.DOTALL)
FILE_LINK = re.compile(r"\[\[(?:file|image|category|media):[^\[\]]*\]\]", re.IGNORECASE)
INNER_LINK = re.compile(r"\[\[(?:[^\[\]|]*\|)?([^\[\]|]*)\]\]")
EXTERNAL_LINK = re.compile(r"\[(?:https?:)?//[^\s\]]*\s*([^\]]*)\]")
EMPHASIS = re.compile(r"'{2,}")
HEADING = re.compile(r"^=+.*?=+\s*$", re.MULTILINE)
LINE_MARKUP = re.compile(r"^[*#:;|!]+.*$", re.MULTILINE)
ENTITY = re.compile(r"&(?:nbsp|#160);")

# a word, or a run of anything that is neither a word nor plain spacing, which breaks the chain
TOKEN = re.compile(r"([A-Za-z']+)|[^A-Za-z' \t]+")


def strip_nested(pattern, text):
    while True:
        stripped = pattern.sub("", text)
        if stripped == text:
            return text
        text = stripped


def plain_text(wikitext):
    text = COMMENT.sub("", wikitext)
    text = REF.sub("", text)
    text = NON_TEXT.sub("", text)
    text = strip_nested(TEMPLATE, text)
    text = strip_nested(TABLE, text)
    text = FILE_LINK.sub("", text)
    text = INNER_LINK.sub(r"\1", text)
    text = EXTERNAL_LINK.sub(r"\1", text)
    text = TAG.sub("", text)
    text = EMPHASIS.sub("", text)
    text = HEADING.sub("", text)
    # lists and tables are rarely sentences, leave them out
    text = LINE_MARKUP.sub("", text)
    text = ENTITY.sub(" ", text)
    return text.replace("’", "'")


def count_pairs(text, vocabulary, counts, capitalized):
    previous = None
    for match in TOKEN.finditer(text):
        spelling = (match.group(1) or "").strip("'")
        word = spelling.lower()
        # punctuation, digits, newlines and unknown words: no pair crosses them
        if word not in vocabulary:
            previous = None
            continue
        if previous is not None:
            counts[previous][word] += 1
            if spelling[0].isupper():
                capitalized[previous][word] += 1
        previous = word


def spelling_after(previous, word, count, vocabulary, capitalized):
    """SCOWL's spelling, unless that is lowercase and the pair is nearly always capitalized."""
    spelling = vocabulary[word]
    if spelling == word and capitalized[previous][word] >= CAPITALIZED_SHARE * count:
        return word[:1].upper() + word[1:]
    return spelling


def articles(dump_path):
    """Yields the wikitext of every main namespace page that is not a redirect."""
    with bz2.open(dump_path, "rb") as dump:
        namespace = None
        redirect = False
        for event, element in iterparse(dump, events=("end",)):
            tag = element.tag.rsplit("}", 1)[-1]
            if tag == "ns":
                namespace = element.text
            elif tag == "redirect":
                redirect = True
            elif tag == "text":
                if namespace == "0" and not redirect and element.text:
                    yield element.text
            elif tag == "page":
                namespace = None
                redirect = False
                element.clear()


def read_vocabulary(scowl_path):
    """Maps the lowercase form of every SCOWL word to how SCOWL spells it."""
    vocabulary = {}
    with open(scowl_path, encoding="utf-8") as scowl:
        for line in scowl:
            word = line.split("\t", 1)[0].strip()
            if word:
                if len(word) > 1 or word.lower() in SINGLE_LETTER_WORDS:
                    vocabulary[word.lower()] = word
    return vocabulary


def main(dump_path, scowl_path, output_path):
    vocabulary = read_vocabulary(scowl_path)
    counts = defaultdict(Counter)
    capitalized = defaultdict(Counter)
    pages = 0
    for wikitext in articles(dump_path):
        count_pairs(plain_text(wikitext), vocabulary, counts, capitalized)
        pages += 1
        if pages % 50000 == 0:
            print(f"{pages} pages", file=sys.stderr)

    lines = pairs = 0
    with open(output_path, "w", encoding="utf-8", newline="\n") as output:
        for previous in sorted(counts):
            followers = sorted(((count, word) for word, count in counts[previous].items() if count >= MIN_COUNT),
                               key=lambda entry: (-entry[0], entry[1]))[:MAX_FOLLOWERS]
            if not followers:
                continue
            output.write(previous + "\t" + " ".join(spelling_after(previous, word, count, vocabulary, capitalized) for count, word in followers) + "\n")
            lines += 1
            pairs += len(followers)
    print(f"Counted {pages} pages, wrote {lines} words with {pairs} followers to {output_path}", file=sys.stderr)


if __name__ == "__main__":
    if len(sys.argv) != 4:
        sys.exit(f"Usage: {sys.argv[0]} <pages-articles.xml.bz2> <scowl-words.txt> <output>")
    main(*sys.argv[1:])
