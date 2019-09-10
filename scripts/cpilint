#!/usr/bin/env bash

# *****************************************************
# * If the CPILINT_HOME variable is not set, assume   *
# * that the home directory is the parent of the      *
# * directory containing this script.                 *
# *****************************************************

if [ -z "$CPILINT_HOME" ]; then
   slink=$(readlink "$0")
   if [ -z "$slink" ]; then
      home="$(dirname "$(dirname "$0")")"
   else
      home="$(dirname "$(dirname "$slink")")"
   fi
else
   home="$CPILINT_HOME"
fi

# *****************************************************
# * Build CPILint's class path. All required library  *
# * files are in the lib directory, and the Logback   *
# * configuration file is in the logback directory.   *
# *****************************************************

cpilint_cp="$home/lib/*:$home/logback"

# *****************************************************
# * Use the CPILINT_JAVA_HOME variable to locate the  *
# * java command. If that variable is undefined, use  *
# * the JAVA_HOME variable instead. If that is also   *
# * undefined, assume that the command is in the      *
# * current user's path.                              *
# *****************************************************

if [ ! -z "$CPILINT_JAVA_HOME" ]; then
   javacmd="$CPILINT_JAVA_HOME/bin/java"
elif [ ! -z "$JAVA_HOME" ]; then
   javacmd="$JAVA_HOME/bin/java"
else
   javacmd=java
fi

# *****************************************************
# * If the -debug option was provided on the command  *
# * line, enable assertions.                          *
# *****************************************************

for arg in "$@"
do
   if [ "$arg" = "-debug" ]; then
      asserts=" -ea"
      break
   fi
done

# *****************************************************
# * Launch CPILint!                                   *
# *****************************************************

"$javacmd"$asserts -classpath "$cpilint_cp" dk.mwittrock.cpilint.CliClient $*
