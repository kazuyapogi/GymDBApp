#!/bin/sh
# Starts the app. Run ./build.sh first.
cd "$(dirname "$0")"
java -cp "out:lib/*" gym.Main
