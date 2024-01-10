/*
 * @brief: This class is used to manage all the multimedia files
 */
#ifndef MULTIMEDIAMANAGER_H
#define MULTIMEDIAMANAGER_H

#include <iostream>
#include <map>
#include <memory>
#include "multimedia.h"
#include "photo.h"
#include "video.h"
#include "film.h"
#include "group.h"

class MultimediaManager
{
private:
    map<string, shared_ptr<Multimedia> > multimediaTable;
    map<string, shared_ptr<Group> > groupTable;

public:
    MultimediaManager();
    virtual ~MultimediaManager();

    // template<typename T> shared_ptr<T> createMultimedia(string name, string path, T *multimedia);

    shared_ptr<Photo> createPhoto(string name, string path, double latitude, double longitude);
    shared_ptr<Video> createVideo(string name, string path, int duration);
    shared_ptr<Film> createFilm(string name, string path, int duration, int chapiterNum, int *chapiters);
    shared_ptr<Group> createGroup(string name);

    void findMultimedia(ostream& ostr, string name);
    void findGroup(ostream& ostr, string name);

    void playMultimedia(string name);

    void deleteMultimedia(string name);
    void deleteGroup(string name);

    // Serialization
    void saveMultimedia(const string &outputFile);
    void loadMultimedia(const string &inputFile);
};

#endif // MULTIMEDIAMANAGER_H
