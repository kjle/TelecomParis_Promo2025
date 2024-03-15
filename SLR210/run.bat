@echo off

setlocal enabledelayedexpansion

del summary\*.txt
del logs\*.txt

REM 定义其他不变的参数值
@REM set LEADER_ELECTION_TIMEOUT=50
set BOUND_OF_PROPOSED_NUMBER=2
set ABORT_TIMEOUT=100

REM 循环CRASH_PROBABILITY参数
for %%a in (0, 0.1, 1) do (
    REM 循环N参数
    for %%b in (3, 10, 100) do (
        for %%c in (10, 50, 100, 500, 1000) do (
            REM 根据N的值设定CRASH_NUMBER的值
            @REM if %%b==3 (set CRASH_NUMBER=1) else if %%b==10 (set CRASH_NUMBER=4) else if %%b==100 (set CRASH_NUMBER=49)
            set /a temp=%%b+1
            set /a CRASH_NUMBER=!temp!/2-1

            REM 写入参数到param.txt
            echo N = %%b> param.txt
            echo LEADER_ELECTION_TIMEOUT = %%c>> param.txt
            echo CRASH_NUMBER = !CRASH_NUMBER!>> param.txt
            echo CRASH_PROBABILITY = %%a>> param.txt
            echo BOUND_OF_PROPOSED_NUMBER = !BOUND_OF_PROPOSED_NUMBER!>> param.txt
            echo ABORT_TIMEOUT = !ABORT_TIMEOUT!>> param.txt

            REM 可以在这里调用其他需要使用param.txt文件的命令或脚本
            @REM call mvn compile
            call mvn exec:exec > logs/log.txt
            call python process.py

        )
        
    )
)
@REM 分析结果
call python analyze.py

echo Finished.

@REM call mvn clean
@REM del logs\*.txt
@REM del summary.txt

@REM call mvn compile

@REM call mvn exec:exec > logs/log.txt

@REM call python process.py

@REM summary.txt

@REM @REM pause