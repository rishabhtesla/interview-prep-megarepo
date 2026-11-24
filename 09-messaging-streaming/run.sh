#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
build=.build
rm -rf "$build"
trap 'rm -rf "$build"' EXIT
mkdir -p "$build"
javac --release 17 -Xlint:all -Werror -d "$build" \
  src/MessageSemantics.java src/BrokerSemantics.java src/OutboxInbox.java
java -cp "$build" MessageSemantics
java -cp "$build" BrokerSemantics
java -cp "$build" OutboxInbox
