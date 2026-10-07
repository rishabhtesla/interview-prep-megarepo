#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"

# This directory is reserved for this script; run one instance at a time.
build=.build-dsa
mkdir -p "$build/classes"
trap 'rm -rf -- "$build"' EXIT
find . -type f -name '*.java' ! -path "./$build/*" ! -path './out/*' \
    ! -path './support/*' ! -path './tests/*' | LC_ALL=C sort > "$build/solutions.txt"
count=$(wc -l < "$build/solutions.txt" | tr -d '[:space:]')
if [[ "$count" != 50 ]]; then
    printf 'Expected exactly 50 solution files, found %s\n' "$count" >&2
    exit 1
fi
find . -type f -name '*.java' ! -path "./$build/*" ! -path './out/*' \
    | LC_ALL=C sort > "$build/all-sources.txt"
javac --release 17 -Xlint:all -Werror -d "$build/classes" @"$build/all-sources.txt"
java -cp "$build/classes" CourseTests "$build/solutions.txt"
