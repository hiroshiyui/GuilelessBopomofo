#!/usr/bin/env bash
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
# Builds the English next-word prediction list from Simple English Wikipedia, counting which
# word follows which in its articles. Only words in the SCOWL list count, so build that one
# first (tools/english-wordlist/build.sh).
#
# Output: app/src/main/assets/english/wikipedia-bigrams.txt, see count_bigrams.py.
#
# Wikimedia keeps dated dumps for a few months only. Once this one is gone from
# dumps.wikimedia.org, look for it on archive.org, or download it yourself and pass its path.
#
# Usage: tools/english-bigrams/build.sh [simplewiki-20261001-pages-articles.xml.bz2]

set -euo pipefail

DUMP_DATE="20261001"
DUMP_NAME="simplewiki-${DUMP_DATE}-pages-articles.xml.bz2"
DUMP_URL="https://dumps.wikimedia.org/simplewiki/${DUMP_DATE}/${DUMP_NAME}"
DUMP_SHA1="f55881650e447c39e8bdeba4b1bb8d5576342d4b"

tool_dir="$(cd "$(dirname "$0")" && pwd)"
repo_root="$(cd "${tool_dir}/../.." && pwd)"
out_dir="${repo_root}/app/src/main/assets/english"
work_dir="$(mktemp -d)"
trap 'rm -rf "${work_dir}"' EXIT

dump="${1:-}"
if [[ -z "${dump}" ]]; then
    dump="${work_dir}/${DUMP_NAME}"
    curl -fsSL -o "${dump}" "${DUMP_URL}"
fi
echo "${DUMP_SHA1}  ${dump}" | sha1sum -c --quiet -

python3 -I "${tool_dir}/count_bigrams.py" "${dump}" "${out_dir}/scowl-words.txt" "${work_dir}/wikipedia-bigrams.txt"
cp "${work_dir}/wikipedia-bigrams.txt" "${out_dir}/wikipedia-bigrams.txt"
echo "Wrote $(wc -l < "${out_dir}/wikipedia-bigrams.txt") lines, $(wc -c < "${out_dir}/wikipedia-bigrams.txt") bytes to ${out_dir}/wikipedia-bigrams.txt"
