@echo off
REM Compile et execute les tests unitaires de GestionNotes (Windows)
cd /d "%~dp0"
setlocal enabledelayedexpansion

echo Compilation du code source...
if not exist out mkdir out
set FILES=
for /r src %%f in (*.java) do set FILES=!FILES! "%%f"
javac -encoding UTF-8 -d out !FILES!
if errorlevel 1 goto :error

echo Compilation des tests...
javac -encoding UTF-8 -cp out -d out tests\TestControleurs.java
if errorlevel 1 goto :error

echo Execution des tests...
java -Dfile.encoding=UTF-8 -cp out TestControleurs
goto :eof

:error
echo Une erreur est survenue lors de la compilation.
pause
