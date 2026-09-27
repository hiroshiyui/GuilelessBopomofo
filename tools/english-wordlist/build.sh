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
# Builds the English word prediction list from SCOWL (http://wordlist.aspell.net/).
#
# Output: app/src/main/assets/english/scowl-words.txt, one "word<TAB>level" per line,
# where level is the SCOWL size the word first appears in (10 = most common).
# SCOWL's copyright notice is copied next to it, as its license asks for.
#
# Usage: tools/english-wordlist/build.sh

set -euo pipefail

SCOWL_VERSION="2020.12.07"
SCOWL_URL="https://downloads.sourceforge.net/project/wordlist/SCOWL/${SCOWL_VERSION}/scowl-${SCOWL_VERSION}.tar.gz"
SCOWL_SHA256="5587667caa20c4891390c2d42dbb4d5c4c3f41bee77af1457ece3ba23fb859cc"
LEVELS=(10 20 35 40 50 55 60)
CATEGORIES=(words upper contractions proper-names)
SPELLINGS=(english american)

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
out_dir="${repo_root}/app/src/main/assets/english"
work_dir="$(mktemp -d)"
trap 'rm -rf "${work_dir}"' EXIT

curl -fsSL -o "${work_dir}/scowl.tar.gz" "${SCOWL_URL}"
echo "${SCOWL_SHA256}  ${work_dir}/scowl.tar.gz" | sha256sum -c --quiet -
tar -xzf "${work_dir}/scowl.tar.gz" -C "${work_dir}"
final_dir="${work_dir}/scowl-${SCOWL_VERSION}/final"

for level in "${LEVELS[@]}"; do
    for spelling in "${SPELLINGS[@]}"; do
        for category in "${CATEGORIES[@]}"; do
            list="${final_dir}/${spelling}-${category}.${level}"
            [[ -f "${list}" ]] || continue
            iconv -f latin1 -t utf-8 "${list}" | sed "s/\$/\t${level}/"
        done
    done
done |
    # plain ASCII words and contractions only; possessives would flood the suggestions
    grep -E $'^[A-Za-z\']+\t' | grep -vE $'\'s\t' |
    # one entry per word regardless of case: the most common level wins, then the lowercase form
    awk -F '\t' '{ key = tolower($1)
                   if (!(key in level) || $2 < level[key] || ($2 == level[key] && $1 == key)) {
                       level[key] = $2; word[key] = $1
                   } }
                 END { for (key in word) print key "\t" word[key] "\t" level[key] }' |
    LC_ALL=C sort -t $'\t' -k1,1 | cut -f 2,3 > "${work_dir}/scowl-words.txt"

mkdir -p "${out_dir}"
cp "${work_dir}/scowl-words.txt" "${out_dir}/scowl-words.txt"
cp "${final_dir}/../Copyright" "${out_dir}/SCOWL-COPYRIGHT.txt"
echo "Wrote $(wc -l < "${out_dir}/scowl-words.txt") words to ${out_dir}/scowl-words.txt"
