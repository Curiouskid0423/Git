#!/bin/bash

echo
echo "===== Welcome to Gitlet ====="
echo
echo "Proceed by initialzing gitlet repo with \`gitlet init\` command."
echo "Or continue with any gitlet command if this is a gitlet initialized"
echo "directory. Type \`gitlet help\` for help."
echo
gitlet() {
    java gitlet.Main "$@"
}
