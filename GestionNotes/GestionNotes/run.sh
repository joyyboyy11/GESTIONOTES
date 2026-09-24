#!/bin/bash
# Compile et lance l'application GestionNotes (Linux / macOS)
set -e
cd "$(dirname "$0")"

echo "Compilation en cours..."
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")

echo "Copie des images..."
mkdir -p out/images
cp -r src/images/. out/images/

echo "Lancement de l'application..."
java -Dfile.encoding=UTF-8 -cp out main.Main
