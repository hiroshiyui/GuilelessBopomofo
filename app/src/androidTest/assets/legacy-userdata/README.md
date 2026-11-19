User data as libchewing 0.13 and earlier left it, for the migration tests.

- `userhash.dat`: the user phrase file this app kept, in libchewing's old trie
  format. It is libchewing's own test fixture `tests/data/chewing.dat`
  (LGPL-2.1-or-later), holding one phrase: 策試 ㄘㄜˋ ㄕˋ.
- `chewing-deleted.dat`: the deleted phrase file libchewing 0.13.1 creates on
  its first start, holding no phrases.

libchewing 0.14 can read this format but no longer write it, so the files are
kept here rather than generated.
