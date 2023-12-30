@echo off
REM (C) 2014 - 2021 ScalAgent Distributed Technologies
REM All rights reserved

if "%OS%" == "Windows_NT" setlocal

if "%1%"=="" goto usage
if "%2%"=="" goto usage
if not "%3%"=="" goto usage

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

set CLASSPATH="%JORAM_BUNDLE%\jorammq-mqtt-accesscontrol.jar;%JORAM_BUNDLE%\monolog.jar"
set A3_PROPERTIES=-Dfr.dyade.aaa.DEBUG_DIR="%CONFIG_DIR%" -Dfr.dyade.aaa.DEBUG_FILE="cmdlog.properties"

"%JAVA%"  %A3_PROPERTIES% -classpath %CLASSPATH% com.scalagent.jorammq.mqtt.accesscontrol.Password %CONFIG_DIR%\passwd.properties %1 %2
goto end

:usage
echo Usage: jorammq-passwd [username] [password]
goto end

:end
