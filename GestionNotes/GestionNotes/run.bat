@echo off
REM Compile et lance l'application GestionNotes (Windows)
cd /d "%~dp0"

echo Compilation en cours...
if not exist out mkdir out

set FILES=
for /r src %%f in (*.java) do set FILES=!FILES! "%%f"

setlocal enabledelayedexpansion
set FILES=
for /r src %%f in (*.java) do set FILES=!FILES! "%%f"
javac -encoding UTF-8 -d out !FILES!
if errorlevel 1 goto :error

echo Copie des images...
xcopy /E /I /Y src\images out\images >nul

echo Lancement de l'application...
java -Dfile.encoding=UTF-8 -cp out main.Main
goto :eof

:error
echo Une erreur est survenue lors de la compilation.
pause
