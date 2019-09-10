@echo off

setlocal

rem *****************************************************
rem * If the CPILINT_HOME environment variable is not   *
rem * set, assume that the home directory is the parent *
rem * of the directory containing this batch file.      *
rem *****************************************************

set HOME=%~dp0..
if defined CPILINT_HOME set HOME=%CPILINT_HOME%

rem *****************************************************
rem * Build CPILint's class path. All required library  *
rem * files are in the lib directory, and the Logback   *
rem * configuration file is in the logback directory.   *
rem *****************************************************

set CPILINT_CP=%HOME%\lib\*;%HOME%\logback

rem *****************************************************
rem * Use the CPILINT_JAVA_HOME environment variable to *
rem * locate java.exe. If that variable is undefined,   *
rem * use the JAVA_HOME environment variable instead.   *
rem * If that is also undefined, assume that java.exe   *
rem * is in the current user's PATH.                    *
rem *****************************************************

set JAVACMD=java.exe
if defined JAVA_HOME set JAVACMD=%JAVA_HOME%\bin\java.exe
if defined CPILINT_JAVA_HOME set JAVACMD=%CPILINT_JAVA_HOME%\bin\java.exe

rem *****************************************************
rem * If the -debug option was provided on the command  *
rem * line, enable assertions.                          *
rem *****************************************************

:check-for-debug
if [%1]==[] goto launch
if /i [%1]==[-debug] (
    set ASSERTS= -ea
    goto launch
)
shift
goto check-for-debug

rem *****************************************************
rem * Launch CPILint!                                   *
rem *****************************************************

:launch
"%JAVACMD%"%ASSERTS% -classpath "%CPILINT_CP%" dk.mwittrock.cpilint.CliClient %*
