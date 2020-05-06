#!/bin/bash

echo
echo "===== Welcome to Gitlet ====="
echo
echo "Proceed by initialzing gitlet repo with \`gitlet init\` command."
echo
gitlet() {
    java gitlet.Main "$@"
}
