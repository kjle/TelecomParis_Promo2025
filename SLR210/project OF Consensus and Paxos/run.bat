@echo off

setlocal enabledelayedexpansion

del summary\*.txt
del logs\*.txt

@REM set LEADER_ELECTION_TIMEOUT=50
set BOUND_OF_PROPOSED_NUMBER=2
set ABORT_TIMEOUT=100

@REM CRASH_PROBABILITY
for %%a in (0, 0.1, 0.5, 1) do (
    @REM N
    for %%b in (3, 10, 50, 100) do (
		@REM tle
        for %%c in (10, 50, 100, 500, 1000) do (
            @REM CRASH_NUMBER
            set /a temp=%%b+1
            set /a CRASH_NUMBER=!temp!/2-1

            @REM write to param.txt
            echo N = %%b> param.txt
            echo LEADER_ELECTION_TIMEOUT = %%c>> param.txt
            echo CRASH_NUMBER = !CRASH_NUMBER!>> param.txt
            echo CRASH_PROBABILITY = %%a>> param.txt
            echo BOUND_OF_PROPOSED_NUMBER = !BOUND_OF_PROPOSED_NUMBER!>> param.txt
            echo ABORT_TIMEOUT = !ABORT_TIMEOUT!>> param.txt

            @REM call mvn compile
            call mvn exec:exec > logs/log.txt
            call python process.py

        )
        
    )
)

call python analyze.py

echo Finished.