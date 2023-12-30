#!/bin/bash
# (C) 2020 -2021 ScalAgent Distributed Technologies
# All rights reserved

# Specific properties can be defined from environment:
# --------------------------------------------------------------------------------
# JORAMMQ_MQTT_HOME     JoramMQ installation directory containing bin, bundle and
#                       lib directories. If not set try to use current directory. 
# JORAMMQ_DATA_DIR      Location of the data directory, this normally contains all
#                       the execution data. by default: "$JORAMMQ_MQTT_HOME/data".
# JORAMMQ_STORAGE_DIR   Base directory for JoramMQ transactional persistence, by
#                       default: "$JORAMMQ_DATA_DIR/jorammq".
# JORAMMQ_CONF_DIR      Location of the config directory, this normally contains all
#                       the configuration data. by default: "$JORAMMQ_MQTT_HOME/conf".
# JORAMMQ_LOG_DIR       Location of the log directory, this normally contains all
#                       the log files. by default: "$JORAMMQ_MQTT_HOME/log".
# JORAMMQ_PID_DIR       Location of the jorammq-mqtt.pid file, this normally contains the pid
#                       of the running broker. by default: "$JORAMMQ_MQTT_HOME".
# JMX_PORT              Define JMX listening port. If not defined the JMX remote
#                       connection is not configured.
# JORAMMQ_TMP_DIR       Directory to use for temporary files. By default the OS
#                       temporary directory.
# JORAMMQ_JAVA_OPTS     Java options to add when launching JoramMQ. For example
#                       "-Djavax.net.debug=ssl:handshake:verbose" for a verbose
#                       output of SSL handshaking.

# Defines the following properties to be used by JoramMQ scripts:
# --------------------------------------------------------------------------------
# JORAMMQ_ENV_SET		Avoids useless multiples execution.
# SERVER_ID				Always 0.
# JORAMMQ_TMP_DIR_OPT	Option allowing to fix java.io.tmpdir Java property using
#						JORAMMQ_TMP_DIR property.
# CONFIG_DIR			Pathname of directory containing configuration files.
# JORAM_BUNDLE			Pathname of directory containing JoramMQ bundles.
# FELIX_CONF_FILE		Pathname of Felix configuration file.
# FELIX_JAR				Pathname of Felix library file.
# FELIX_ROOT_DIR		Pathname of Felix bundles cache directory.
# SERVER_RUN_DIR		
# JAVA					Pathname of Java executable.
# STORAGE_DIR			Pathname to JoramMQ persistence directory.

if [ "$JORAMMQ_ENV_SET" == "OK" ]; then
  return 0
fi
export JORAMMQ_ENV_SET="OK"

echo Use JORAMMQ_MQTT_HOME="$JORAMMQ_MQTT_HOME"

# Define server identifier
export SERVER_ID=0

# Define JMX listening port
# export JMX_PORT=3333

# OS specific support.  $var _must_ be set to either true or false.
export cygwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
MINGW*) cygwin=true;;
esac

if [ ! -z "$JORAMMQ_TMP_DIR" ]; then
  if $cygwin; then
	JORAMMQ_TMP_DIR=`cygpath --absolute --unix "$JORAMMQ_TMP_DIR"`
  fi
  export JORAMMQ_TMP_DIR_OPT=-Djava.io.tmpdir=$JORAMMQ_TMP_DIR
fi
export JORAMMQ_TMP_DIR

if [ -z "$JORAMMQ_DATA_DIR" ]; then
  JORAMMQ_DATA_DIR=$JORAMMQ_MQTT_HOME/data
fi
# For Cygwin, switch paths to Unix format
if $cygwin; then
  JORAMMQ_DATA_DIR=`cygpath --absolute --unix "$JORAMMQ_DATA_DIR"`
fi
export JORAMMQ_DATA_DIR
if [ ! -d "$JORAMMQ_DATA_DIR" ] ; then
  mkdir -p "$JORAMMQ_DATA_DIR"
