#!/bin/sh
# Compiles the app into the out folder.
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out -cp "lib/*" $(find src -name '*.java') && echo "Build finished. Run ./run.sh to start the app."
