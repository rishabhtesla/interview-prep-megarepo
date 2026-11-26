#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"

# Compile the complete default-package course so linked-list, tree, and graph
# examples can use the shared node types in support/.
build=.build-examples
mkdir -p "$build/classes"
trap 'rm -rf -- "$build"' EXIT

find . -type f -name '*.java' ! -path "./$build/*" ! -path './out/*' \
    ! -path './support/*' ! -path './tests/*' | LC_ALL=C sort > "$build/solutions.txt"
count=$(wc -l < "$build/solutions.txt" | tr -d '[:space:]')
if [[ "$count" != 73 ]]; then
    printf 'Expected exactly 73 solution files, found %s\n' "$count" >&2
    exit 1
fi

find . -type f -name '*.java' ! -path "./$build/*" ! -path './out/*' \
    | LC_ALL=C sort > "$build/all-sources.txt"
javac --release 17 -Xlint:all -Werror -d "$build/classes" @"$build/all-sources.txt"

while IFS= read -r source; do
    class_name=${source##*/}
    class_name=${class_name%.java}
    printf '\n--- %s ---\n' "$class_name"
    java -cp "$build/classes" "$class_name"
done < "$build/solutions.txt"

printf '\nAll %s solution examples passed.\n' "$count"
