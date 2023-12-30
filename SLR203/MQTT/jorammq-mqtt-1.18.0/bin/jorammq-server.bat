@echo off
REM (C) 2014 - 2022 ScalAgent Distributed Technologies
REM All rights reserved

REM Since JoramMQ 1.15 the configuration is automatically updated at each restart.
REM If you want keep the old behavior set the UPDATE_CONF_AUTO variable below to any
REM value other than OK
set UPDATE_CONF_AUTO=OK

set JVM_PROPERTIES=-server -Xmx2G
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
REM Fixes JVM Properties for JAVA >= 10

REM for /f tokens^=2-5^ delims^=.-_^" %%j in ('"%JAVA%" -fullversion 2^>^&1') do @set "JAVA_VER=%%j%%k%%l%%m"
REM for /f tokens^=2-5^ delims^=.-_^" %%j in ('"%JAVA%" -version 2^>^&1 ^| findstr /I version') do @set "JAVA_VER=%%j%%k%%l%%m"

REM for /f tokens^=2-5^ delims^=.-_^" %%j in ('"%JAVA%" -version 2^>^&1 ^| findstr /I version') do @set "JAVA_MAJOR=%%j"
REM if not "%JAVA_MAJOR%" == "1" (
REM   set JVM_PROPERTIES=%JVM_PROPERTIES% --add-modules java.se.ee --illegal-access=warn
REM   echo Use JVM properties: %JVM_PROPERTIES%
REM )

set CLASSPATH="%FELIX_JAR%"
set FELIX_PROPERTIES=-Dgosh.args=--nointeractive -Dfelix.config.properties="file:%FELIX_CONF_FILE%" -Dfelix.cache.rootdir="%FELIX_ROOT_DIR%"
set A3_OSGI_PROPERTIES=-Dfr.dyade.aaa.agent.AgentServer.id=%SERVER_ID% -Dfr.dyade.aaa.agent.AgentServer.storage="%STORAGE_DIR%" -Dfr.dyade.aaa.osgi.exit=true
set A3_PROPERTIES=-Dfr.dyade.aaa.agent.A3CONF_DIR="%CONFIG_DIR%" -Dfr.dyade.aaa.agent.A3CONF_FILE="jorammq.xml" -Dfr.dyade.aaa.agent.useDefaultConfiguration=false -Dfr.dyade.aaa.DEBUG_DIR="%CONFIG_DIR%" -Dfr.dyade.aaa.DEBUG_FILE="log.properties" -Dcom.scalagent.jorammq.mqtt.adapter.data.path="%JORAMMQ_DATA_DIR%\jorammq-swap"
REM set SSL_DEBUG=-Djavax.net.debug=ssl,handshake,data

if "%UPDATE_CONF_AUTO%"=="OK" (
  if exist "%STORAGE_DIR%" (
    echo Removes OSGi cache directory and updates JoramMQ configuration
    if exist "%FELIX_ROOT_DIR%" (
      rmdir /S /Q "%FELIX_ROOT_DIR%" 1>NUL 2>NUL
      if exist "%FELIX_ROOT_DIR%" (
        echo Cannot remove OSGi cache, probably the server is already running
        goto end
      )
    )
    call "%JORAMMQ_MQTT_HOME%\bin\jorammq-admin.bat" -update
    if not "%errorlevel%"=="0" goto end
  )
)

if not "%JMX_PORT%"=="" (
  set JMX_PROPERTIES=-Dcom.sun.management.jmxremote.port=%JMX_PORT% -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote
  echo JMX listening to port %JMX_PORT%
)

echo Launching JoramMQ MQTT server %SERVER_ID% data_dir:%JORAMMQ_DATA_DIR%

cd %SERVER_RUN_DIR%
"%JAVA%" %JVM_PROPERTIES% %JORAMMQ_JAVA_OPTS% %JORAMMQ_TMP_DIR_OPT% %FELIX_PROPERTIES% %A3_OSGI_PROPERTIES% %A3_PROPERTIES% %JMX_PROPERTIES% -Dhawtdispatch.workaround-select-spin=true -classpath %CLASSPATH% org.apache.felix.main.Main

:end
