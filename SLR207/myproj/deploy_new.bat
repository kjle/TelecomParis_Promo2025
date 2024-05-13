@echo
set login=jkang-23
set localFolder=.\ 
set todeploy=Server\target
set remoteFolder=\dev\shm\%login%\
set nameOfTheJarToExecute=myserver-1-jar-with-dependencies.jar

REM Create a machines.txt file with the list of computers
for /F %%i in (machines.txt) do (
  ssh %login%@%%i && rm -f %remoteFolder% && mkdir %remoteFolder%

  scp -r %localFolder%%todeploy% %login%@%%i:%remoteFolder%

  ssh -tt %login%@%%i cd %remoteFolder% && timeout /t 3 && java -jar target\%nameOfTheJarToExecute%
)