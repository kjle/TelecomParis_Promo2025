@echo off
REM (C) 2020 -2021 ScalAgent Distributed Technologies
REM All rights reserved

REM Specific properties can be defined from environment:
REM --------------------------------------------------------------------------------
REM JORAMMQ_MQTT_HOME     JoramMQ installation directory containing bin, bundle and
REM                       lib directories. If not set try to use current directory. 
REM JORAMMQ_DATA_DIR      Location of the data directory, this normally contains all
REM                       the execution data. by default: "%JORAMMQ_MQTT_HOME%\data".
REM JORAMMQ_STORAGE_DIR   Base directory for JoramMQ transactional persistence, by
REM                       default: "%JORAMMQ_DATA_DIR%\jorammq".
REM JORAMMQ_CONF_DIR      Location of the config directory, this normally contains all
REM                       the configuration data. by default: "%JORAMMQ_MQTT_HOME%\conf".
REM JORAMMQ_LOG_DIR       Location of the log directory, this normally contains all
REM                       the log files. by default: "%JORAMMQ_MQTT_HOME%\log".
REM JORAMMQ_PID_DIR       Location of the jorammq-mqtt.pid file, this normally contains the pid
REM                       of the running broker. by default: "%JORAMMQ_MQTT_HOME%".
REM JMX_PORT              Define JMX listening port. If not defined the JMX remote
REM                       connection is not configured.
REM JORAMMQ_TMP_DIR       Directory to use for temporary files. By default the OS
REM                       temporary directory.
REM JORAMMQ_JAVA_OPTS     Java options to add when launching JoramMQ. For example
REM                       "-Djavax.net.debug=ssl:handshake:verbose" for a verbose
REM                       output of SSL handshaking.

REM Defines the following properties to be used by JoramMQ scripts:
REM --------------------------------------------------------------------------------
REM JORAMMQ_ENV_SET		Avoids useless multiples execution.
REM SERVER_ID				Always 0.
REM JORAMMQ_TMP_DIR_OPT	Option allowing to fix java.io.tmpdir Java property using
REM						JORAMMQ_TMP_DIR property.
REM CONFIG_DIR			Pathname of directory containing configuration files.
REM JORAM_BUNDLE			Pathname of directory containing JoramMQ bundles.
REM FELIX_CONF_FILE		Pathname of Felix configuration file.
REM FELIX_JAR				Pathname of Felix library file.
REM FELIX_ROOT_DIR		Pathname of Felix bundles cache directory.
REM SERVER_RUN_DIR		
REM JAVA					Pathname of Java executable.
REM STORAGE_DIR			Pathname to JoramMQ persistence directory.

if "%JORAMMQ_ENV_SET%" == "OK" goto end_ok
set JORAMMQ_ENV_SET=OK

echo Use JORAMMQ_MQTT_HOME="%JORAMMQ_MQTT_HOME%"

REM Define server identifier
set SERVER_ID=0

REM Define JMX listening port
REM set JMX_PORT=3333

if not "%JORAMMQ_TMP_DIR%" == "" set "JORAMMQ_TMP_DIR_OPT=-Djava.io.tmpdir=%JORAMMQ_TMP_DIR%"

if "%JORAMMQ_DATA_DIR%" == "" set "JORAMMQ_DATA_DIR=%JORAMMQ_MQTT_HOME%\data"
if "%JORAMMQ_STORAGE_DIR%" == "" set "JORAMMQ_STORAGE_DIR=%JORAMMQ_DATA_DIR%\jorammq"

if not exist "%JORAMMQ_DATA_DIR%" mkdir "%JORAMMQ_DATA_DIR%"
if not exist "%JORAMMQ_STORAGE_DIR%" mkdir "%JORAMMQ_STORAGE_DIR%"

if "%JORAMMQ_CONF_DIR%" == "" set "JORAMMQ_CONF_DIR=%JORAMMQ_MQTT_HOME%\conf"
if not exist "%JORAMMQ_CONF_DIR%" (
  echo JORAMMQ_CONF_DIR is not valid: "%JORAMMQ_CONF_DIR%"
  goto fail
)

if "%JORAMMQ_LOG_DIR%" == "" set "JORAMMQ_LOG_DIR=%JORAMMQ_MQTT_HOME%\log"
if not exist "%JORAMMQ_LOG_DIR%" mkdir "%JORAMMQ_LOG_DIR%"

if "%JORAMMQ_PID_DIR%" == "" set "JORAMMQ_PID_DIR=%JORAMMQ_MQTT_HOME%"
if not exist "%JORAMMQ_PID_DIR%" mkdir "%JORAMMQ_PID_DIR%"

set "CONFIG_DIR=%JORAMMQ_CONF_DIR%"
set "JORAM_BUNDLE=%JORAMMQ_MQTT_HOME%\bundle"

set "FELIX_CONF_FILE=%CONFIG_DIR%/felix.properties"
set "FELIX_JAR=%JORAMMQ_MQTT_HOME%\lib\felix.jar"
set "FELIX_ROOT_DIR=%JORAMMQ_DATA_DIR%/felix/s%SERVER_ID%"
if not exist "%JORAMMQ_DATA_DIR%\felix" mkdir "%JORAMMQ_DATA_DIR%\felix"

REM Currently this directory cannot be changed due to dependencies in felix and
REM logging configuration files.
set "SERVER_RUN_DIR=%JORAMMQ_MQTT_HOME%"
if not exist "%SERVER_RUN_DIR%\log" mkdir "%SERVER_RUN_DIR%\log"

set "STORAGE_DIR=%JORAMMQ_STORAGE_DIR%\s%SERVER_ID%"

if "%JAVA_HOME%" == "" (
  echo JAVA_HOME is not defined use default
  set JAVA=java
)

if not "%JAVA_HOME%" == "" (
  if not exist "%JAVA_HOME%" (
    echo JAVA_HOME is not valid: "%JAVA_HOME%"
    goto fail
  )
  if "%JAVA%" == "" set "JAVA=%JAVA_HOME%\bin\java"
)
goto end_ok

:fail
exit -1

:end_ok
