@echo off
REM (C) 2014 - 2021 ScalAgent Distributed Technologies
REM All rights reserved

if "%OS%" == "Windows_NT" setlocal

REM --------------------------------------------------------------------------------
REM Set JORAMMQ_MQTT_HOME if not defined

set "CURRENT_DIR=%cd%"
if not "%JORAMMQ_MQTT_HOME%" == "" goto gotHome
echo The JORAMMQ_MQTT_HOME environment variable is not defined.

set "JORAMMQ_MQTT_HOME=%CURRENT_DIR%"
if exist "%JORAMMQ_MQTT_HOME%\bin\jorammq-admin.bat" goto okHome1

cd ..
set "JORAMMQ_MQTT_HOME=%cd%"
cd "%CURRENT_DIR%"
if exist "%JORAMMQ_MQTT_HOME%\bin\jorammq-admin.bat" goto okHome1

echo Can not define JORAMMQ_MQTT_HOME environment variable
goto end

:gotHome
if exist "%JORAMMQ_MQTT_HOME%\bin\jorammq-admin.bat" goto okHome0

echo The JORAMMQ_MQTT_HOME environment variable is not defined correctly: "%JORAMMQ_MQTT_HOME%"
goto end

:okHome1
:okHome0
REM JORAMMQ_MQTT_HOME is now correctly set

REM set environment
CALL "%JORAMMQ_MQTT_HOME%\bin\setenv.bat"
REM --------------------------------------------------------------------------------

set "SSH_CLASSPATH=%JORAM_BUNDLE%\sshd-osgi.jar;%JORAM_BUNDLE%\slf4j-api.jar;%JORAM_BUNDLE%\slf4j-jdk14.jar"
set "A3_CLASSPATH=%JORAM_BUNDLE%\a3-rt.jar;%JORAM_BUNDLE%\a3-common.jar;%JORAM_BUNDLE%\txlog.jar;%FELIX_JAR%;%JORAM_BUNDLE%\monolog.jar"
set "JORAM_CLASSPATH=%JORAM_BUNDLE%\ow2-jms-2.0-spec.jar;%JORAM_BUNDLE%\joram-client-jms.jar;%JORAM_BUNDLE%\joram-shared.jar;%JORAM_BUNDLE%\jndi-client.jar;%JORAM_BUNDLE%\jcup.jar"
set "JORAMMQ_CLASSPATH=%JORAM_BUNDLE%\jorammq-mqtt-util.jar;%JORAM_BUNDLE%\commons-cli.jar"

set A3_DEBUG=-Dfr.dyade.aaa.DEBUG_DIR="%CONFIG_DIR%" -Dfr.dyade.aaa.DEBUG_FILE="cmdlog.properties"
set CMD=-Dserver.config.file="%CONFIG_DIR%\jorammq.xml" -Dserver.data.dir="%STORAGE_DIR%" -Dfelix.config.file="%FELIX_CONF_FILE%"

cd %SERVER_RUN_DIR%
"%JAVA%" %A3_DEBUG% %JORAMMQ_JAVA_OPTS% %JORAMMQ_TMP_DIR_OPT% %CMD% -Dserver.id=%SERVER_ID% -classpath "%SSH_CLASSPATH%;%A3_CLASSPATH%;%JORAM_CLASSPATH%;%JORAMMQ_CLASSPATH%" com.scalagent.jorammq.mqtt.util.AdminCommand "%1" "%2" "%3" "%4" "%5" "%6"

:end
