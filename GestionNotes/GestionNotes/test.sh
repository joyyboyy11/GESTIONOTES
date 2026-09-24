#!/bin/bash
# Compile et exécute les tests unitaires de GestionNotes (Linux / macOS)
set -e
cd "$(dirname "$0")"

echo "Compilation du code source..."
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")

echo "Compilation des tests..."
javac -encoding UTF-8 -cp out -d out tests/TestControleurs.java

echo "Exécution des tests..."
java -Dfile.encoding=UTF-8 -cp out TestControleurs