fi

if [ -z "$JORAMMQ_STORAGE_DIR" ]; then
  JORAMMQ_STORAGE_DIR=$JORAMMQ_DATA_DIR/jorammq
fi
# For Cygwin, switch paths to Unix format
if $cygwin; then
  JORAMMQ_STORAGE_DIR=`cygpath --absolute --unix "$JORAMMQ_STORAGE_DIR"`
fi
export JORAMMQ_STORAGE_DIR
if [ ! -d "$JORAMMQ_STORAGE_DIR" ] ; then
  mkdir -p "$JORAMMQ_STORAGE_DIR"
fi

if [ -z "$JORAMMQ_CONF_DIR" ]; then
  JORAMMQ_CONF_DIR=$JORAMMQ_MQTT_HOME/conf
fi
# For Cygwin, switch paths to Unix format
if $cygwin; then
  JORAMMQ_CONF_DIR=`cygpath --absolute --unix "$JORAMMQ_CONF_DIR"`
fi
export JORAMMQ_CONF_DIR
if [ ! -d "$JORAMMQ_CONF_DIR" ] ; then
  echo "JORAMMQ_CONF_DIR is not valid: $JORAMMQ_CONF_DIR"
  exit 1
fi

if [ -z "$JORAMMQ_LOG_DIR" ]; then
  JORAMMQ_LOG_DIR=$JORAMMQ_MQTT_HOME/log
fi
# For Cygwin, switch paths to Unix format
if $cygwin; then
  JORAMMQ_LOG_DIR=`cygpath --absolute --unix "$JORAMMQ_LOG_DIR"`
fi
export JORAMMQ_LOG_DIR
if [ ! -d "$JORAMMQ_LOG_DIR" ] ; then
  mkdir -p "$JORAMMQ_LOG_DIR"
fi

if [ -z "$JORAMMQ_PID_DIR" ]; then
  JORAMMQ_PID_DIR=$JORAMMQ_MQTT_HOME
fi
# For Cygwin, switch paths to Unix format
if $cygwin; then
  JORAMMQ_PID_DIR=`cygpath --absolute --unix "$JORAMMQ_PID_DIR"`
fi
export JORAMMQ_PID_DIR
if [ ! -d "$JORAMMQ_PID_DIR" ] ; then
  mkdir -p "$JORAMMQ_PID_DIR"
fi

export CONFIG_DIR=$JORAMMQ_CONF_DIR
export JORAM_BUNDLE=$JORAMMQ_MQTT_HOME/bundle

export FELIX_CONF_FILE=$CONFIG_DIR/felix.properties
export FELIX_JAR=$JORAMMQ_MQTT_HOME/lib/felix.jar
export FELIX_ROOT_DIR=$JORAMMQ_DATA_DIR/felix/s$SERVER_ID

if [ ! -d "$JORAMMQ_DATA_DIR/felix" ] ; then
  mkdir "$JORAMMQ_DATA_DIR/felix"
fi

# Currently this directory cannot be changed due to dependencies in felix and
# logging configuration files.
export SERVER_RUN_DIR=$JORAMMQ_MQTT_HOME
if [ ! -d "$SERVER_RUN_DIR/log" ] ; then
  mkdir "$SERVER_RUN_DIR/log"
fi

export STORAGE_DIR=$JORAMMQ_STORAGE_DIR/s$SERVER_ID

if [ ! -z "$JAVA_HOME" ]; then
  if [ ! -d "$JAVA_HOME" ]; then
    echo "JAVA_HOME is not valid: $JAVA_HOME"
    exit 1
  fi
  # For Cygwin, switch paths to Unix format
  if $cygwin; then
    JAVA_HOME=`cygpath --absolute --unix "$JAVA_HOME"`
  fi
  if [ -z "$JAVA" ]; then
    export JAVA="$JAVA_HOME/bin/java"
  fi
else
  echo "JAVA_HOME is not defined use default"
  export JAVA="java"
fi
