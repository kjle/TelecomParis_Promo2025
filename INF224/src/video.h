/*
 * @brief: subclass of Multimedia, to specify the propotes of the video which include one parameter
 */
#ifndef VIDEO_H
#define VIDEO_H

#define Ubuntu

#include <iostream>
#include <string>
#include "multimedia.h"

class Video : public Multimedia
{
private:
    double length;

public:
/*
* Constructor of the class Video with parameters
* @param name: name of the video
* @param path: path of the video
* @param length: length of the video
*/
    Video() : length(0) {};
    Video(double length) : length(length) {};
    Video(string name, string path, double length) : Multimedia(name, path), length(length) {};

/*
 * Destructor of the class Video
 */
    virtual ~Video() {
        cout << "Video object: " << getName() << " deleted !" << endl;
    };

/*
 * Getters and setters of the class Video
 * @param length: length of the video
 * @return length: length of the video
 */
    double getLength() const { return length; };
    void setLength(double length) { this->length = length; };

/*
 * Display the video object with the name, the path, and the length
 * @param ostrm: output stream
 */
    virtual void display(std::ostream& ostrm) const {
        ostrm << "Video object:" << endl;
        ostrm << "  Name: " << getName() << endl;
        ostrm << "  Path: " << getPath() << endl;
        ostrm << "  length: " << length << endl;
    };
    
/*
 * Play the video object with the mpv
 */
    virtual void play() const {
        #if defined(Ubuntu)
        string cmd = "mpv " + getPath() + getName() + " &";
        #elif defined(Mac)
        string cmd = "open " + getPath() + getName();
        #else
        string cmd = "show " + getPath() + getName();
        #endif

        system(cmd.c_str());
    };
};
#endif // VIDEO_H
