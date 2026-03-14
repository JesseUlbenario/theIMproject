@echo off
REM Run the payroll app from terminal. Put MySQL Connector/J in lib/ first.
cd /d "%~dp0"

set CP=.
if exist lib\*.jar set CP=.;lib\*

echo Compiling...
javac -encoding UTF-8 -cp "%CP%" *.java
if errorlevel 1 (
    echo Compile failed. If you see "MySQL driver not found", add mysql-connector-j jar to lib\
    pause
    exit /b 1
)

echo Running main page (no login)...
java -cp "%CP%" page
pause
