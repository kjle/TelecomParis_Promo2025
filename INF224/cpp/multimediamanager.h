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

/// @brief This class is used to manage all the multimedia files
class MultimediaManager
{
private:
/// @brief map of multimedia objects
    map<string, shared_ptr<Multimedia> > multimediaTable;

/// @brief map of group objects    
    map<string, shared_ptr<Group> > groupTable;

public:

    MultimediaManager();
    virtual ~MultimediaManager();

/// @brief create a photo object and insert it to the multimedia table
/// @param name: name of the photo
/// @param path: path of the photo
/// @param latitude: latitude of the photo
/// @param longitude: longitude of the photo
    shared_ptr<Photo> createPhoto(string name, string path, double latitude, double longitude);

/// @brief create a video object and insert it to the multimedia table    
/// @param name: name of the video
/// @param path: path of the video
/// @param duration: duration of the video
    shared_ptr<Video> createVideo(string name, string path, int duration);

/// @brief create a film object and insert it to the multimedia table
/// @param name: name of the film
/// @param path: path of the film
/// @param duration: duration of the film
/// @param chapiterNum: number of chapiters
/// @param chapiters: array of chapiters
    shared_ptr<Film> createFilm(string name, string path, int duration, int chapiterNum, int *chapiters);

/// @brief create a group object and insert it to the group table
/// @param name: name of the group
    shared_ptr<Group> createGroup(string name);

/// @brief find a multimedia object in the multimedia table and display it
/// @param ostr: output stream
/// @param name: name of the multimedia object
    void findMultimedia(ostream& ostr, string name);

/// @brief find a group object in the group table and display it
/// @param ostr: output stream
/// @param name: name of the group object
    void findGroup(ostream& ostr, string name);

/// @brief play a multimedia object
/// @param name: name of the multimedia object
    void playMultimedia(string name);

/// @brief delete a multimedia object from the multimedia table
/// @param name: name of the multimedia object
    void deleteMultimedia(string name);

/// @brief delete a group object from the group table
/// @param name: name of the group object
    void deleteGroup(string name);

    // Serialization
/// @brief save the multimedia objects to a file
/// @param outputFile: name of the output file
    void saveMultimedia(const string &outputFile);

/// @brief load the multimedia objects from a file
/// @param inputFile: name of the input file
    void loadMultimedia(const string &inputFile);
};

#endif // MULTIMEDIAMANAGER_H
