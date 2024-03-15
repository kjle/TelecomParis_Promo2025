@echo off
setlocal enableextensions

del savedatas\*.pickle

REM 循环执行run.bat五次
for /L %%i in (1,1,5) do (
    echo Executing run.bat %%i time
    call run.bat
)

echo Executing draw.py

del figures\*.png

call python draw.py

echo All executions completed.