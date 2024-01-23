# INF224

[webpage](https://perso.telecom-paristech.fr/elc/inf224/)

The aim of this TP is to create a software for a multimedia set-top box that can play videos, films, display photos and more.

----| INF224

    ----| cpp

        ----| ressource

        ----| Makefile

        ----| *.cpp

        ----| *.h

        
        ----| ....
    
    ----| html
    
        ----| index.html
    
        ----| ...
    
    ----| swing
    
        ----| Makefile
    
        ----| *.java

## How to run ?

/!\ This program runs on Linux ! 
If you want to run on MacOS, it is necessary to modify `cpp/photo.h` and `cpp/video.h`. Change `#define Ubuntu` to `#define Mac` to enable the flag. 
If you want to run on Windows, move the flag.

On Linux, please check that the cmd `imagej` and `mvp` work without errors.

To execute the Server

```
cd ./cpp
make
./main
```

To execute the Client

```
cd ./swing
make
java MainFrame
```
/!\ Remember : Connect the Server first !



